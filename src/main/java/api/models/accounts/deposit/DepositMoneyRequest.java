package api.models.accounts.deposit;

import api.generators.GeneratingRule;
import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyRequest extends BaseModel {
    private long id;
    @GeneratingRule(min = 0.01, max = 5000.0)
    private double balance;
}
