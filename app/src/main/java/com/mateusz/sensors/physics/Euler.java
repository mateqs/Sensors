package com.mateusz.sensors.physics;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mateusz on 2015-09-14.
 */
public class Euler {

    //time interval
    private static float timestamp;
    public static final float NS2S = 1.0f / 1000000000.0f;

    //Last saved Euler angles values
    private static volatile double lastPhi;
    private static volatile double lastTheta;
    private static volatile double lastPsi;

    //Values from accelerometer and gyroscope for filtering
    private static volatile float[] accel = new float[3];
    private static volatile float[] magnet = new float[3];

    //Complementary filter fields
    public static final float FILTER_COEFFICIENT = 0.97f;
    private static Timer timer = new Timer();
    private static float[] rotationMatrix = new float[9];
    private static float[] accMagOrientation = new float[3];


    //Returns Euler angles if event from gyro, or null otherwise
    public static double[] getEulers(SensorEvent event) {

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

    public static void setComplementaryFilterOn() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                    SensorManager.getOrientation(rotationMatrix, accMagOrientation);
                }

                float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

                lastPhi = lastPhi * FILTER_COEFFICIENT + oneMinusCoeff * (accMagOrientation[1] * -1.0f);
                lastTheta = lastTheta * FILTER_COEFFICIENT + oneMinusCoeff * accMagOrientation[2];
                lastPsi = lastPsi * FILTER_COEFFICIENT + oneMinusCoeff * (accMagOrientation[0] * -1.0f);

            }
        }, 500, 30);

    }

    public static void turnFiltersOff() {
        timer.cancel();
    }

    private static double[] calculateGyroData(SensorEvent event) {

        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            final double gyroX = event.values[0];
            final double gyroY = event.values[1];
            final double gyroZ = event.values[2];

            final double sinPhi = Math.sin(lastPhi);
            final double cosPhi = Math.cos(lastPhi);
            final double cosTheta = Math.cos(lastTheta);
            final double tanTheta = Math.tan(lastTheta);

            final double phi = lastPhi +     (dT * ( gyroX + gyroY * sinPhi * tanTheta + gyroZ * cosPhi * tanTheta));
            final double theta = lastTheta + (dT * ( gyroY * cosPhi - gyroZ * sinPhi));
            final double psi = lastPsi +     (dT * ((gyroY * sinPhi) / cosTheta + (gyroZ * cosPhi) / cosTheta));

            lastPhi = phi;
            lastTheta = theta;
            lastPsi = psi;

        }

        timestamp = event.timestamp;

        double[] eulers = new double[3];
        eulers[0] = lastPhi;
        eulers[1] = lastTheta;
        eulers[2] = lastPsi;
        return eulers;
    }

    public static void resetValuesToZero() {

        lastPhi = 0;
        lastTheta = 0;
        lastPsi = 0;
    }

}
