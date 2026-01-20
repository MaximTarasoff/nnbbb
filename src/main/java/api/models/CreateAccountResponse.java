package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data //@ToString , @EqualsAndHashCode, @Getter, @Setter, etc
@AllArgsConstructor // конструктор для всех элементов
@NoArgsConstructor // конструктор без элементов
@Builder //паттерн билдер
public class CreateAccountResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<String> transactions;
}
