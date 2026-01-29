package api.models.customer.profile;

import api.generators.GeneratingRule;
import lombok.*;
import api.models.BaseModel;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{5} [A-Za-z]{5}$")
    private String name;
}
