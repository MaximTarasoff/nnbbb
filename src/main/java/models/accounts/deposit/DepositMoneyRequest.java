package models.accounts.deposit;

import generators.GeneratingRule;
import lombok.*;
import models.BaseModel;

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
