package models.accounts.transfer;

import lombok.*;
import models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
    private String message;
}
