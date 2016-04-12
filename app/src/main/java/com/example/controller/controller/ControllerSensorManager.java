package com.example.controller.controller;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by hatemshahbari on 01/04/2016.
 */
public class ControllerSensorManager implements SensorEventListener {

    private final Context context;
    private static final String TAG = "ControllerSensorManager";
    private final Sensor rotationVectorSensor;
    private SensorManager mSensorManager;
    private HandlerThread mSensorThread;
    private Handler mSensorHandler;
    private float[] rotationVector;
    private MatrixCalculator mMatrixCalculator;
    private boolean running;

    /**
     * Sensor manager that receives data from the sensors in the background.<br/>
     * Call {@link #start()} to start receiving data and {@link #stop()} when you're done.
     * @param context context
     * @param listener A matrix calculator that listens for the sensor data
     */
    public ControllerSensorManager(Context context, MatrixCalculator listener) {
        this.context = context;
        rotationVector = new float[3];
        mMatrixCalculator = listener;
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        rotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    /**
     * Starts receiving data from the sensors in a new HandlerThread. received data will be sent to {@link MatrixCalculator}<br/>
     * Don't forget to call {@link #stop()} when you're done.
     */
    public void start() {
        if (!running) {

            mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
            mSensorThread.start();
            mSensorHandler = new Handler(mSensorThread.getLooper()); //Blocks until looper is
            //prepared, which is fairly quick
            mSensorManager.registerListener(this, rotationVectorSensor, SensorManager
                    .SENSOR_DELAY_GAME, mSensorHandler);
            running = true;
        }
    }

    /**
     * Stops listening to data from the sensors and quits the HandlerThread
     */
    public void stop() {
        if (running) {
            mSensorManager.unregisterListener(this);
            mSensorThread.quit();
            running = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        rotationVector[0] = event.values[0];
        rotationVector[1] = event.values[1];
        rotationVector[2] = event.values[2];

        if (mMatrixCalculator != null) {
            mMatrixCalculator.onRotationVectorChanged(rotationVector);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
