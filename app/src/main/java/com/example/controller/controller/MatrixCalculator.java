package com.example.controller.controller;

import android.hardware.SensorManager;

import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;

/**
 * Created by hatemshahbari on 01/04/2016.
 */
public class MatrixCalculator {

    private OrientationListener orientationListener;
    private OrientationDetector orientationDetector;

    private float[] sensorRotationMatrix;
    private float[] initialOffsetMatrix;
    private float[] rotationMatrix;
    private float[] invertedSensorRotationMatrix;
    private float[] rotationVector;

    private int previousOrientation;
    private int currentOrientation;

    private static final String TAG = "MatrixCalculator";


    /**
     * Register a listener that will get notified when the phone orientation changes and where the phone is pointing
     * @param listener
     */
    public void registerOrientationListener(OrientationListener listener) {
        this.orientationListener = listener;
        this.orientationDetector = new OrientationDetector();
    }

    /**
     * Unregisters the {@link OrientationListener} set using {@link #registerOrientationListener(OrientationListener)}
     */
    public void unregisterOrientationListener() {
        this.orientationListener = null;
        this.orientationDetector = null;
    }

    /**
     * Turns Data sent to it from {@link ControllerSensorManager} into a rotation matrix, which is accessible through {@link #getRotationMatrix()}<br/>
     * Can notify listeners ({@link OrientationListener}) when the device orientation changes and where the device's top is pointing.
     */
    public MatrixCalculator() {
        sensorRotationMatrix = new float[16];
        initialOffsetMatrix = new float[16];
        rotationMatrix = new float[16];
        invertedSensorRotationMatrix = new float[16];
        rotationVector = new float[4];

        setIdentityM(rotationMatrix,0);
        setIdentityM(initialOffsetMatrix, 0);
    }


    protected void calculateMatrix() {

        SensorManager.getRotationMatrixFromVector(sensorRotationMatrix, rotationVector);

        invertM(invertedSensorRotationMatrix, 0, sensorRotationMatrix, 0);


        multiplyMM(rotationMatrix, 0, initialOffsetMatrix, 0, invertedSensorRotationMatrix, 0);

        if (orientationListener != null) {
            currentOrientation = orientationDetector.getOrientation(rotationMatrix);
            //Log.d(TAG, "4: " + Arrays.toString(rotationMatrix));

            if (previousOrientation != currentOrientation) {
                previousOrientation = currentOrientation;
                orientationListener.onOrientationChanged(currentOrientation);
            }
            orientationListener.onLaserPointChanged(orientationDetector.getLaserPosition
                    (rotationMatrix));
        }
    }

    /**
     *
     * @return ready to use rotation matrix with offset if {@link #offset()} has been called.
     */
    public float[] getRotationMatrix() {
        return rotationMatrix;
    }

    /**
     * Call this after telling the user how to hold his phone in your implementation of the default orientation
     */
    public void offset() {
        System.arraycopy(sensorRotationMatrix, 0, initialOffsetMatrix, 0, 16);
    }

    protected void onRotationVectorChanged(float[] newRotationVector) {
        this.rotationVector = newRotationVector;
        calculateMatrix();
    }

    public interface OrientationListener {
        void onOrientationChanged(int newOrientation);
        void onLaserPointChanged(float[] point);
    }
}
