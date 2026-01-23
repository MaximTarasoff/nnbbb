package requests.steps;

import generators.RandomModelGenerator;
import models.accounts.CreateAccountResponse;
import models.accounts.Transaction;
import models.accounts.deposit.DepositMoneyRequest;
import models.accounts.deposit.DepositMoneyResponse;
import models.accounts.transactions.ReadAccountTransactionsResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class UserSteps {

    public static CreateAccountResponse createAccount(String username, String password) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }

    public static DepositMoneyResponse depositAccount(String username, String password, long id) {
        DepositMoneyRequest request = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequest.class);
        request.setId(id);

        return new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }

    public static List<Transaction> getAllAccountTransactions(String username, String password, long accountId){
        return new ValidatedCrudRequester<ReadAccountTransactionsResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).get(accountId).getTransactions();
    }
}
