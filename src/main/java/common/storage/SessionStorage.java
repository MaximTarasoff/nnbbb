package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.ProfileSteps;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    /* Thread Local - способ сделать SessionStorage потокобезопасным

   Каждый поток обращаясь к INSTANCE.get() получают свою КОПИЮ

   Map<Thread, SessionStorage>

   Тест1 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ1), работает с ними
   Тест2 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ2), работает с ними
   Тест3 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ3), работает с ними
    */
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final LinkedHashMap<CreateUserRequest, ProfileSteps> profileStepsMap = new LinkedHashMap<>();

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            INSTANCE.get().userStepsMap.put(
                    user,
                    new UserSteps(user.getUsername(), user.getPassword())
            );

            INSTANCE.get().profileStepsMap.put(
                    user,
                    new ProfileSteps(user.getUsername(), user.getPassword())
            );
        }
    }


    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return getUser(1);
    }

    public static UserSteps getUserSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getUserSteps() {
        return getUserSteps(1);
    }

    public static CreateUserRequest getProfile(int number) {
        return new ArrayList<>(INSTANCE.get().profileStepsMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getProfile() {
        return getProfile(1);
    }

    public static ProfileSteps getProfileSteps(int number) {
        return new ArrayList<>(INSTANCE.get().profileStepsMap.values()).get(number - 1);
    }

    public static ProfileSteps getProfileSteps() {
        return getProfileSteps(1);
    }

    public static void clear() {
        INSTANCE.get().userStepsMap.clear();
        INSTANCE.get().profileStepsMap.clear();
    }
}
