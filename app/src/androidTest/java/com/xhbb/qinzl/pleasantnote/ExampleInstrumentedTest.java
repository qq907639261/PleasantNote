package com.xhbb.qinzl.pleasantnote;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals(BuildConfig.APPLICATION_ID, appContext.getPackageName());

        TypedArray typedArray = appContext.getResources().obtainTypedArray(R.array.play_spinner_drawables);
        Log.i(TAG, "useAppContext: " + typedArray.length());
        typedArray.recycle();
    }
}
