package iteration1.ui;

import api.configs.Config;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("ui.remote");
        Configuration.baseUrl = Config.getProperty("ui.baseUrl");
        Configuration.browser = Config.getProperty("ui.browser");
        Configuration.browserSize = Config.getProperty("ui.browserSize");

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableVideo", false,
                        "enableLog", true
                )
        );
    }


    public void authAsUser(String username, String password) {
        Selenide.open("/");
//        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
        //OR
//        С localStorage можно так же работать через Selenide
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        Selenide.localStorage().setItem("authToken", userAuthHeader);
    }

    public void authAsUser(CreateUserRequest createdUser) {
        authAsUser(createdUser.getUsername(), createdUser.getPassword());
    }
}
