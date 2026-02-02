package iteration2.api;

import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.TransactionType;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyRequest;
import api.models.accounts.deposit.DepositMoneyResponse;
import api.models.accounts.transactions.ReadAccountTransactionsResponse;
import api.models.comparison.ModelAssertions;
import org.assertj.core.api.Assertions;
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
    private static CreateAccountResponse ownAccount;

    @BeforeEach
    public void setUpBeforeClass() {
        ownAccount = SessionStorage.getUserSteps().createAccount();
    }

    @Test
    @UserSession(type = "API")
    public void userCanDepositMoneyToHisOwnAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(ownAccount.getId());

        DepositMoneyResponse depositMoneyResponse = new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositMoneyRequest);

        ModelAssertions.assertThatModels(depositMoneyRequest, depositMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(ownAccount.getId());

        ModelAssertions.assertThatModels(depositMoneyRequest, transactionsResponse).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.getId())
                .build();

        List<Transaction> depositTransaction = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.getId(), expectedTransaction);

        Assertions.assertThat(depositTransaction.size()).isEqualTo(1);
    }

    @Test
    @UserSession(type = "API", value = 2)
    public void userCannotDepositMoneyToNotHisAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(ownAccount.getId());

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser(2).getUsername(), SessionStorage.getUser(2).getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsForbiddenRequest())
                .post(depositMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyRequest.getBalance())
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.getId())
                .build();

        List<Transaction> transactions = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.getId(), expectedTransaction);

        assertThat(transactions.size()).isZero();
    }

    @Test
    @UserSession(type = "API")
    public void userCannotDepositMoneyToNotExistedAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(-1);

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsForbiddenRequest())
                .post(depositMoneyRequest);
    }


    public static Stream<Arguments> depositInvalidData() {
        return Stream.of(
                Arguments.of(0.009, "Deposit amount must be at least 0.01"),
                Arguments.of(-5000, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.001, "Deposit amount cannot exceed 5000")
        );
    }

    @MethodSource("depositInvalidData")
    @ParameterizedTest(name = "Deposit {0} should return error: {1}")
    @UserSession(type = "API")
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double balance, String errorValue) {
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(ownAccount.getId())
                .balance(balance)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(depositMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(balance)
                .type(TransactionType.DEPOSIT)
                .relatedAccountId(ownAccount.getId())
                .build();

        List<Transaction> transactions = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccount.getId(), expectedTransaction);

        assertThat(transactions.size()).isZero();
    }
}
