package iteration2;

import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.accounts.CreateAccountResponse;
import models.CreateUserRequest;
import models.accounts.Transaction;
import models.accounts.deposit.DepositMoneyRequest;
import models.accounts.deposit.DepositMoneyResponse;
import models.accounts.transactions.ReadAccountTransactionsResponse;
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

public class DepositAccountTest extends BaseTest {
    private static CreateUserRequest createUserRequestOwn;
    private static CreateUserRequest createUserRequestOther;
    private static CreateAccountResponse ownAccount;

    @BeforeAll
    public static void setUpBeforeClass() {
        createUserRequestOwn = AdminSteps.createUser();
        ownAccount = UserSteps.createAccount(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword());
        createUserRequestOther = AdminSteps.createUser();
    }

    @Test
    public void userCanDepositMoneyToHisOwnAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(ownAccount.getId());

        DepositMoneyResponse depositMoneyResponse = new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositMoneyRequest);

        ModelAssertions.assertThatModels(depositMoneyRequest, depositMoneyResponse).match();

        ReadAccountTransactionsResponse transactionsResponse = new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(ownAccount.getId());

        ModelAssertions.assertThatModels(depositMoneyRequest, transactionsResponse).match();

        List<Transaction> transactions = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccount.getId())
                .stream().filter(transaction -> transaction.getType().contains("DEPOSIT") &&
                        transaction.getAmount() == depositMoneyResponse.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccount.getId())
                .toList();

        assertThat(transactions.size()).isEqualTo(1);
    }

    @Test
    public void userCannotDepositMoneyToNotHisAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(ownAccount.getId());

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequestOther.getUsername(), createUserRequestOther.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsForbiddenRequest())
                .post(depositMoneyRequest);

        List<Transaction> transactions = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccount.getId()).stream().filter(transaction -> transaction.getType().contains("DEPOSIT") &&
                        transaction.getAmount() == depositMoneyRequest.getBalance() &&
                        transaction.getRelatedAccountId() == ownAccount.getId())
                .toList();

        assertThat(transactions.size()).isZero();
    }

    @Test
    public void userCannotDepositMoneyToNotExistedAccountTest() {
        DepositMoneyRequest depositMoneyRequest = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        depositMoneyRequest.setId(-1);

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
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
    public void userCanDepositInvalidMoneyToHisOwnAccountTest(double balance, String errorValue) {
        DepositMoneyRequest depositMoneyRequest = DepositMoneyRequest.builder()
                .id(ownAccount.getId())
                .balance(balance)
                .build();

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequestOwn.getUsername(), createUserRequestOwn.getPassword()),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .post(depositMoneyRequest);

        List<Transaction> transactions = UserSteps.getAllAccountTransactions(createUserRequestOwn.getUsername(),
                        createUserRequestOwn.getPassword(), ownAccount.getId())
                .stream().filter(transaction -> transaction.getType().contains("DEPOSIT") &&
                        transaction.getAmount() == balance &&
                        transaction.getRelatedAccountId() == ownAccount.getId())
                .toList();

        assertThat(transactions.size()).isZero();
    }
}
