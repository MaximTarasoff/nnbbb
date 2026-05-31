package api.enums.bank;

import lombok.Getter;

@Getter
public enum BankMessage {
    DEPOSIT_AMOUNT_MUST_BE_MORE_ZERO_POINT_ZERO_ONE("Deposit amount must be at least 0.01"),
    DEPOSIT_AMOUNT_CANNOT_EXCEED_FIVE_THOUSAND("Deposit amount cannot exceed 5000"),
    INVALID_ACCOUNT_OR_AMOUNT("Invalid account or amount"),
    DEPOSIT_AMOUNT_EXCEEDS_THE_FIVE_THOUSAND_LIMIT("Deposit amount exceeds the 5000 limit"),
    TRANSFER_AMOUNT_MUST_BE_AT_LEAST_ZERO_POINT_ZERO_ONE("Transfer amount must be at least 0.01"),
    INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS("Invalid transfer: insufficient funds or invalid accounts"),
    TRANSFER_AMOUNT_CANNOT_EXCEED_TEN_THOUSAND("Transfer amount cannot exceed 10000");

    private final String message;

    BankMessage(String message) {
        this.message = message;
    }
}
