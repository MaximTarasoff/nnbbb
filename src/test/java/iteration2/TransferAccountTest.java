package iteration2;

import iteration1.BaseTest;
import models.CreateUserRequest;
import models.accounts.CreateAccountResponse;
import models.accounts.Transaction;
import models.accounts.deposit.DepositMoneyResponse;
import models.accounts.transactions.ReadAccountTransactionsResponse;
import models.accounts.transfer.TransferMoneyRequest;
import models.accounts.transfer.TransferMoneyResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TransferAccountTest extends BaseTest {
    private static CreateUserRequest createUserRequestOwn;
    private static CreateUserRequest createUserRequestOther;
    private static  CreateAccountResponse ownAccountOne;
    private static CreateAccountResponse ownAccountTwo;
    private static CreateAccountResponse otherAccountOne;

    @BeforeAll
    public static void setUpBeforeClass() {
        createUserRequestOwn = AdminSteps.createUser();
        ownAccountOne = UserSteps.createAccount(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword());
        ownAccountTwo = UserSteps.createAccount(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword());

        createUserRequestOther = AdminSteps.createUser();
        otherAccountOne = UserSteps.createAccount(createUserRequestOther.getUsername(), createUserRequestOther.getPassword());
    }

    @Test
    public void AuthUserCanTransferMoneyBetweenHisOwnAccountsTest() {
        DepositMoneyResponse depositMoneyResponse = UserSteps.depositAccount(
                createUserRequestOwn.getUsername(),
                createUserRequestOwn.getPassword(),
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(ownAccountOne.getId())
                .amount(depositMoneyResponse.getBalance())
                .receiverAccountId(ownAccountTwo.getId())
                .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest);

        ModelAssertions.assertThatModels(transferMoneyRequest, transferMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(ownAccountTwo.getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        List<Transaction> transactionsAccountOne = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_OUT") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccountTwo.getId())
                .toList();

        assertThat(transactionsAccountOne.size()).isEqualTo(1);

        List<Transaction> transactionsAccountTwo = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccountTwo.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_IN") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccountOne.getId())
                .toList();

        assertThat(transactionsAccountTwo.size()).isEqualTo(1);

    }

    @Test
    public void AuthUserCanTransferMoneyToNotHisAccountTest() {
        DepositMoneyResponse depositMoneyResponse = UserSteps.depositAccount(
                createUserRequestOwn.getUsername(),
                createUserRequestOwn.getPassword(),
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(depositMoneyResponse.getBalance())
                        .receiverAccountId(otherAccountOne.getId())
                        .build();

        TransferMoneyResponse transferMoneyResponse = new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transferMoneyRequest);

        ModelAssertions.assertThatModels(transferMoneyRequest, transferMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(createUserRequestOther.getUsername(), createUserRequestOther.getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(otherAccountOne.getId());

        ModelAssertions.assertThatModels(transactionsResponse, transferMoneyRequest).match();

        List<Transaction> transactionsAccountOne = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER_OUT") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == otherAccountOne.getId())
                .toList();

        assertThat(transactionsAccountOne.size()).isEqualTo(1);
    }

    @Test
    public void AuthUserCannotTransferMoneyMoreThenExistTest() {
        DepositMoneyResponse depositMoneyResponse = UserSteps.depositAccount(
                createUserRequestOwn.getUsername(),
                createUserRequestOwn.getPassword(),
                ownAccountOne.getId()
        );

        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(depositMoneyResponse.getBalance() + 1)
                        .receiverAccountId(ownAccountTwo.getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferMoneyRequest);

        List<Transaction> transactionsAccountOne = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                createUserRequestOwn.getPassword(), ownAccountOne.getId())
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
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double amount, String errorValue) {
        TransferMoneyRequest transferMoneyRequest =
                TransferMoneyRequest.builder()
                        .senderAccountId(ownAccountOne.getId())
                        .amount(amount)
                        .receiverAccountId(ownAccountTwo.getId())
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSFER,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(transferMoneyRequest);

        List<Transaction> transactionsAccountOne = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccountOne.getId())
                .stream().filter(transaction -> transaction.getType().contains("TRANSFER") &&
                        transaction.getAmount() == amount &&
                        transaction.getRelatedAccountId() == ownAccountTwo.getId())
                .toList();

        assertThat(transactionsAccountOne.size()).isZero();
    }
}
