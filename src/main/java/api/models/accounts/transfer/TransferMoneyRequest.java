package api.models.accounts.transfer;

import generators.GeneratingRule;
import lombok.*;
import models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyRequest extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    @GeneratingRule(min = 0.01, max = 10000.0)
    private double amount;
}
