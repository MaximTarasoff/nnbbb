package iteration2;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.comparison.ModelAssertions;
import models.customer.profile.ReadProfileResponse;
import models.customer.profile.UpdateProfileRequest;
import models.customer.profile.UpdateProfileResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.ProfileSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UpdateProfileTest {
    private static CreateUserRequest createUserRequest;

    @BeforeAll
    public static void setup() {
        createUserRequest = AdminSteps.createUser();
    }

    @Test
    public void AuthUserCanUpdateProfileNameTest() {
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);

        UpdateProfileResponse createUserResponse = new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK("message", "Profile updated successfully"))
                .update(updateProfileRequest);

        ModelAssertions.assertThatModels(updateProfileRequest, createUserResponse).match();

        ReadProfileResponse readCustomerResponse = ProfileSteps.getProfileInfo(
                createUserRequest.getUsername(),
                createUserRequest.getPassword());

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
    public void AuthUserCannotUpdateProfileWithInvalidNameTest(String name, String errorValue) {
        UpdateProfileRequest updateProfileRequest =
                UpdateProfileRequest.builder()
                        .name(name)
                        .build();

        new CrudRequester(
                RequestSpecs.authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword()),
                Endpoint.CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsBadRequest(errorValue))
                .update(updateProfileRequest);

        ReadProfileResponse readProfileResponse = ProfileSteps.getProfileInfo(
                createUserRequest.getUsername(),
                createUserRequest.getPassword());
        assertThat(readProfileResponse.getName()).isNotEqualTo(name);
    }
}
