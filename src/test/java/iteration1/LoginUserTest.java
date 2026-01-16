package iteration1;

import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;


public class LoginUserTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<LoginResponse>(
                RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(loginRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest createUserRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(createUserRequest)
                .header("Authorization", Matchers.notNullValue());
    }
}
