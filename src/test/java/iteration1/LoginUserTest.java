package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.LoginRequest;
import models.UserRole;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequest;
import requests.LoginRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;


public class LoginUserTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();
        new LoginRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginRequest);
//
//
//        //У нас в данном случае жесткая связка: эндпойнт, JSON запроса, JSON ответа // обычно такие связки проектируются с помощью enum-ов или ДатаКлассов
//        given()
//                .spec(RequestSpecification) //Параметр 1: спецификация включает в себя (хэдеры)
//                //Параметр 2: меняется тело запроса
//                .body("""
//                        {
//                          "username": "admin",
//                          "password": "admin"
//                        }
//                        """)
//                // Параметр 3: эндпойнт
//                .post("http://localhost:4111/api/v1/auth/login")
//                .then()
//                .assertThat()
//                //Параметр 4: спецификация ответа (статус код, проверки и тд)
//                .spec(ResponseSpecification);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(UserRole.USER)
                .build();

        new AdminCreateUserRequest(RequestSpecs.adminSpec(), ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .username(createUserRequest.getUsername())
                .password(createUserRequest.getPassword())
                .build();

        new LoginRequester(RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginRequest)
                .header("Authorization", Matchers.notNullValue());
        //создание пользователя
//        given()
//                .contentType("application/json")
//                .accept(ContentType.JSON)
//                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
//                .body("""
//                        {
//                           "username": "kate19981223",
//                           "password": "Kate#1997fhdsfjhds",
//                           "role": "USER"
//                        }
//                        """)
//                .post("http://localhost:4111/api/v1/admin/users")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED);
//
//        given()
//                .contentType("application/json")
//                .accept(ContentType.JSON)
//                .body("""
//                        {
//                          "username": "kate1998122",
//                           "password": "Kate#1997fhdsfjhds"
//                        }
//                        """)
//                .post("http://localhost:4111/api/v1/auth/login")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .header("Authorization", Matchers.notNullValue());
    }
}
