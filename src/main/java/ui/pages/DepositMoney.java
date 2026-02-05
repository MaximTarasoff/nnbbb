package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.elements.DepositBage;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoney extends BasePage<DepositMoney> {
    private final SelenideElement depositMoneyTitleText = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private final SelenideElement selectAccountButton = $(Selectors.byClassName("account-selector"));
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement depositButton = $(Selectors.byCssSelector("button.btn-primary"));
    private final SelenideElement accountsList = $(Selectors.byClassName("account-selector"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoney selectAccountInListByName(String accountName) {
        accountsList.findAll("option").findBy(text(accountName)).click();
        accountsList.shouldHave(text(accountName));
        return this;
    }

    public DepositMoney enterAmount(double amountValue) {
        amountInput.sendKeys(String.valueOf(amountValue));
        amountInput.shouldHave(value(String.valueOf(amountValue)));
        return this;
    }

    public DepositMoney depositMoney() {
        depositButton.click();
        return this;
    }

    public List<DepositBage> getAllAccounts() {
        ElementsCollection elementsCollection = $(Selectors.byClassName("account-selector")).findAll("option");
        return generatePageElements(elementsCollection, DepositBage::new);
    }
}
