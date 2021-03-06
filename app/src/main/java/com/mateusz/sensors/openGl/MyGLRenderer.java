package com.mateusz.sensors.openGl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import com.mateusz.sensors.R;
import com.mateusz.sensors.physics.Quaternions;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer, SensorEventListener {
    private static final String TAG = "GLRenderer";
    private final Context context;
    private final MyCube cube = new MyCube();
    private long fpsStartTime;
    private long numFrames;
    private float[] axisAngle = new float[4];

    MyGLRenderer(Context context) {
        this.context = context;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        for (int i = 0; i < axisAngle.length; i++) {
            axisAngle[i] = 0;
        }

        boolean SEE_THRU = true;

        long startTime = System.currentTimeMillis();
        fpsStartTime = startTime;
        numFrames = 0;
// Define the lighting
        float lightAmbient[] = new float[]{0.2f, 0.2f, 0.2f, 1};
        float[] lightPos = new float[]{1, 1, 1, 1};
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
// What is the cube made of?
        float matDiffuse[] = new float[]{1, 1, 1, 1};
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE,
                matDiffuse, 0);
// Set up any OpenGL options we need
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        if (SEE_THRU) {
            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
        }

// Enable textures
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL10.GL_TEXTURE_2D);

// Load the cube's texture from a bitmap
        MyCube.loadTexture(gl, context, R.drawable.one);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {

// Define the view frustum
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float) width / height;
        GLU.gluPerspective(gl, 45.0f, ratio, 1, 100f);

    }

    public void onDrawFrame(GL10 gl) {
// Clear the screen to black
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT
                | GL10.GL_DEPTH_BUFFER_BIT);

// Position model so we can see it
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -3.2f);

// Set rotation angle based on the time
        gl.glRotatef(-axisAngle[0], axisAngle[1], axisAngle[2], axisAngle[3]);

// Draw the model
        cube.draw(gl);

// Keep track of number of frames drawn
        numFrames++;
        long fpsElapsed = System.currentTimeMillis() - fpsStartTime;
        if (fpsElapsed > 5 * 1000) { // every 5 seconds
            float fps = (numFrames * 1000.0F) / fpsElapsed;
            Log.d(TAG, "Frames per second: " + fps + " (" + numFrames
                    + " frames in " + fpsElapsed + " ms)");
            fpsStartTime = System.currentTimeMillis();
            numFrames = 0;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] angles = Quaternions.getQuaternionsInAxisAngle(event);

        if (angles != null)
            axisAngle = angles;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}