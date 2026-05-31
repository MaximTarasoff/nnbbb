package api.requests.skelethon;


import api.models.*;
import api.models.accounts.CreateAccountResponse;
import api.models.accounts.deposit.DepositMoneyRequestV2;
import api.models.accounts.deposit.DepositMoneyResponseV2;
import api.models.accounts.transactions.ReadAccountTransactionsResponse;
import api.models.accounts.transfer.TransferMoneyRequest;
import api.models.accounts.transfer.TransferMoneyResponse;
import api.models.customer.profile.ReadProfileResponse;
import api.models.customer.profile.UpdateProfileRequest;
import api.models.customer.profile.UpdateProfileResponse;
import api.models.fraud.DepositMoneyRequestV1;
import api.models.fraud.DepositMoneyResponseV1;
import api.models.fraud.FraudCheckResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER("/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    LOGIN("/auth/login",
            LoginRequest.class,
            LoginResponse.class
    ),
    ACCOUNTS("/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    CUSTOMER_ACCOUNTS("/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    ACCOUNT_DEPOSIT_V1(
            "/accounts/deposit",
            DepositMoneyRequestV1.class,
            DepositMoneyResponseV1.class
    ),

    ACCOUNTS_DEPOSIT_V2("/accounts/deposit",
            DepositMoneyRequestV2.class,
            DepositMoneyResponseV2.class
    ),
    ACCOUNTS_TRANSFER("/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class
    ),
    ACCOUNTS_TRANSACTIONS("/accounts/%s/transactions",
            BaseModel.class,
            ReadAccountTransactionsResponse.class
    ),
    CUSTOMER_PROFILE("/customer/profile",
            UpdateProfileRequest.class,
            UpdateProfileResponse.class
    ),
    CUSTOMER_PROFILE_GET("/customer/profile",
            BaseModel.class,
            ReadProfileResponse.class),

    TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/api/v1/accounts/fraud-check/{transactionId}",
            BaseModel.class,
            FraudCheckResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;

    public String getUrl(long id) {
        if (!url.contains("%s")) {
            throw new IllegalStateException(
                    String.format("URL '%s' does not contain a placeholder for id. Use getUrl() instead.", url)
            );
        }
        return String.format(url, id);
    }
}
