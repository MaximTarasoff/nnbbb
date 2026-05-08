package api.models.accounts.transactions;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private long id;
    private Double amount;
    private TransactionType type;
    private String timestamp;
    private String timestampAsString;
    private long relatedAccountId;
    private Long amountAsDouble;

    public boolean matches(Transaction expected) {
        if (expected == null) {
            throw new IllegalArgumentException("Был передан пустой объект");
        }

        if (expected.id != 0 && !(expected.id == this.id)) return false;
        if (expected.type != null && expected.type != this.type) return false;
        if (Math.abs(this.amount - expected.amount) > 0.01) {
            System.out.println("expected.amount " + expected.amount + " != " + this.amount);
            return false;
        }
        if (expected.relatedAccountId != 0
                && expected.relatedAccountId != this.relatedAccountId) return false;
        return expected.timestamp == null || expected.timestamp.equals(this.timestamp);
    }

}
