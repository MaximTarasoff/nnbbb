package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.Transaction;
import api.models.accounts.deposit.DepositMoneyRequest;
import api.models.accounts.deposit.DepositMoneyResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private final String username;
    private final String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()
        ).getAll(CreateAccountResponse[].class);
    }

    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }

    public DepositMoneyResponse depositAccountById(long id) {
        DepositMoneyRequest request = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        request.setId(id);

        return new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }

    public List<Transaction> getAllAccountTransactions(long accountId){
        return new ValidatedCrudRequester<Transaction>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).getAll(Transaction[].class, accountId);
    }
}
