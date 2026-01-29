package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.actions;

@Getter
public class EditProfile extends BasePage<EditProfile> {
    private final SelenideElement titleText = $(Selectors.byText("✏\uFE0F Edit Profile"));
    private final SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement saveChangesButton = $(Selectors.byCssSelector("button.btn-primary"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile enterName(String name) {
        nameInput.shouldBe(Condition.enabled, Condition.clickable);
        actions().moveToElement(nameInput).click().doubleClick().sendKeys(Keys.DELETE).perform();
        actions().moveToElement(nameInput).click().sendKeys(name).perform();
        nameInput.shouldBe(value(name));
        return this;
    }

    public EditProfile saveChanges() {
        saveChangesButton.click();
        return this;
    }
}
