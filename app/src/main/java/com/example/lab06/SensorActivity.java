package com.example.lab06;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.file.Path;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor mSensor;
    private int sensorType;
    private long lastUpdate = -1;
    private TextView sensorLabelTextView;
    private TextView sensorValuesTextView;
    private ImageView ballImgView;
    private ImageView moonImgView;
    private ImageView sunImgView;

    private int screenWidth, screenHeight, imgEdgeSize;
    private boolean layoutReady;
    private ConstraintLayout mainContainer;
    private Path upPath;
    private Path downPath;
    private boolean animFlag = false;
    private boolean isFlashOn = false;
    private boolean hasFlash = false;

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

                if(sensorType == Sensor.TYPE_ACCELEROMETER){
                    ballImgView.setVisibility(View.VISIBLE);
                    moonImgView.setVisibility(View.INVISIBLE);
                    sunImgView.setVisibility(View.INVISIBLE);
                }else if(sensorType == Sensor.TYPE_LIGHT){
                    ballImgView.setVisibility(View.INVISIBLE);
                    moonImgView.setVisibility(View.VISIBLE);
                    moonImgView.setAlpha(0f);
                    sunImgView.setVisibility(View.VISIBLE);
                    sunImgView.setAlpha(1f);
                }else{
                    ballImgView.setVisibility(View.INVISIBLE);
                    moonImgView.setVisibility(View.INVISIBLE);
                    sunImgView.setVisibility(View.INVISIBLE);

                    if(sensorType == Sensor.TYPE_PROXIMITY){
                        hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                        if(!hasFlash)
                            Toast.makeText(this, "No flashlight!", Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                Toast.makeText(this, "Wrong sensor type", Toast.LENGTH_SHORT).show();
            }
        }
        layoutReady = false;
        mainContainer = findViewById(R.id.sensor_container);
        mainContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imgEdgeSize = sunImgView.getWidth();
                screenWidth = mainContainer.getWidth();
                screenHeight = mainContainer.getHeight();

                float rectY = (screenHeight - imgEdgeSize)/2f;
                float rectHeight = screenHeight-rectY-imgEdgeSize;
                float rectWidth = min(screenWidth - imgEdgeSize, rectHeight);
                float rectX = (screenWidth - rectWidth - imgEdgeSize)/2;
                RectF animRect = new RectF(rectX, rectY, rectX+rectWidth, rectY+rectHeight);

                upPath = new Path();
                downPath = new Path();
                upPath.arcTo(animRect, 90f, -180f, true);
                //wczytac inna klase Path
                downPath.arcTo(animRect, 270f, -180f, true);
                mainContainer.getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
                layoutReady = true;
            }
        });
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

    @Override
    protected void onResume(){
        super.onResume();
        if(mSensor != null)
            MainActivity.mSensorManager.registerListener(this, mSensor, 100000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mSensor != null)
            MainActivity.mSensorManager.unregisterListener(this, mSensor);
    }

    private void handleLightSensor(float sensorValue){
        if(!animFlag && (sensorValue < 100)){
            animFlag = true;
            moonImgView.setAlpha(0f);
            sunImgView.setAlpha(1f);
            lightSensorAnimation(true);
        }else if(animFlag && (sensorType == Sensor.TYPE_LIGHT && sensorValue >= 100)){
            animFlag = false;
            lightSensorAnimation(false);
        }
    }

    private void lightSensorAnimation(boolean showMoon){
        ObjectAnimator sunAnimator;
        final ObjectAnimator moonAnimator;
        ObjectAnimator sunFadeAnimator;
        ObjectAnimator moonFadeAnimator;

        float moonFromAlpha = showMoon ? 0f : 1f;
        float moonToAlpha = showMoon ? 1f : 0f;
        float sunFromAlpha = showMoon ? 1f : 0f;
        float sunToAlpha = showMoon ? 0f : 1f;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Path moonPath = showMoon ? upPath : downPath;
            Path sunPath = showMoon ? downPath : upPath;
            moonAnimator = ObjectAnimator.ofFloat(moonImgView, View.X, View.Y, moonPath);
            moonAnimator.setDuration(2000);
            moonAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            moonFadeAnimator = ObjectAnimator.ofFloat(moonImgView, "alpha", moonFromAlpha, moonToAlpha);
            moodFadeAnimator.setInterpolator(new AccelerateInterpolator());
            moonFadeAnimator.setDuration(2200);

            sunAnimator = ObjectAnimator.ofFloat(sunImgView, View.X, View.Y, sunPath);
            sunAnimator.setDuration(2000);
            sunAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            sunFadeAnimator = ObjectAnimator.ofFloat(sunImgView, "alpha", sunFromAlpha, sunToAlpha);
            sunFadeAnimator.setInterpolator(new AccelerateInterpolator());
            sunFadeAnimator.setDuration(2200);
        }else{
            float moonStartY = showMoon ? screenHeight - imgEdgeSize : (screenHeight - imgEdgeSize)/2f;
            float moonEndY = showMoon ? (screenHeight - imgEdgeSize)/2f : screenHeight - imgEdgeSize;
            float sunStartY = !showMoon ? screenHeight - imgEdgeSize : (screenHeight - imgEdgeSize)/2f;
            float sunEndY = !showMoon ? (screenHeight - imgEdgeSize)/2f : screenHeight - imgEdgeSize;

            moonAnimator = ObjectAnimator.ofFloat(moonImgView, "y", moonStartY, moonEndY);
            moonAnimator.setInterpolator(new AccelerateInterpolator());
            moonAnimator.setDuration(2000);

            sunAnimator = ObjectAnimator.ofFloat(sunImgView, "y", sunStartY, sunEndY);
            sunAnimator.setInterpolator(new AccelerateInterpolator());
            sunAnimator.setDuration(2000);

            moonFadeAnimator = ObjectAnimator.ofFloat(moonImgView, "alpha", moonFromAlpha, moonToAlpha);
            moonFadeAnimator.setInterpolator(new AccelerateInterpolator());
            moonFadeAnimator.setDuration(2200);
            sunFadeAnimator = ObjectAnimator.ofFloat(sunImgView, "alpha", sunFromAlpha, sunToAlpha);
            sunFadeAnimator.setInterpolator(new AccelerateInterpolator());
            sunFadeAnimator.setDuration(2200);
        }
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(sunAnimator).with(moonAnimator).with(sunFadeAnimator).with(moonFadeAnimator);
        animSet.start();

        int startColor = showMoon ? Color.WHITE : getResources().getColor(android.R.color.background_dark);
        int endColor = showMoon ? getResources().getColor(android.R.color.background_dark) : Color.WHITE;
        final int labelColor = showMoon ? Color.WHITE : getResources().getColor(R.color.colorPrimary);
        final int valuesColor = showMoon ? Color.WHITE : getResources().getColor(R.color.colorAccent);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(1000);

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mainContainer.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation){
                super.onAnimationEnd(animation);
                sensorValuesTextView.setTextColor(valuesColor);
                sensorLabelTextView.setTextColor(labelColor);
            }
        });
    }

    private void handleAccelerationSensor(float sensorValue){
        if(!animFlag){
            if(abs(sensorValue) > 1){
                animFlag = true;
                FlingAnimation flingX = new FlingAnimation(ballImgView, DynamicAnimation.X);
                flingX.setStartVelocity(-1 * sensorValue * screenWidth/2f)
                        .setMinValue(5)
                        .setMaxValue(screenWidth - imgEdgeSize - 5)
                        .setFriction(1f);
            }
        }
    }
}
