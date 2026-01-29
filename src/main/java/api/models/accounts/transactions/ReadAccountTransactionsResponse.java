package api.models.accounts.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;
import models.BaseModel;
import api.models.accounts.Transaction;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadAccountTransactionsResponse extends BaseModel {
    private List<Transaction> transactions;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ReadAccountTransactionsResponse fromArray(List<Transaction> array) {
        ReadAccountTransactionsResponse wrapper = new ReadAccountTransactionsResponse();
        wrapper.setTransactions(array);
        return wrapper;
    }
}
