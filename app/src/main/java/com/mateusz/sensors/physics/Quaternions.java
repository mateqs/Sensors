package com.mateusz.sensors.physics;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import Jama.Matrix;

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
    private static final float[] accMagInitOrientation = new float[3];
    private static final float[] rotationMatrix = new float[9];
    private static final float[] accMagOrientation = new float[3];

    //Complementary filter fields
    public static final float FILTER_COEFFICIENT = 0.98f;
    private static Timer timer = new Timer();


    //Kalman filter fields
    private static boolean kalman = false;
    private static Matrix H;
    private static Matrix Q;
    private static Matrix R;
    private static Matrix x;
    private static Matrix P;


    //Returns quaternions if event from gyro, or null otherwise
    @Nullable
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

            final double phi = event.values[0];
            final double theta = event.values[1];
            final double psi = event.values[2];

            double newQ1 = 0;
            double newQ2 = 0;
            double newQ3 = 0;
            double newQ4 = 1;

            if (kalman) {

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                    SensorManager.getOrientation(rotationMatrix, accMagOrientation);

                    accMagOrientation[0] = accMagOrientation[0] - accMagInitOrientation[0];
                    accMagOrientation[1] = accMagOrientation[1] - accMagInitOrientation[1];
                    accMagOrientation[2] = accMagOrientation[2] - accMagInitOrientation[2];

                    Matrix AHelper = new Matrix(new double[][]{
                            {0, -phi, -theta, -psi},
                            {phi, 0, psi, -theta},
                            {theta, -psi, 0, phi},
                            {psi, theta, -phi, 0}
                    }).times(0.5).times(dT);
                    Matrix A = Matrix.identity(4, 4).plus(AHelper);

                    double[] values = eulersToQuaternions(phi, theta, psi);
                    Matrix z = new Matrix(new double[][]{{values[0], values[1], values[2], values[3]}});

//                Predict state
                    Matrix xp = A.times(x);

//                Predict error covariance
                    Matrix Pp = A.transpose().times(P).times(A).plus(Q);

//                Compute Kalman gain
                    Matrix K = Pp.times(H.transpose().times(H.times(Pp.times(H.transpose())).plus(R).inverse()));

//                Compute estimate
                    Matrix x = xp.plus(K.times(z.minus(H.times(xp).transpose()).transpose()));

                    newQ1 = x.get(0, 0);
                    newQ2 = x.get(1, 0);
                    newQ3 = x.get(2, 0);
                    newQ4 = x.get(3, 0);

//                Compute the error covariance
                    P = Pp.minus(Pp.times(H).times(K));

                }

            } else {
//                 Compute quaternions

                newQ1 = q1 + dT * 0.5f * (q4 * phi - q3 * theta + q2 * psi);
                newQ2 = q2 + dT * 0.5f * (q3 * phi + q4 * theta - q1 * psi);
                newQ3 = q3 + dT * 0.5f * (-1 * q2 * phi + q1 * theta + q4 * psi);
                newQ4 = q4 + dT * 0.5f * (-1 * q1 * phi - q2 * theta - q3 * psi);

            }

            //Normalize quaternions
            final double Nq = Math.pow(newQ1, 2) + Math.pow(newQ2, 2) + Math.pow(newQ3, 2) + Math.pow(newQ4, 2);
            final double sqrt=Math.sqrt(Nq);

            newQ1 = newQ1 / sqrt;
            newQ2 = newQ2 / sqrt;
            newQ3 = newQ3 / sqrt;
            newQ4 = newQ4 / sqrt;

            q1 = newQ1;
            q2 = newQ2;
            q3 = newQ3;
            q4 = newQ4;

        }
        timestamp = event.timestamp;

        double[] quaternions = new double[4];


        quaternions[0] = q1;
        quaternions[1] = q2;
        quaternions[2] = q3;
        quaternions[3] = q4;

        return quaternions;
    }

    public static void resetValuesToZero() {

        q1 = 0;
        q2 = 0;
        q3 = 0;
        q4 = 1;
    }

    public static void setKalmanFilterOn() {
        kalman = true;

//        Initial orientation from accelerometer and magnetometer
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet))
            SensorManager.getOrientation(rotationMatrix, accMagInitOrientation);

//        Setting initial values
            H = Matrix.identity(4, 4);
            Q = Matrix.identity(4, 4).times(0.0001);
            R = Matrix.identity(4, 4).times(10);
            x = new Matrix(new double[][]{{0, 0, 0, 1}}).transpose();
            P = Matrix.identity(4, 4).times(1);


    }

    public static void setComplementaryFilterOn() {

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagInitOrientation);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                        SensorManager.getOrientation(rotationMatrix, accMagOrientation);

                        final float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

                        accMagOrientation[0] = accMagOrientation[0] - accMagInitOrientation[0];
                        accMagOrientation[1] = accMagOrientation[1] - accMagInitOrientation[1];
                        accMagOrientation[2] = accMagOrientation[2] - accMagInitOrientation[2];

                        final double[] filteredQuat = eulersToQuaternions();

                        q1 = q1 * FILTER_COEFFICIENT + filteredQuat[3] * oneMinusCoeff;
                        q2 = q2 * FILTER_COEFFICIENT + filteredQuat[1] * oneMinusCoeff;
                        q3 = q3 * FILTER_COEFFICIENT + filteredQuat[2] * oneMinusCoeff;
                        q4 = q4 * FILTER_COEFFICIENT + filteredQuat[0] * oneMinusCoeff;
                    }

                }
            }, 500, 30);

        }
    }

    public static void turnFiltersOff() {
        timer.cancel();
        kalman = false;
    }

    private static double[] eulersToQuaternions(double phi, double theta, double psi) {

        double sinPhi = Math.sin(phi / 2);
        double sinTheta = Math.sin(theta / 2);
        double sinPsi = Math.sin(psi / 2);

        double cosPhi = Math.cos(phi / 2);
        double cosTheta = Math.cos(theta / 2);
        double cosPsi = Math.cos(psi / 2);

        double[] values = new double[4];

        values[0] = cosPhi * cosTheta * cosPsi + sinPhi * sinTheta * sinPsi;
        values[1] = sinPhi * cosTheta * cosPsi - cosPhi * sinTheta * sinPsi;
        values[2] = cosPhi * sinTheta * cosPsi + sinPhi * cosTheta * sinPsi;
        values[3] = cosPhi * cosTheta * sinPsi - sinPhi * sinTheta * cosPsi;

        return values;

    }


    private static double[] eulersToQuaternions() {

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

    //    Returns axis angle values if event from gyro, or null otherwise
    @Nullable
    public static float[] getQuaternionsInAxisAngle(SensorEvent event) {
        final float values[] = new float[4];
        final double[] quaternions = getQuaternions(event);

        if (quaternions != null) {

            values[0] = (float) Math.toDegrees((2 * Math.acos(quaternions[3])));
            values[1] = (float) quaternions[0];
            values[2] = (float) quaternions[1];
            values[3] = (float) quaternions[2];

            return values;
        } else
            return null;

    }
}
