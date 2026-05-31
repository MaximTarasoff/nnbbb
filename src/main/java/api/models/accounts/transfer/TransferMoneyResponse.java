package api.models.accounts.transfer;

import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel {
    private String status;
    private String message;
    private Long transactionId;
    private Long senderAccountId;
    private Long receiverAccountId;
    private double amount;
    private double fraudRiskScore;
    private String fraudReason;
    private boolean requiresVerification;
    private boolean requiresManualReview;
}
