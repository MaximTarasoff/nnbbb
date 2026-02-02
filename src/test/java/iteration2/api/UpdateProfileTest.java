package iteration2.api;

import api.generators.RandomModelGenerator;
import api.models.comparison.ModelAssertions;
import api.models.customer.profile.ReadProfileResponse;
import api.models.customer.profile.UpdateProfileRequest;
import api.models.customer.profile.UpdateProfileResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UpdateProfileTest extends BaseTest {
    @Test
    @UserSession(type = "API")
    public void AuthUserCanUpdateProfileNameTest() {
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);

        UpdateProfileResponse createUserResponse = new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .update(updateProfileRequest);

        ModelAssertions.assertThatModels(updateProfileRequest, createUserResponse).match();

        ReadProfileResponse readCustomerResponse = SessionStorage.getProfileSteps().getProfileInfo();

        assertThat(readCustomerResponse.getName()).isEqualTo(updateProfileRequest.getName());
        ModelAssertions.assertThatModels(readCustomerResponse, createUserResponse).match();
    }

    public static Stream<Arguments> nameInvalidData() {
        return Stream.of(
                Arguments.of("Rope Block Tra", "Name must contain two words with letters only"),
                Arguments.of("John", "Name must contain two words with letters only")
        );
    }

    @MethodSource("nameInvalidData")
    @ParameterizedTest
    @UserSession(type = "API")
    public void AuthUserCannotUpdateProfileWithInvalidNameTest(String name, String errorValue) {
        UpdateProfileRequest updateProfileRequest =
                UpdateProfileRequest.builder()
                        .name(name)
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .update(updateProfileRequest);

        ReadProfileResponse readProfileResponse = SessionStorage.getProfileSteps().getProfileInfo();
        assertThat(readProfileResponse.getName()).isNotEqualTo(name);
    }
}
