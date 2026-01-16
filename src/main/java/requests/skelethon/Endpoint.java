package requests.skelethon;


import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

/*given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);*/
//url, body request, body response - у нас жестко связаны
@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER("/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    LOGIN("/auth/login",
            LoginRequest.class,
            LoginResponse.class
    ),
    ACCOUNTS("/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
