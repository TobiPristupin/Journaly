package com.example.journaly;

import android.content.Context;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.fragment.app.testing.FragmentScenario;

import com.example.journaly.profile_screen.ProfileFragment;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.journaly", appContext.getPackageName());
    }

    @Test
    public void userCanFollowAndUnfollow(){
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launch(ProfileFragment.class);
        Espresso.onView(ViewMatchers.withId(R.id.follow_button))
    }

}