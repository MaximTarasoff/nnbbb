package api.enums.mocks;

import lombok.Getter;

@Getter
public enum FraudMessage {
    INSUFFICIENT_FUNDS("Insufficient funds"),
    INVALID_ACCOUNTS_OR_AMOUNT("Invalid accounts or amount"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_TEN_THOUSAND("Transfer amount cannot exceed 10000");

    private final String message;

    FraudMessage(String message) {
        this.message = message;
    }
}
