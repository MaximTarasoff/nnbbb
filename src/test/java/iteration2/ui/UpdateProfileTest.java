package iteration2.ui;

import api.generators.RandomModelGenerator;
import api.models.customer.profile.ReadProfileResponse;
import api.models.customer.profile.UpdateProfileRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.EditProfile;
import ui.pages.UserDashboard;
import ui.pages.enums.ProfileAlert;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UpdateProfileTest extends BaseUiTest {
    private static UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    public void setup() {
        updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);

        new EditProfile()
                .open()
                .getTitleText()
                .shouldBe(Condition.visible);
    }

    @Test
    @UserSession
    public void AuthUserCanUpdateProfileNameTest() {
        new EditProfile().enterName(updateProfileRequest.getName())
                .saveChanges()
                .checkAlertMessageAndAccept(ProfileAlert.NAME_UPDATE_SUCCESSFULLY.getMessage())
                .clickHomeButton()
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + updateProfileRequest.getName() + "!"));

        ReadProfileResponse readCustomerResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readCustomerResponse.getName()).isEqualTo(updateProfileRequest.getName());
    }

    @Test
    @UserSession
    public void AuthUserCanUpdateProfileWitSameNameTest() {
        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .update(updateProfileRequest);

        new EditProfile().enterName(updateProfileRequest.getName())
                .saveChanges()
                .checkAlertMessageAndAccept(ProfileAlert.NAME_CANNOT_BE_SAME.getMessage())
                .clickHomeButton()
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + updateProfileRequest.getName() + "!"));

        ReadProfileResponse readCustomerResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readCustomerResponse.getName()).isEqualTo(updateProfileRequest.getName());
    }

    public static Stream<Arguments> nameInvalidData() {
        return Stream.of(
                Arguments.of("Rope Block Tra", "Name must contain two words with letters only"),
                Arguments.of("John", "Name must contain two words with letters only")
        );
    }

    @MethodSource("nameInvalidData")
    @ParameterizedTest
    @UserSession
    public void AuthUserCannotUpdateProfileWithInvalidNameTest(String name, String errorValue) {
        new EditProfile().enterName(name)
                .saveChanges()
                .checkAlertMessageAndAccept(errorValue)
                .clickHomeButton()
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));

        ReadProfileResponse readProfileResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readProfileResponse.getName()).isNotEqualTo(name);
    }

    @AfterEach
    public void tearDown() {
        SessionStorage.clear();
    }
}
