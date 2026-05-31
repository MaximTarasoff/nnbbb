package api.models.fraud;

import api.models.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyRequestV1 extends BaseModel {
    private Long accountId;
    private double amount;
    private String description;
}
