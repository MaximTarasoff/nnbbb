package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private final SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private final SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private final SelenideElement depositMoney = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private final SelenideElement transferMoney = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }
}
