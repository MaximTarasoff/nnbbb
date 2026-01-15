package specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import models.LoginRequest;
import requests.LoginRequester;

import java.util.List;

public class RequestSpecs {
    //класс один на всех поэтому запрещаем создание объектов
    private RequestSpecs() {}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType("application/json")
                .setAccept("application/json")
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()))
                .setBaseUri("http://localhost:4111");
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        //получаем токен для юзера
        String userAuthHeader = new LoginRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(LoginRequest.builder().username(username).password(password).build())
                .extract().header("Authorization");

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
}
