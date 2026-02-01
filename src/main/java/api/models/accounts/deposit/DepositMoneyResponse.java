package api.models.accounts.deposit;

import lombok.*;
import api.models.BaseModel;
import api.models.accounts.transactions.Transaction;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
}
