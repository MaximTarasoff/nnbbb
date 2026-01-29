package iteration2.api;

import api.models.accounts.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import api.models.accounts.Transaction;
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

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_OUT") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccountTwo.getId())
                .toList();

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        List<Transaction> transactionsAccountTwo = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccountTwo.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_IN") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccountOne.getId())
                .toList();

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

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_OUT") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == otherAccountOne.getId())
                .toList();

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

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccountTwo.getId())
                .toList();

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

        List<Transaction> transactionsAccountOne = SessionStorage.getUserSteps().getAllAccountTransactions(ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER") &&
                        transaction.getAmount() == amount &&
                        transaction.getRelatedAccountId() == ownAccountTwo.getId())
                .toList();

        assertThat(transactionsAccountOne.size()).isZero();
    }
}
