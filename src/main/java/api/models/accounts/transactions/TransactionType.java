package api.models.accounts.transactions;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TransactionType {

    TRANSFER_IN("TRANSFER_IN"),
    TRANSFER_OUT("TRANSFER_OUT"),
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAW"),
    TRANSFER("TRANSFER");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public static TransactionType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown transaction type: " + value)
                );
    }
}
