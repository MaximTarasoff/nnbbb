package iteration2.api;

import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.TransactionType;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyResponse;
import api.models.accounts.transactions.ReadAccountTransactionsResponse;
import api.models.accounts.transfer.TransferMoneyRequest;
import api.models.accounts.transfer.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
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
    private static  CreateAccountResponse ownAccountOne;
    private static CreateAccountResponse ownAccountTwo;
    private static CreateAccountResponse otherAccountOne;

    @BeforeEach
    public void setUpBeforeClass() {
        ownAccountOne = SessionStorage.getUserSteps(1).createAccount();
        ownAccountTwo = SessionStorage.getUserSteps(1).createAccount();
    }

    @Test
    @UserSession(type = "API")
    public void AuthUserCanTransferMoneyBetweenHisOwnAccountsTest() {
        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(ownAccountOne.getId())
                .amount(depositMoneyResponse.getBalance())
                .receiverAccountId(ownAccountTwo.getId())
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
        ).get(ownAccountTwo.getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        Transaction expectedTransactionTwo = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_IN)
                .relatedAccountId(ownAccountOne.getId())
                .build();

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountTwo.getId(), expectedTransactionTwo);

        assertThat(transactionsAccountTwo.size()).isEqualTo(1);

    }

    @Test
    @UserSession(type = "API", value = 2)
    public void AuthUserCanTransferMoneyToNotHisAccountTest() {
        otherAccountOne = SessionStorage.getUserSteps(2).createAccount();

        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(depositMoneyResponse.getBalance())
                        .receiverAccountId(otherAccountOne.getId())
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
        ).get(otherAccountOne.getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        Transaction expectedTransaction = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(otherAccountOne.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps(1)
                .getAccountTransactionsByParams(
                        ownAccountOne.getId(),expectedTransaction);

        assertThat(transactionsAccountOne.size()).isEqualTo(1);
    }

    @Test
    @UserSession(type = "API")
    public void AuthUserCannotTransferMoneyMoreThenExistTest() {
        DepositMoneyResponse depositMoneyResponse = SessionStorage.getUserSteps().depositAccountById(
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(depositMoneyResponse.getBalance() + 1)
                        .receiverAccountId(ownAccountTwo.getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferMoneyRequest);

        Transaction expectedTransactionOne = Transaction.builder()
                .amount(depositMoneyResponse.getBalance())
                .type(TransactionType.TRANSFER_OUT)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(
                        ownAccountOne.getId(), expectedTransactionOne);

        assertThat(transactionsAccountOne.size()).isZero();
    }

    public static Stream<Arguments> transferInvalidData() {
        return Stream.of(
                Arguments.of(0.009, "Transfer amount must be at least 0.01"),
                Arguments.of(-10000, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferInvalidData")
    @ParameterizedTest
    @UserSession(type = "API")
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double amount, String errorValue) {
        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(amount)
                        .receiverAccountId(ownAccountTwo.getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(transferMoneyRequest);

        Transaction expectedTransaction = Transaction.builder()
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .relatedAccountId(ownAccountTwo.getId())
                .build();

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps()
                .getAccountTransactionsByParams(ownAccountOne.getId(), expectedTransaction);


        assertThat(transactionsAccountOne.size()).isZero();
    }
}
