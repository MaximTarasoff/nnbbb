package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //@ToString , @EqualsAndHashCode, @Getter, @Setter, etc
@AllArgsConstructor // конструктор для всех элементов
@NoArgsConstructor // конструктор без элементов
@Builder //паттерн билдер
public class CreateUserRequest extends BaseModel{
    private String username;
    private String password;
    private UserRole role;
}
