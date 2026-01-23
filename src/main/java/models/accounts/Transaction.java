package models.accounts;

import lombok.*;
import models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private long id;
    private double amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;
}
