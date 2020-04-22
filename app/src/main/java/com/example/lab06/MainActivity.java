package com.example.lab06;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        setContentView(R.layout.activity_main);

        SensorFragment sensorFragment = (SensorFragment) getSupportFragmentManager().findFragmentById(R.id.sensorList);
        sensorFragment.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showSensor(parent, position);
            }
        });
    }

    static public SensorManager mSensorManager;
    static List<Sensor> SensorList;
    static final public String SENSOR_TYPE = "sensorType";

    public void showSensor(AdapterView<?> parent, int position){
        Intent sensIntent = new Intent(this, SensorActivity.class);
        Sensor currentSensor = (Sensor) parent.getItemAtPosition(position);
        sensIntent.putExtra(SENSOR_TYPE, currentSensor.getType());
        startActivity(sensIntent);
    }
}
