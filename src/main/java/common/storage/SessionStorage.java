package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.ProfileSteps;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final LinkedHashMap<CreateUserRequest, ProfileSteps> profileStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
            INSTANCE.profileStepsMap.put(user, new ProfileSteps(user.getUsername(), user.getPassword()));
        }
    }

    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getUserSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getUserSteps() {
        return getUserSteps(1);
    }

    public static CreateUserRequest getProfile(int number) {
        return new ArrayList<>(INSTANCE.profileStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getProfile() {
        return getProfile(1);
    }

    public static ProfileSteps getProfileSteps(int number) {
        return new ArrayList<>(INSTANCE.profileStepsMap.values()).get(number - 1);
    }

    public static ProfileSteps getProfileSteps() {
        return getProfileSteps(1);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
        INSTANCE.profileStepsMap.clear();
    }
}
