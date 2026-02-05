package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.*;

@Getter
public class EditProfile extends BasePage<EditProfile> {
    private final SelenideElement titleText = $(Selectors.byText("✏️ Edit Profile"));
    private final SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private final SelenideElement saveChangesButton = $(Selectors.byCssSelector("button.btn-primary"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile enterName(String name) {
        nameInput.shouldBe(Condition.visible, Condition.enabled);

        RetryUtils.retry(
                () -> {
                    nameInput.clear();
                    nameInput.sendKeys(name);
                    nameInput.pressTab();
                    return nameInput.getValue();
                },
                name::equals,
                5,
                1000
        );
        return this;
    }

    public EditProfile saveChanges() {
        saveChangesButton.shouldBe(Condition.visible, Condition.enabled);
        saveChangesButton.click();
        return this;
    }
}
