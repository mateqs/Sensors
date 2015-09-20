package com.mateusz.sensors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

public class SensorsRaw extends Activity  implements SensorEventListener{

    private SensorManager sensorManager;
    private boolean booleansArray[];
    private Sensor sensorsArray[];
    private LinkedList<LinkedList<TextView>> textViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensory);

        textViewList = findTextViews();

        Intent intent = getIntent();
        boolean acce = intent.getBooleanExtra("acce", true);
        boolean gyro = intent.getBooleanExtra("gyro", true);
        boolean magne= intent.getBooleanExtra("magne",true);

        booleansArray = new boolean[] {acce, gyro, magne};

        for (int i = 0; i < booleansArray.length ; i++){
            if(booleansArray [i] == false){
                setInvisible(textViewList.get(i));
            }
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor acceSen=null, gyroSen=null, magneSen = null;

        if (acce == true){
             acceSen = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (gyro == true){
             gyroSen = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        if (magne == true){
             magneSen = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        sensorsArray = new Sensor[]{acceSen,gyroSen,magneSen};

        boolean ok = true;
        for (int i =0 ; i<sensorsArray.length ; i++){

            if(ok == true) {
                if (sensorsArray[i] == null) {

                    if (booleansArray[i] == true) {
                        Toast.makeText(this, "Problem with some sensor", Toast.LENGTH_LONG).show();
                        ok=false;
                    }
                }
            }
        }
    }

    private LinkedList<LinkedList<TextView>> findTextViews(){

        LinkedList<TextView> acc = new LinkedList<TextView>();
        acc.add((TextView) (findViewById(R.id.acc)));
        acc.add((TextView) (findViewById(R.id.accx)));
        acc.add((TextView) (findViewById(R.id.accy)));
        acc.add((TextView) (findViewById(R.id.accz)));

        LinkedList<TextView> gyro = new LinkedList<TextView>();
        gyro.add((TextView) findViewById(R.id.gyro));
        gyro.add((TextView) findViewById(R.id.gyrox));
        gyro.add((TextView) findViewById(R.id.gyroy));
        gyro.add((TextView) findViewById(R.id.gyroz));

        LinkedList<TextView> magne = new LinkedList<TextView>();
        magne.add((TextView) findViewById(R.id.magne));
        magne.add((TextView) findViewById(R.id.magnex));
        magne.add((TextView) findViewById(R.id.magney));
        magne.add((TextView) findViewById(R.id.magnez));

        LinkedList<LinkedList<TextView>> lists = new LinkedList<>();
        lists.add(acc);
        lists.add(gyro);
        lists.add(magne);

        return lists;
    }

    public void setInvisible(LinkedList<TextView> invisible) {

        for(TextView element: invisible){
            element.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        for(Sensor sensor:sensorsArray){
            if(sensor != null){
                sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            updateText(0,x,y,z);
        }
        else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            updateText(1,x,y,z);

        }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            updateText(2,x,y,z);
        }
    }

    private void updateText(int i,float x, float y, float z){
        LinkedList<TextView> list= textViewList.get(i);

        list.get(1).setText(String.valueOf(x));
        list.get(2).setText(String.valueOf(y));
        list.get(3).setText(String.valueOf(z));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
