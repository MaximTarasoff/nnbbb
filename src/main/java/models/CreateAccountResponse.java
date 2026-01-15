package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data //@ToString , @EqualsAndHashCode, @Getter, @Setter, etc
@AllArgsConstructor // конструктор для всех элементов
@NoArgsConstructor // конструктор без элементов
@Builder //паттерн билдер
public class CreateAccountResponse extends BaseModel {
    private int id;
    private String accountNumber;
    private BigDecimal balance;
    private List<String> transactions;
}
