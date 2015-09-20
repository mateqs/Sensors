package com.mateusz.sensors.openGl;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mateusz.sensors.physics.Quaternions;


public class OpenGL extends Activity {

    private FilterinModes mode = FilterinModes.NONE;
    private MyGLView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = new MyGLView(this);
        setContentView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mode) {
                    case NONE: {

                        mode = FilterinModes.COMPLEMENTARY;
                        Quaternions.setComplementaryFilterOn();
                        Toast.makeText(getApplicationContext(), "Complementary filter", Toast.LENGTH_SHORT).show();

                        break;
                    }
                    case COMPLEMENTARY: {
                        mode = FilterinModes.NONE;
                        Quaternions.turnFiltersOff();
                        Toast.makeText(getApplicationContext(), "Filtering turned off", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        view.onPause();
        ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(view.getRenderer());
        Quaternions.turnFiltersOff();
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.onResume();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        MyGLRenderer renderer = view.getRenderer();
        sensorManager.registerListener(renderer, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(renderer, mag, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(renderer, gyr, SensorManager.SENSOR_DELAY_NORMAL);
    }
}

enum FilterinModes {
    NONE, COMPLEMENTARY, KALMAN
}