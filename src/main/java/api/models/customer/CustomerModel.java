package api.models.customer;

import api.models.accounts.AccountResponse;
import lombok.*;
import api.models.BaseModel;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerModel extends BaseModel {
    private String id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<AccountResponse> accounts;
}
