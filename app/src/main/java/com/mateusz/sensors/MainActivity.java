package com.mateusz.sensors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.mateusz.sensors.openGl.OpenGL;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch switchAcce = (Switch) findViewById(R.id.accelerometer);
        final Switch switchGyro = (Switch) findViewById(R.id.gyroskope);
        final Switch switchMagne = (Switch) findViewById(R.id.magnetometer);


        Button startButton = (Button) findViewById(R.id.startRawData);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean acce = switchAcce.isChecked();
                boolean gyro = switchGyro.isChecked();
                boolean magne = switchMagne.isChecked();


                Intent intent = new Intent(getApplicationContext(), SensorsRaw.class);

                intent.putExtra("acce", acce);
                intent.putExtra("gyro", gyro);
                intent.putExtra("magne", magne);

                startActivity(intent);
            }
        });

        ((Button) findViewById(R.id.startEuler)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), EulerPlot.class));
            }

        });
        ((Button) findViewById(R.id.startQuaternions)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), QuaternionsPlot.class));
            }
        });


        ((Button) findViewById(R.id.startCube)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OpenGL.class));
            }
        });

    }

}
