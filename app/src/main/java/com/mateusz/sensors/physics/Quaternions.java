package com.mateusz.sensors.physics;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mateusz on 2015-09-14.
 */
public class Quaternions {

    //time interval
    private static float timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;

    //Last saved quaternions
    private static volatile double q1;
    private static volatile double q2;
    private static volatile double q3;
    private static volatile double q4 = 1;

    //Values from accelerometer and gyroscope for filtering
    private static volatile float[] accel = new float[3];
    private static volatile float[] magnet = new float[3];

    //Complementary filter fields
    public static final float FILTER_COEFFICIENT = 0.98f;
    private static Timer timer = new Timer();
    private static float[] rotationMatrix = new float[9];
    private static float[] accMagOrientation = new float[3];

    //Returns quaternions if event from gyro, or null otherwise
    public static double[] getQuaternions(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                return calculateGyroData(event);

            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accel, 0, 3);
                return null;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnet, 0, 3);
                return null;
        }

        return null;

    }

    private static double[] calculateGyroData(SensorEvent event) {
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            final double x = event.values[0];
            final double y = event.values[1];
            final double z = event.values[2];

            double newQ1 = q1 + dT * 0.5f * (q4 * x - q3 * y + q2 * z);
            double newQ2 = q2 + dT * 0.5f * (q3 * x + q4 * y - q1 * z);
            double newQ3 = q3 + dT * 0.5f * (-1 * q2 * x + q1 * y + q4 * z);
            double newQ4 = q4 + dT * 0.5f * (-1 * q1 * x - q2 * y - q3 * z);

            final double Nq = Math.pow(newQ1, 2) + Math.pow(newQ2, 2) + Math.pow(newQ3, 2) + Math.pow(newQ4, 2);

            newQ1 = newQ1 / Math.sqrt(Nq);
            newQ2 = newQ2 / Math.sqrt(Nq);
            newQ3 = newQ3 / Math.sqrt(Nq);
            newQ4 = newQ4 / Math.sqrt(Nq);

            q1 = newQ1;
            q2 = newQ2;
            q3 = newQ3;
            q4 = newQ4;
        }

        timestamp = event.timestamp;

        double[] quaterions = new double[4];
        quaterions[0] = q1;
        quaterions[1] = q2;
        quaterions[2] = q3;
        quaterions[3] = q4;

        return quaterions;
    }

    public static void resetValuesToZero() {

        q1 = 0;
        q2 = 0;
        q3 = 0;
        q4 = 1;
    }

    public static void setComplementaryFilterOn() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                    SensorManager.getOrientation(rotationMatrix, accMagOrientation);

                final float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

                final double[] filteredQuat = eulersToQuaternions();

                q1 = q1 * FILTER_COEFFICIENT + filteredQuat[3] * oneMinusCoeff;
                q2 = q2 * FILTER_COEFFICIENT + filteredQuat[1] * oneMinusCoeff;
                q3 = q3 * FILTER_COEFFICIENT + filteredQuat[2] * oneMinusCoeff;
                q4 = q4 * FILTER_COEFFICIENT + filteredQuat[0] * oneMinusCoeff;
                }

            }
        }, 200, 30);

    }

    public static void turnFiltersOff() {
        timer.cancel();
    }

    public static double[] eulersToQuaternions() {

        final double c1 = Math.cos((-1) * accMagOrientation[0] / 2);
        final double s1 = Math.sin((-1) * accMagOrientation[0] / 2);
        final double c2 = Math.cos((-1) * accMagOrientation[1] / 2);
        final double s2 = Math.sin((-1) * accMagOrientation[1] / 2);
        final double c3 = Math.cos(accMagOrientation[2] / 2);
        final double s3 = Math.sin(accMagOrientation[2] / 2);
        final double c1c2 = c1 * c2;
        final double s1s2 = s1 * s2;

        final double[] quaternions = new double[4];

        quaternions[0] = c1c2 * c3 - s1s2 * s3;
        quaternions[1] = c1c2 * s3 + s1s2 * c3;
        quaternions[2] = s1 * c2 * c3 + c1 * s2 * s3;
        quaternions[3] = c1 * s2 * c3 - s1 * c2 * s3;

        return quaternions;
    }

    //Returns axis angle values if event from gyro, or null otherwise
    public static float[] getQuaterionsInAxisAngle(SensorEvent event){
        final float values[] = new float[4];
        final double[] quaternions= getQuaternions(event);

        if (quaternions != null){

            values[0]= (float) Math.toDegrees((2*Math.acos(quaternions[3])));
            values[1]= (float)quaternions[0];
            values[2]= (float)quaternions[1];
            values[3]= (float)quaternions[2];

            return values;
        }
        else
            return null;

    }
}
