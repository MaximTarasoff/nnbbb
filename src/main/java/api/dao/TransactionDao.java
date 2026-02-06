package api.dao;

import api.models.accounts.transactions.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDao {
    private Long id;
    private Double amount;
    private TransactionType type;
    private String timestamp;
    private Long accountId;
    private Long relatedAccountId;
}
