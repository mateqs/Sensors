package com.mateusz.sensors.physics;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * Created by Mateusz on 2015-09-17.
 */
public class Test {

    private static float[] rotationMatrix = new float[9];

    private static float[] accel = new float[3];
    private static float[] magnet = new float[3];

    private static float[] accMagOrientation = new float[3];

    public static void setValuesFromEvent(SensorEvent event){

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, accel, 0, 3);
        }
        else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            System.arraycopy(event.values, 0, magnet, 0, 3);
        }

    }


    public static float[] getEulers() {

            if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                SensorManager.getOrientation(rotationMatrix, accMagOrientation);
            }
        return accMagOrientation;
    }
}
