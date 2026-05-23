package iteration1.ui;

import api.configs.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.extensions.*;
import io.qameta.allure.selenide.AllureSelenide;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@ExtendWith(APIVersionExtensions.class)
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = Config.getProperty("ui.remote");
        Configuration.baseUrl = Config.getProperty("ui.baseUrl");
        Configuration.browser = Config.getProperty("ui.browser");
        Configuration.browserSize = Config.getProperty("ui.browserSize");
        Configuration.timeout = Long.parseLong(Config.getProperty("ui.timeout"));
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
        Configuration.headless = false;
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableVideo", false,
                        "enableLog", true
                )
        );
    }
}
