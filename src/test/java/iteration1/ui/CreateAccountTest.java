package iteration1.ui;

import api.models.accounts.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.enums.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        new UserDashboard().open().createNewAccount();

        List<CreateAccountResponse> createdAccounts = SessionStorage.getUserSteps().getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard()
                .checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + " " + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
