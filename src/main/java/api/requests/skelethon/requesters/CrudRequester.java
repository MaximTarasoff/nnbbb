package api.requests.skelethon.requesters;

import api.configs.Config;
import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import common.helpers.StepLogger;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private final static String API_VERSION = Config.getProperty("apiVersion");

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get() {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse get(long id) {
        return StepLogger.log("GET request to " + endpoint.getUrl() + "/" + id, () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_VERSION + endpoint.getUrl(id))
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        var body = model == null ? "" : model;
        return StepLogger.log("PUT request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .put(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse update(long id, BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse delete(long id, BaseModel model) {
        return null;
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz, long id) {
        return StepLogger.log("GET request to " + endpoint.getUrl(), () -> {
            return given()
                    .spec(requestSpecification)
                    .get(API_VERSION + endpoint.getUrl(id))
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }
}
