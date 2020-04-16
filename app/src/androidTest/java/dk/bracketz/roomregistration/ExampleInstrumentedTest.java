package dk.bracketz.roomregistration;

import android.content.Context;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dk.bracketz.roomregistration.activities.login.LoginActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule(LoginActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("dk.bracketz.roomregistration", appContext.getPackageName());
    }

    @Test
    public void testLoginActivity() {

        onView(withId(R.id.loginEnterMail)).perform(replaceText("test@test.dk"));
        onView(withId(R.id.loginEnterPassword)).perform(replaceText("123456"));
        onView(withId(R.id.loginButton)).perform(ViewActions.click());

        ViewInteraction viewInteraction = onView(withText("Logged in successfully."));
        viewInteraction.inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))));
        viewInteraction.check(matches(isDisplayed()));
    }
}
