package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse extends BaseModel {
    private String username;
    private String role;
}
