package com.openclaw.androidapp;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        assertEquals("com.openclaw.androidapp",
            InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }
}
