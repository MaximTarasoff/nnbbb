package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
}
