package iteration1;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

public class Practise {

    @BeforeAll
    public static void setUpBeforeClass() {
        RestAssured.filters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        // или в самом запросе или/и ответе given().log().all()
        ////                .log().all()           // Всё об ответе
        ////                .log().body()          // Только тело ответа
        ////                .log().headers()       // Только заголовки ответа
        ////                .log().cookies()       // Только cookies из ответа
        ////                .log().status()        // Только статус код
        ////                .log().ifError()       // Логирует только если ошибка
        ////                .log().ifStatusCodeIs(404)  // Логирует если статус 404
        ////                .log().ifValidationFails()  // Логирует если валидация не прошла
    }


    @Test
    public void practiseTest() {
        String username = "user" + new Random().nextInt(10000);
        String userBody = String.format(
                """
                          {
                          "username": "%s",
                          "password": "Kate#1997fhdsfjhds",
                          "role": "USER"
                          }
                        """, username);
        String userAuth = given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(userBody)
//                .log().all() //
//                .log().all()           // Всё об ответе
//                .log().body()          // Только тело ответа
//                .log().headers()       // Только заголовки ответа
//                .log().cookies()       // Только cookies из ответа
//                .log().status()        // Только статус код
//                .log().ifError()       // Логирует только если ошибка
//                .log().ifStatusCodeIs(404)  // Логирует если статус 404
//                .log().ifValidationFails()  // Логирует если валидация не прошла
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .header("Authorization");
//                .body("username", Matchers.equalTo(username))
//                .body("password", Matchers.not(Matchers.equalTo("Kate#1997fhdsfjhds")))
//                .body("role", Matchers.equalTo("USER"));

        int oldCount = given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", userAuth)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("size()");

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", userAuth)
                .when()
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", userAuth)
                .when()
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThan(oldCount));
    }

    private static Stream<Arguments> invalidParameters() {
        return Stream.of(
                Arguments.of("a", "Kate#1997fhdsfjhds", "USER", "username", "Username must be between 3 and 15 characters")
        );
    }

    @MethodSource("invalidParameters")
    @ParameterizedTest
    public void practiseParametrizedTest(String username, String password, String role, String errorKey, String errorValue) {
        String userBody = String.format(
                """
                          {
                          "username": "%s",
                          "password": "%s",
                          "role": "%s"
                          }
                        """, username, password, role);
        given()
                .contentType("application/json")
                .accept("application/json")
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(userBody)
                .when()
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, hasItem(errorValue));
//                .body("password", Matchers.not(Matchers.equalTo("Kate#1997fhdsfjhds")))
//                .body("role", Matchers.equalTo("USER"));
    }
}
