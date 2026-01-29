package api.models.accounts;

import api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data //@ToString , @EqualsAndHashCode, @Getter, @Setter, etc
@AllArgsConstructor // конструктор для всех элементов
@NoArgsConstructor // конструктор без элементов
@Builder //паттерн билдер
public class CreateAccountResponse extends BaseModel {
    private long id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
}
