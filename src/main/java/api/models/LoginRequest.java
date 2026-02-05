package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest extends BaseModel {
    private String username;
    private String password;
}
