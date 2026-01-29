package api.models.customer.profile;

import lombok.*;
import models.BaseModel;
import api.models.accounts.CreateAccountResponse;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadProfileResponse extends BaseModel {
    private String id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<CreateAccountResponse> accounts;
}
