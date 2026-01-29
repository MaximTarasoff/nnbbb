package api.models.customer.profile;

import lombok.*;
import models.BaseModel;
import api.models.customer.CustomerModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileResponse extends BaseModel {
    private CustomerModel customer;
    private String message;
}
