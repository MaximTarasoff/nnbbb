package iteration2.ui;

import api.dao.UserDao;
import api.generators.RandomModelGenerator;
import api.models.customer.profile.ReadProfileResponse;
import api.models.customer.profile.UpdateProfileRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.DataBaseSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import com.codeborne.selenide.Condition;
import common.annotations.APIVersion;
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
    private static final ThreadLocal<UpdateProfileRequest> updateProfileRequest = new ThreadLocal<>();

    @BeforeEach
    public void setup() {
        updateProfileRequest.set(RandomModelGenerator.generate(UpdateProfileRequest.class));

        new EditProfile()
                .open()
                .getTitleText()
                .shouldBe(Condition.visible);
    }

    @Test
    @UserSession
    @APIVersion("with_database_with_fix")
    public void AuthUserCanUpdateProfileNameTest() {
        new EditProfile().enterName(updateProfileRequest.get().getName())
                .saveChanges()
                .checkAlertMessageAndAccept(ProfileAlert.NAME_UPDATE_SUCCESSFULLY.getMessage())
                .clickHomeButton()
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + updateProfileRequest.get().getName() + "!"));

        ReadProfileResponse readCustomerResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readCustomerResponse.getName()).isEqualTo(updateProfileRequest.get().getName());

        UserDao userDao = DataBaseSteps.getUserByUsername(SessionStorage.getUser().getUsername());
        assertThat(userDao.getName()).isEqualTo(updateProfileRequest.get().getName());
    }

    @Test
    @UserSession
    @APIVersion("with_database_with_fix")
    public void AuthUserCannotUpdateProfileWithSameNameTest() {
        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .update(updateProfileRequest.get());

        new EditProfile().enterName(updateProfileRequest.get().getName())
                .saveChanges()
                .checkAlertMessageAndAccept(ProfileAlert.NAME_CANNOT_BE_SAME.getMessage())
                .clickHomeButton()
                .getPage(UserDashboard.class)
                .getWelcomeText()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + updateProfileRequest.get().getName() + "!"));

        ReadProfileResponse readCustomerResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readCustomerResponse.getName()).isEqualTo(updateProfileRequest.get().getName());

        UserDao userDao = DataBaseSteps.getUserByUsername(SessionStorage.getUser().getUsername());
        assertThat(userDao.getName()).isEqualTo(updateProfileRequest.get().getName());
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
    @APIVersion("with_database_with_fix")
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

        UserDao userDao = DataBaseSteps.getUserByUsername(SessionStorage.getUser().getUsername());
        assertThat(userDao.getName()).isNotEqualTo(name);
    }

    @AfterEach
    public void tearDown() {
        SessionStorage.clear();
    }
}
