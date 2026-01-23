package requests.steps;

import models.accounts.CreateAccountResponse;
import models.customer.profile.ReadProfileResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

public class ProfileSteps {
    public static ReadProfileResponse getProfileInfo(String username, String password){
        return new ValidatedCrudRequester<ReadProfileResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    public static List<CreateAccountResponse> getProfileAccounts(String username, String password){
        return new ValidatedCrudRequester<ReadProfileResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK()
        ).get().getAccounts();
    }
}
