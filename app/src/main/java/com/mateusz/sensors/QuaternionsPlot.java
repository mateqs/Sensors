package com.mateusz.sensors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.mateusz.sensors.physics.Quaternions;

public class QuaternionsPlot extends Activity implements SensorEventListener {

    private GraphView graph;
    private BarGraphSeries<DataPoint> series;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quaternions_plot);


        graph = (GraphView) findViewById(R.id.graph);


        series = new BarGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0)
        });
        series.setSpacing(2);
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.RED);
        graph.addSeries(series);



    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double[] quaternions = Quaternions.getQuaternions(event);

        if (quaternions != null) {

            final double condition = Math.pow(quaternions[0], 2) + Math.pow(quaternions[1], 2) + Math.pow(quaternions[2], 2) + Math.pow(quaternions[3], 2);

            series.resetData(new DataPoint[]{
                    new DataPoint(0, condition),
                    new DataPoint(1, quaternions[0]),
                    new DataPoint(2, quaternions[1]),
                    new DataPoint(3, quaternions[2]),
                    new DataPoint(4, quaternions[3])
            });
        }

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {

            case R.id.none:
                if(checked){
                    Quaternions.turnFiltersOff();
                }
                break;
            case R.id.complementary:
                if(checked){
                    Quaternions.turnFiltersOff();
                    Quaternions.setComplementaryFilterOn();
                }
                break;
            case R.id.kalman:
                if(checked){
                    Quaternions.turnFiltersOff();
                    Quaternions.setKalmanFilterOn();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Quaternions.resetValuesToZero();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyr = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {

        ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
        Quaternions.turnFiltersOff();
        super.onPause();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        Toast.makeText(getApplicationContext(), sensor.getName() + " accuarycy changed to: " + accuracy, Toast.LENGTH_SHORT).show();

    }


}
