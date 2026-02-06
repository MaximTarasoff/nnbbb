package iteration2.api;

import api.dao.TransactionDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.TransactionType;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyResponse;
import api.models.accounts.transactions.ReadAccountTransactionsResponse;
import api.models.accounts.transfer.TransferMoneyRequest;
import api.models.accounts.transfer.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
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

public class TransferAccountTest extends BaseTest {
    private static final ThreadLocal<CreateAccountResponse> ownAccountOne = new ThreadLocal<>();
    private static final ThreadLocal<CreateAccountResponse> ownAccountTwo = new ThreadLocal<>();
    private static final ThreadLocal<CreateAccountResponse> otherAccountOne = new ThreadLocal<>();

    @BeforeEach
    public void setUpBeforeClass() {
        ownAccountOne.set(SessionStorage.getUserSteps(1).createAccount());
        ownAccountTwo.set(SessionStorage.getUserSteps(1).createAccount());
    }

    @Test
    @UserSession(type = "API")
    @APIVersion("with_database_with_fix")
    public void AuthUserCanTransferMoneyBetweenHisOwnAccountsTest() {
        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.get().getId()
        );

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(ownAccountOne.get().getId())
                .amount(depositMoneyResponse.getBalance())
                .receiverAccountId(ownAccountTwo.get().getId())
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest);

        ModelAssertions.assertThatModels(transferMoneyRequest, transferMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(ownAccountTwo.get().getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransactionOne);
        DaoAndModelAssertions.assertThat(transferMoneyResponse, transactionDaoOne).match();

        Transaction expectedTransactionTwo = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_IN)
                .relatedAccountId(ownAccountOne.get().getId())
                .build();

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountTwo.get().getId(), expectedTransactionTwo);

        assertThat(transactionsAccountTwo.size()).isEqualTo(1);

        TransactionDao transactionDaoTwo = DataBaseSteps.getTransactionByTransaction(expectedTransactionTwo);
        softly.assertThat(transactionDaoTwo.getRelatedAccountId()).isEqualTo(expectedTransactionTwo.getRelatedAccountId());
        softly.assertThat(transactionDaoTwo.getAmount()).isEqualTo(expectedTransactionTwo.getAmount());

    }

    @Test
    @UserSession(type = "API", value = 2)
    @APIVersion("with_database_with_fix")
    public void AuthUserCanTransferMoneyToNotHisAccountTest() {
        otherAccountOne.set(SessionStorage.getUserSteps(2).createAccount());

        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.get().getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.get().getId())
                        .amount(depositMoneyResponse.getBalance())
                        .receiverAccountId(otherAccountOne.get().getId())
                        .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest);

        ModelAssertions.assertThatModels(transferMoneyRequest, transferMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser(2).getUsername(), SessionStorage.getUser(2).getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(otherAccountOne.get().getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(otherAccountOne.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps(1)
                .getAccountTransactionsByParams(
                        ownAccountOne.get().getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        DaoAndModelAssertions.assertThat(transferMoneyResponse, transactionDaoOne).match();
    }

    @Test
    @UserSession(type = "API")
    @APIVersion("with_database_with_fix")
    public void AuthUserCannotTransferMoneyMoreThenExistTest() {
        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.get().getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.get().getId())
                        .amount(depositMoneyResponse.getBalance() + 1)
                        .receiverAccountId(ownAccountTwo.get().getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountOne.get().getId(), expectedTransaction);

        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDaoOne).isNull();
    }

        public static Stream<Arguments> transferInvalidDataV1() {
        return Stream.of(
                Arguments.of(0.009, "Transfer amount must be at least 0.01"),
                Arguments.of(-10000, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidDataV1")
    @ParameterizedTest
    @UserSession(type = "API")
    @APIVersion("with_validation_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV1(double amount, String errorValue) {
        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.get().getId())
                        .amount(amount)
                        .receiverAccountId(ownAccountTwo.get().getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(transferMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransaction);


        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDaoOne).isNull();
    }

    public static Stream<Arguments> transferInvalidDataV2() {
        return Stream.of(
                Arguments.of(0.009, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(-10000, "Invalid transfer: insufficient funds or invalid accounts"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidDataV2")
    @ParameterizedTest
    @UserSession(type = "API")
    @APIVersion("with_database_with_fix")
    public void userCanDepositInvalidMoneyToHisOwnAccountTestV2(double amount, String errorValue) {
        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.get().getId())
                        .amount(amount)
                        .receiverAccountId(ownAccountTwo.get().getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(transferMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.get().getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.get().getId(), expectedTransaction);


        assertThat(transactionsAccountOne.size()).isZero();

        TransactionDao transactionDaoOne = DataBaseSteps.getTransactionByTransaction(expectedTransaction);
        assertThat(transactionDaoOne).isNull();
    }

    @AfterEach
    void tearDown() {
        ownAccountOne.remove();
        ownAccountTwo.remove();
        otherAccountOne.remove();
    }
}
