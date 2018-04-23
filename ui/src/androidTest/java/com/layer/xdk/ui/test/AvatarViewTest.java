package com.layer.xdk.ui.test;


import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.graphics.Color;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.WindowManager;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.presence.PresenceView;
import com.layer.xdk.ui.testactivity.AvatarActivityTestView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AvatarViewTest {

    private int mAwayColor = Color.rgb(0xF7, 0xCA, 0x40);

    @Rule
    public ActivityTestRule<AvatarActivityTestView> mAvatarActivityTestRule =
            new ActivityTestRule<>(AvatarActivityTestView.class);

    @Before
    public void setUp() {
        final AvatarActivityTestView avatarActivityTestView = mAvatarActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                avatarActivityTestView.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        avatarActivityTestView.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void testThatAvatarColorChangeWhenSpinnerIsChanged() {
        String selectionText = "AWAY";

        onView(withId(R.id.test_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
        onView(withId(R.id.test_spinner)).check(matches(withSpinnerText(containsString(selectionText))));
        onView(withText(selectionText)).perform(click());
    }


    public static Matcher<View> withBgColor(final int color) {
        Checks.checkNotNull(color);
        return new BoundedMatcher<View, PresenceView>(PresenceView.class) {
            @Override
            public boolean matchesSafely(PresenceView presenceView) {
                return color ==  presenceView.getSolidColor();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("");
            }
        };
    }

}