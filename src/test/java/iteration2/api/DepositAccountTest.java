package iteration2.api;

import api.dao.TransactionDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.enums.bank.BankMessage;
import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.TransactionType;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyRequestV2;
import api.models.accounts.deposit.DepositMoneyResponseV2;
import api.models.accounts.transactions.ReadAccountTransactionsResponse;
import api.models.comparison.ModelAssertions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DepositAccountTest extends BaseTest {
    private static final ThreadLocal<CreateAccountResponse> ownAccount = new ThreadLocal<>();

    @BeforeEach
    public void setUpBeforeClass() {
        ownAccount.set(SessionStorage.getUserSteps().createAccount());
    }

    @Test
    @UserSession(type = "API")
    @APIVersion("with_database_with_fix")
    public void userCanDepositMoneyToHisOwnAccountTest() {
        DepositMoneyRequestV2 depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequestV2.class);
        depositMoneyRequest.setId(ownAccount.get().getId());

        DepositMoneyResponseV2 depositMoneyResponse = new ValidatedCrudRequester<DepositMoneyResponseV2>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsOK())
                .post(depositMoneyRequest);

        ModelAssertions.assertThatModels(depositMoneyRequest, depositMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(ownAccount.get().getId());

        ModelAssertions.assertThatModels(depositMoneyRequest, transactionsResponse).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.get().getId())
                .build();

        List<Transaction> depositTransaction = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.get().getId(), expectedTransaction);

        Assertions.assertThat(depositTransaction.size()).isEqualTo(1);

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        DaoAndModelAssertions.assertThat(depositMoneyResponse, transactionDao).match();
    }

    @Test
    @UserSession(type = "API", value = 2)
    @APIVersion("with_database_with_fix")
    public void userCannotDepositMoneyToNotHisAccountTest() {
        DepositMoneyRequestV2 depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequestV2.class);
        depositMoneyRequest.setId(ownAccount.get().getId());

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser(2).getUsername(), SessionStorage.getUser(2).getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsForbiddenRequest())
                .post(depositMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyRequest.getBalance())
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.get().getId())
                .build();

        List<Transaction> transactions = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.get().getId(), expectedTransaction);
        assertThat(transactions.size()).isZero();

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDao).isNull();
    }

    @Test
    @UserSession(type = "API")
    public void userCannotDepositMoneyToNotExistedAccountTest() {
        DepositMoneyRequestV2 depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequestV2.class);
        depositMoneyRequest.setId(-1);

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsForbiddenRequest())
                .post(depositMoneyRequest);
    }


    public static Stream<Arguments> depositInvalidDataV1() {
        return Stream.of(
                Arguments.of(0.009, BankMessage.DEPOSIT_AMOUNT_MUST_BE_MORE_ZERO_POINT_ZERO_ONE.getMessage()),
                Arguments.of(-5000, BankMessage.DEPOSIT_AMOUNT_MUST_BE_MORE_ZERO_POINT_ZERO_ONE.getMessage()),
                Arguments.of(5000.001, BankMessage.DEPOSIT_AMOUNT_CANNOT_EXCEED_FIVE_THOUSAND.getMessage())
        );
    }

    @MethodSource("depositInvalidDataV1")
    @ParameterizedTest(name = "Deposit {0} should return error: {1}")
    @UserSession(type = "API")
    @APIVersion("with_validation_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double balance, String errorValue) {
        DepositMoneyRequestV2 depositMoneyRequest = DepositMoneyRequestV2.builder()
                .id(ownAccount.get().getId())
                .balance(balance)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(depositMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(balance)
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.get().getId())
                .build();

        List<Transaction> transactions = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.get().getId(), expectedTransaction);

        assertThat(transactions.size()).isZero();

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDao).isNull();
    }

    public static Stream<Arguments> depositInvalidDataV2() {
        return Stream.of(
                Arguments.of(0.009, BankMessage.DEPOSIT_AMOUNT_MUST_BE_MORE_ZERO_POINT_ZERO_ONE.getMessage()),
                Arguments.of(-5000, BankMessage.INVALID_ACCOUNT_OR_AMOUNT.getMessage()),
                Arguments.of(5000.001, BankMessage.DEPOSIT_AMOUNT_EXCEEDS_THE_FIVE_THOUSAND_LIMIT.getMessage())
        );
    }

    @MethodSource("depositInvalidDataV2")
    @ParameterizedTest(name = "Deposit {0} should return error: {1}")
    @UserSession(type = "API")
    @APIVersion("with_database_with_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV2(double balance, String errorValue) {
        DepositMoneyRequestV2 depositMoneyRequest = DepositMoneyRequestV2.builder()
                .id(ownAccount.get().getId())
                .balance(balance)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(depositMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(balance)
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.get().getId())
                .build();

        List<Transaction> transactions = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.get().getId(), expectedTransaction);

        assertThat(transactions.size()).isZero();

        TransactionDao transactionDao = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDao).isNull();
    }

    @AfterEach
    void tearDown() {
        ownAccount.remove();
    }
}
