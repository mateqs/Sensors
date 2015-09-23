package com.mateusz.sensors;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import Jama.Matrix;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testMatrix() throws Exception {

        Matrix n = Matrix.identity(4,4);
        n=n.times(2);
        Log.i("Matrix",String.valueOf(n.get(3,3)));
        assertEquals(true, true);

    }
}