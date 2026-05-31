package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.transactions.Transaction;
import api.models.accounts.deposit.DepositMoneyRequestV2;
import api.models.accounts.deposit.DepositMoneyResponseV2;
import api.models.accounts.transfer.TransferMoneyRequest;
import api.models.accounts.transfer.TransferMoneyResponse;
import api.models.fraud.DepositMoneyRequestV1;
import api.models.fraud.DepositMoneyResponseV1;
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

    public DepositMoneyResponseV1 depositToAccountFraud(Long accountId, double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () -> {
            DepositMoneyRequestV1 depositMoneyRequestV1 = DepositMoneyRequestV1.builder()
                    .accountId(accountId)
                    .amount(amount)
                    .description("Test deposit")
                    .build();

            return new ValidatedCrudRequester<DepositMoneyResponseV1>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.ACCOUNT_DEPOSIT_V1,
                    ResponseSpecs.requestReturnsOK()).post(depositMoneyRequestV1);
        });
    }

    public DepositMoneyResponseV2 depositRandomBalanceToAccount(long id) {
        DepositMoneyRequestV2 request = RandomModelGenerator.generateAnnotatedFieldsOnly(DepositMoneyRequestV2.class);
        request.setId(id);

        return new ValidatedCrudRequester<DepositMoneyResponseV2>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }

    public DepositMoneyResponseV2 depositAccountWith(long id, double money) {
        DepositMoneyRequestV2 request = DepositMoneyRequestV2.builder()
                .id(id)
                .balance(money)
                .build();

        return new ValidatedCrudRequester<DepositMoneyResponseV2>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_DEPOSIT_V2,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }

    public List<Transaction> getAllTransactionsByAccountId(long accountId) {
        return new ValidatedCrudRequester<Transaction>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS_TRANSACTIONS,
                ResponseSpecs.requestReturnsOK()
        ).getAll(Transaction[].class, accountId);
    }

    public List<Transaction> getAccountTransactionsByParams(long accountId, Transaction expectedTransaction) {
        return getAllTransactionsByAccountId(accountId)
                .stream()
                .filter(t -> t.matches(expectedTransaction))
                .toList();
    }

    public TransferMoneyResponse transferWithFraudCheck(Long senderAccountId, Long receiverAccountId, double amount) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .description("Test transfer with fraud check")
                    .build();

            return new ValidatedCrudRequester<TransferMoneyResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                    ResponseSpecs.requestReturnsOK()).post(transferRequest);
        });
    }

    public TransferMoneyResponse transferWithFraudCheckReturnsBadRequest(Long senderAccountId, Long receiverAccountId, double amount, String errorMsg) {
        return StepLogger.log("User " + username + " transfers " + amount + " to " + receiverAccountId + " with fraud check", () -> {
            TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                    .senderAccountId(senderAccountId)
                    .receiverAccountId(receiverAccountId)
                    .amount(amount)
                    .description("Test transfer with fraud check")
                    .build();

            return new ValidatedCrudRequester<TransferMoneyResponse>(
                    RequestSpecs.authAsUser(username, password),
                    Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                    ResponseSpecs.requestReturnsBadRequest(errorMsg)).post(transferRequest);
        });
    }
}
