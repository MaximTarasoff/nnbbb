package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateUserRequest;

import static io.restassured.RestAssured.given;

public class AdminCreateUserRequest extends Request<CreateUserRequest> {

    public AdminCreateUserRequest(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateUserRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return null;
    }
}
