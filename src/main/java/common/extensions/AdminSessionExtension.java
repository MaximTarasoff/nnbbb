package common.extensions;

import api.models.CreateUserRequest;
import common.annotations.AdminSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

public class AdminSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // шаг 1: проверка есть ли у теста аннотация AdminSession
        AdminSession adminSession = extensionContext.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if (adminSession != null) { //если есть добавляем в local storage токен админа
            BasePage.authAsUser(CreateUserRequest.getAdmin());
        }
        //
    }
}
