package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class DepositBage extends BaseElement {
    private String accountName;
    private String balance;

    public DepositBage(SelenideElement element) {
        super(element);
        String fullText = element.getText();

        accountName = fullText.split("\\(")[0].trim();

        String[] parts = fullText.split("\\$");
        balance = parts.length > 1 ? parts[1].replaceAll("[^\\d.]", "") : "";
    }
}
