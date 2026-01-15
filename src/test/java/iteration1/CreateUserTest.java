package iteration1;

import generators.RandomData;

import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequest;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectDataTest() {
        CreateUserRequest createdUser = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER).build();
        CreateUserResponse cur = new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createdUser)
                .extract().as(CreateUserResponse.class); //возвращаем в виде класса

        //чтобы мы не падали после падения одной строчки, можно использовать библиотеку AssertJ softly получаем через класс BaseTest
        softly.assertThat(createdUser.getUsername()).isEqualTo(cur.getUsername());
        softly.assertThat(createdUser.getPassword()).isNotEqualTo(cur.getPassword());
        softly.assertThat(cur.getRole()).isEqualTo(UserRole.USER.toString());

        //создание пользователя
//        given()
//                .contentType("application/json")
//                .accept(ContentType.JSON)
//                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
//                .body("""
//                        {
//                           "username": "kate193666",
//                           "password": "Kate#1997fhdsfjhds",
//                           "role": "USER"
//                        }
//                        """)
//                .post("http://localhost:4111/api/v1/admin/users")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED)
//                .body("username", Matchers.equalTo("kate193666"))
//                .body("username", Matchers.not(Matchers.equalTo("Kate#1997fhdsfjhds")))
//                .body("role", Matchers.equalTo("USER"));
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                //username field validation
                Arguments.of("   ", "Password33$", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "Password33$", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abc$", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots"),
                Arguments.of("abc%", "Password33$", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    //    @CsvSource({
//            //username field validation
//            "q, Password33$, USER, ",
//            "'  ', P@ssword3, USER, username, Username cannot be blank"
//    })
    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanCreateUserWithInvalidDataTest(String username, String password, UserRole role, String errorKey, String errorValue) {
        CreateUserRequest createdUser = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createdUser);

//        given()
//                .contentType("application/json")
//                .accept(ContentType.JSON)
//                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
//                .body(requestBody)
//                .post("http://localhost:4111/api/v1/admin/users")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_BAD_REQUEST)
//                .body(errorKey, hasItem(errorValue));
//                .body(errorKey, Matchers.equalTo(errorValue))

    }

}
