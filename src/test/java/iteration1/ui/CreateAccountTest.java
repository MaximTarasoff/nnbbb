package iteration1.ui;

import api.dao.AccountDao;
import api.models.accounts.CreateAccountResponse;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import common.utils.RetryUtils;
import org.junit.jupiter.api.Test;
import ui.pages.enums.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        UserSteps user = SessionStorage.getUserSteps();

        new UserDashboard().open().createNewAccount();

        RetryUtils.retry(
                user::getAllAccounts,
                accounts -> accounts.size() == 1,
                20,
                200L
        );

        List<CreateAccountResponse> createdAccounts = user.getAllAccounts();

        new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage() + " " + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccounts.getFirst().getAccountNumber());
        assertThat(accountDao).isNotNull();
    }
}
