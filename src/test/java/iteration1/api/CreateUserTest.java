package iteration1.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.UserRole;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        //подготовка данных
        CreateUserRequest createdUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        //post запрос
        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(createdUserRequest);

        //Get зарос для проверки созданного юзера

        ModelAssertions.assertThatModels(createdUserRequest, createUserResponse).match();

        // Проверка через базу данных
        UserDao userDao = DataBaseSteps.getUserByUsername(createdUserRequest.getUsername());
        DaoAndModelAssertions.assertThat(createUserResponse, userDao).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                // username field validation
                Arguments.of("   ", "Password33$", "USER", "username", List.of("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("ab", "Password33$", "USER", "username", List.of("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("abc%", "Password33$", "USER", "username", List.of("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanCreateUserWithInvalidDataTest(String username, String password, UserRole role, String errorKey, List<String> errorValues) {
        CreateUserRequest createdUser = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role.toString())
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValues))
                .post(createdUser);

        assertNull(DataBaseSteps.getUserByUsername(createdUser.getUsername()));

    }
}
