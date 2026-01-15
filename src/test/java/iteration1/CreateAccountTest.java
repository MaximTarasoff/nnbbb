package iteration1;

import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginRequest;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequest;
import requests.CreateAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;


import static models.UserRole.USER;
import static org.hamcrest.Matchers.hasItem;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getPassword())
                .role(USER)
                .build();

        LoginRequest loginRequest = LoginRequest.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();

        new AdminCreateUserRequest(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        CreateAccountResponse createAccountResponse = new CreateAccountRequester(
                RequestSpecs.authAsUser(loginRequest.getUsername(), loginRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        new CreateAccountRequester(
                RequestSpecs.authAsUser(loginRequest.getUsername(), loginRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get()
                .body("accountNumber", hasItem(createAccountResponse.getAccountNumber()));


//        int oldCount = given()
//                .contentType("application/json")
//                .accept("application/json")
//                .header("Authorization", userAuth)
//                .when()
//                .get("http://localhost:4111/api/v1/customer/accounts")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .extract()
//                .path("size()");
//        int oldCount = new CreateAccountRequester(
//                RequestSpecs.authAsUser(loginRequest.getUsername(), loginRequest.getPassword()),
//                ResponseSpecs.requestReturnsOK())
//                .get()
//                .extract()
//                .path("size()");
//
//        new CreateAccountRequester(
//                RequestSpecs.authAsUser(loginRequest.getUsername(), loginRequest.getPassword()),
//                ResponseSpecs.entityWasCreated())
//                .post(null);//тк тело пустое
//
//        new CreateAccountRequester(
//                RequestSpecs.authAsUser(loginRequest.getUsername(), loginRequest.getPassword()),
//                ResponseSpecs.requestReturnsOK())
//                .get()
//                .body("size()", greaterThan(oldCount));

//        given()
//                .contentType("application/json")
//                .accept("application/json")
//                .header("Authorization", userAuth)
//                .when()
//                .get("http://localhost:4111/api/v1/customer/accounts")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_OK)
//                .body("size()", greaterThan(oldCount));


//        String userAuthHeader = new LoginRequester(
//                RequestSpecs.unauthSpec(),
//                ResponseSpecs.requestReturnsOK())
//                .post(loginRequest)
//                .extract().header("Authorization");



//        String userAuthHeader = given()
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
//                //Параметр 5: хотим получить экстракт какого-то ответа, достать как переменную из ответа
//                .extract().header("Authorization");

        //создать аккаунт
//        given()
//                .header("Authorization", userAuthHeader)
//                .contentType("application/json")
//                .accept(ContentType.JSON)
//                .post("http://localhost:4111/api/v1/accounts")
//                .then()
//                .assertThat()
//                .statusCode(HttpStatus.SC_CREATED);

        //Запросить все аккаунт пользователя и проверить что наш аккаунт там


    }
}
