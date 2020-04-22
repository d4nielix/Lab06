package com.example.lab06;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor mSensor;
    private int sensorType;
    private long lastUpdate = -1;
    private TextView sensorLabelTextView;
    private TextView sensorValuesTextView;
    private ImageView ballImgView;
    private ImageView moonImgView;
    private ImageView sunImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sensorLabelTextView = findViewById(R.id.sensorLabel);
        sensorValuesTextView = findViewById(R.id.sensorValues);
        ballImgView = findViewById(R.id.ballImageView);
        moonImgView = findViewById(R.id.moonImageView);
        sunImgView = findViewById(R.id.sunImageView);
        Intent receivedIntent = getIntent();
        if(receivedIntent != null){
            sensorType = receivedIntent.getIntExtra(MainActivity.SENSOR_TYPE, -1);
            if(sensorType != -1){
                mSensor = MainActivity.mSensorManager.getDefaultSensor(sensorType);
                sensorLabelTextView.setText(mSensor.getName());
            }else{
                Toast.makeText(this, "Wrong sensor type", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long timeMicro;
        if(lastUpdate == -1){
            lastUpdate = event.timestamp;
            timeMicro = 0;
        }else{
            timeMicro = (event.timestamp - lastUpdate)/1000L;
            lastUpdate = event.timestamp;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Time difference: ").append(timeMicro).append(" \u03bcs\n");

        for(int i=0; i<event.values.length; i++){
            stringBuilder.append(String.format("Val[%d]-%.4f\n", i, event.values[i]));
        }

        TextView valueTextView = findViewById(R.id.sensorValues);
        valueTextView.setText(stringBuilder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
