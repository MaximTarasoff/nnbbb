package iteration1.api;

import common.extensions.APIVersionExtensions;
import common.extensions.TimingExtension;
import common.extensions.UserSessionExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UserSessionExtension.class)
@ExtendWith(TimingExtension.class)
@ExtendWith(APIVersionExtensions .class)
public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    //если какой-то из тестов не прошел, то роняем его
    @AfterEach
    public void afterTest(){
        softly.assertAll();
    }
}
