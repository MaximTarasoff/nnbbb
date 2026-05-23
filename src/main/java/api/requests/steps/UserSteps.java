package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyRequest;
import api.models.accounts.deposit.DepositMoneyResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.helpers.StepLogger;

import java.util.List;

public class UserSteps {
    private final String username;
    private final String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return StepLogger.log("User " + username + " get all accounts", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpecs.requestReturnsOK()
            ).getAll(CreateAccountResponse[].class);
        });
    }

    public CreateAccountResponse createAccount() {
        return StepLogger.log("User " + username + " create account", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.ACCOUNTS,
                    ResponseSpecs.entityWasCreated())
                    .post(null);
        });
    }

    public DepositMoneyResponse depositAccountById(long id) {
        DepositMoneyRequest request = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        request.setId(id);

        return StepLogger.log("User " + username + " deposit " + request.getBalance() + " to account with id: " + request.getId(), () -> {
            return new ValidatedCrudRequester<DepositMoneyResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.ACCOUNTS_DEPOSIT,
                    ResponseSpecs.requestReturnsOK())
                    .post(request);
        });
    }

    public List<Transaction> getAllTransactionsByAccountId(long accountId) {
        return StepLogger.log("Get all transactions by account id: " + accountId, () -> {
            return new ValidatedCrudRequester<Transaction>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.ACCOUNTS_TRANSACTIONS,
                    ResponseSpecs.requestReturnsOK()
            ).getAll(Transaction[].class, accountId);
        });
    }

    public List<Transaction> getAccountTransactionsByParams(long accountId, Transaction expectedTransaction) {
        return StepLogger.log("Get all transactions by account id=" + accountId + " and expected transaction id=" + expectedTransaction.getId(), () -> {
            return getAllTransactionsByAccountId(accountId)
                    .stream()
                    .filter(t -> t.matches(expectedTransaction))
                    .toList();
        });
    }
}
