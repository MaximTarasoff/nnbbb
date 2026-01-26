package models.customer;

import lombok.*;
import models.BaseModel;

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
    private List<String> accounts;
}
