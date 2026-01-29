package api.requests.steps;

import api.models.accounts.AccountResponse;
import api.models.customer.profile.ReadProfileResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileSteps {
    private final String username;
    private final String password;

    public ProfileSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public ReadProfileResponse getProfileInfo(){
        return new ValidatedCrudRequester<ReadProfileResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK()
        ).get();
    }

    public List<AccountResponse> getAllProfileAccounts(){
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpecs.requestReturnsOK()
        ).getAll(AccountResponse[].class);
    }
}
