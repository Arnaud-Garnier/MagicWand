package com.example.arnaud.wizzguild;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created with love by Arnaud on 22/10/2016.
 */
public class AccelerometerActivity extends Activity implements SensorEventListener {

    protected Display mDisplay;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravity;
    private Sensor linearAcc;

    private AppView custom;
    protected TextView textView2;
    protected TextView xCoord;
    protected TextView yCoord;
    protected TextView zCoord;
    protected TextView rgbVal;
    protected Button recastButton;
    protected Button colorSelect;

    private CameraManager mCameraManager;
    private String mCameraId;
    private Boolean isTorchOn;

    float x, y, z, lastX, lastY, lastZ;
    boolean point1 = false;
    boolean point2 = false;
    boolean point3 = false;
    boolean point4 = false;

    int g = 0;
    int b = 0;

    boolean boolColorSelect = false;
    long lastUpdate, lastUpdateBis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mDisplay = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        textView2 = (TextView) findViewById(R.id.textView2);
        xCoord = (TextView) findViewById(R.id.xCoord);
        yCoord = (TextView) findViewById(R.id.yCoord);
        zCoord = (TextView) findViewById(R.id.zCoord);
        rgbVal = (TextView) findViewById(R.id.rgbVal);
        textView2.setText(R.string.acc_title);
        custom = (AppView) findViewById(R.id.custom);
        recastButton = (Button) findViewById(R.id.recastButton);
        colorSelect = (Button) findViewById(R.id.colorSelect);

        lastX = 0;
        lastY = 0;
        lastZ = 0;
        lastUpdate = 0;
        lastUpdateBis = 0;

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        isTorchOn = false;

        recastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // On réinitialise les points avec leur valeur par défaut et on supprime la couleur de fond (qui indique qu'un sort à été lancé)
            public void onClick(View v) {
                point1 = false;
                point2 = false;
                point3 = false;
                point4 = false;
                boolColorSelect = false;
                custom.setBackgroundColor(Color.TRANSPARENT);
                rgbVal.setText("");
                if (isTorchOn) {
                    turnOffFlashLight();
                }
            }
        });

        colorSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolColorSelect = true;
            }
        });
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, gravity);
        sensorManager.unregisterListener(this, linearAcc);
        super.onPause();
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void turnOnFlashLight() {
        try {
            mCameraManager.setTorchMode(mCameraId, true);
            isTorchOn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void turnOffFlashLight() {
        try {
            mCameraManager.setTorchMode(mCameraId, false);
            isTorchOn = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        // ***DEBUG***
        /*xCoord.setText("x = " + x);
        yCoord.setText("y = " + y);
        zCoord.setText("z = " + z);*/

        if (!boolColorSelect) {
            if ((y >= -1 && y <= 2) && (z >= 9 && z <= 11) && (x >= -1 && x <= 1) && !point1 && !point2 && !point3 && !point4) {
                point1 = true;
            }

            if ((y >= 5 && y <= 10) && (x >= 2 && x < 7) && (z >= 0 && z <= 5) && point1 && !point2 && !point3 && !point4) {
                point2 = true;
            }

            if ((y >= 5 && y <= 10) && (z >= -1 && z <= 5) && (x >= -1 && x <= 4) && !point3 && point2 && !point4) {
                point3 = true;
            }

            if ((y >= -1 && y <= 1) && (x >= -1 && x <= 1) && (z >= -10 && z <= -7) && point1 && !point2 && !point3 && !point4) {
                point4 = true;
            }

            // Spell to turn on the light
            if (point1 && point2 && point3) {
                if (!isTorchOn) {
                    turnOnFlashLight();
                    point1 = false;
                    point2 = false;
                    point3 = false;
                }
            }

            // Spell to turn off the light
            if (point1 && point4 && isTorchOn) {
                turnOffFlashLight();
                point1 = false;
                point2 = false;
                point4 = false;
            }
        } else {
            // Map r g b with x y z, refresh every 30ms
            // Display on screen rgb values
            if ((curTime - lastUpdate) > 30) {
                g = ((int) Math.floor(Math.abs(x / 2))) * 50;
                b = ((int) Math.floor(Math.abs(z / 2))) * 50;
                custom.setBackgroundColor(Color.rgb(255, g, b));
                rgbVal.setText("R=255" + " G=" + g + " B=" + b);
                lastUpdate = curTime;
            }
        }
    }
}
