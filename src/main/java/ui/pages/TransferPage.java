package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class TransferPage extends BasePage<TransferPage> {
    private final SelenideElement transferTitleText = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));
    private final ElementsCollection accountsList = $(Selectors.byClassName("account-selector")).findAll("option");
    private final SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private final SelenideElement recipientAccountNumbertInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private final SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement confirmCheckBox = $(Selectors.byId("confirmCheck"));
    private final SelenideElement sendTransferButton = $(Selectors.byCssSelector("button.btn-primary"));

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage selectAccountInListByName(String accountName) {
        accountsList.findBy(text(accountName)).click();
        return this;
    }
    public TransferPage enterRecipientName(String recipientName) {
        recipientNameInput.sendKeys(recipientName);
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        recipientAccountNumbertInput.sendKeys(accountNumber);
        return this;
    }

    public TransferPage enterAmount(double amount) {
        amountInput.sendKeys(String.valueOf(amount));
        return this;
    }

    public TransferPage setCheckBox() {
        if (!confirmCheckBox.isSelected()) {
            confirmCheckBox.click();
        }
        return this;
    }

    public TransferPage sendTransfer() {
        sendTransferButton.click();
        return this;
    }
}
