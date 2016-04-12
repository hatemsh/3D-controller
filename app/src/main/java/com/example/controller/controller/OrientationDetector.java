package com.example.controller.controller;

import static android.opengl.Matrix.multiplyMV;

/**
 * Created by Hatem on 01-Apr-16.
 */
public class OrientationDetector {

    private static final int DIRECTION_UNKNOWN = -1;
    private static final int DIRECTION_FRONT = 1;
    private static final String TAG = "OrientationDetector";
    private static final int DIRECTION_TOP = 2;

    public static final int ORIENTATION_UNKNOWN = -1;

    public static final int ORIENTATION_FRONT_TOP = 0;
    public static final int ORIENTATION_FRONT_TOP_RIGHT = 1;
    public static final int ORIENTATION_FRONT_RIGHT = 2;
    public static final int ORIENTATION_FRONT_BOTTOM_RIGHT = 3;
    public static final int ORIENTATION_FRONT_BOTTOM = 4;
    public static final int ORIENTATION_FRONT_BOTTOM_LEFT = 5;
    public static final int ORIENTATION_FRONT_LEFT = 6;
    public static final int ORIENTATION_FRONT_TOP_LEFT = 7;

    public static final int ORIENTATION_BACK_TOP = 8;
    public static final int ORIENTATION_BACK_TOP_RIGHT = 9;
    public static final int ORIENTATION_BACK_RIGHT = 10;
    public static final int ORIENTATION_BACK_BOTTOM_RIGHT = 11;
    public static final int ORIENTATION_BACK_BOTTOM = 12;
    public static final int ORIENTATION_BACK_BOTTOM_LEFT = 13;
    public static final int ORIENTATION_BACK_LEFT = 14;
    public static final int ORIENTATION_BACK_TOP_LEFT = 15;

    private final float[] upVector;
    private final float[] upVectorRotated;
    private final float[] frontVector;
    private final float[] frontVectorRotated;

    private float[] laserPoint;

    public OrientationDetector() {

        upVector = new float[]{0, 1, 0, 0};
        upVectorRotated = new float[4];

        frontVector = new float[]{0, 0, 1, 0};
        frontVectorRotated = new float[4];

        laserPoint = new float[2];
    }


    public static String getOrientationString(int orientation) {
        switch (orientation) {
            case ORIENTATION_FRONT_TOP:
                return "front top";
            case ORIENTATION_FRONT_TOP_RIGHT:
                return "front top right";
            case ORIENTATION_FRONT_RIGHT:
                return "front right";
            case ORIENTATION_FRONT_BOTTOM_RIGHT:
                return "front bottom right";
            case ORIENTATION_FRONT_BOTTOM:
                return "front bottom";
            case ORIENTATION_FRONT_BOTTOM_LEFT:
                return "front bottom left";
            case ORIENTATION_FRONT_LEFT:
                return "front left";
            case ORIENTATION_FRONT_TOP_LEFT:
                return "front top left";
            case ORIENTATION_BACK_TOP:
                return "back top";
            case ORIENTATION_BACK_TOP_RIGHT:
                return "back top right";
            case ORIENTATION_BACK_RIGHT:
                return "back right";
            case ORIENTATION_BACK_BOTTOM_RIGHT:
                return "back bottom right";
            case ORIENTATION_BACK_BOTTOM:
                return "back bottom";
            case ORIENTATION_BACK_BOTTOM_LEFT:
                return "back bottom left";
            case ORIENTATION_BACK_LEFT:
                return "back left";
            case ORIENTATION_BACK_TOP_LEFT:
                return "back top left";
            default:
                return "unknown";
        }
    }

    private static final float j = 1f / (float) Math.sqrt(2);


    private static final float[][] orientationsFront = new float[][]{
            {0, 0, 1}, //front
            {0, 0, -1} //back
    };

    private static final float[][] orientationsTop = new float[][]{
            {0, 1, 0},  //top
            {j, j, 0},
            {1, 0, 0},
            {j, -j, 0},
            {0, -1, 0},
            {-j, -j, 0},
            {-1, 0, 0},
            {-j, j, 0},
    };

    public final int getOrientation(float[] rotationMatrix) {
        multiplyMV(upVectorRotated, 0, rotationMatrix, 0, upVector, 0);
        multiplyMV(frontVectorRotated, 0, rotationMatrix, 0, frontVector, 0);

        int front = getOrientation(frontVectorRotated, DIRECTION_FRONT);
        int top = getOrientation(upVectorRotated, DIRECTION_TOP);

        if (front != DIRECTION_UNKNOWN && top != DIRECTION_UNKNOWN) {
            return (front * orientationsTop.length + top);
        } else return DIRECTION_UNKNOWN;
    }

    public final float[] getLaserPosition(float[] rotationMatrix) {

        multiplyMV(upVectorRotated, 0, rotationMatrix, 0, upVector, 0);


        laserPoint[0] = upVectorRotated[0] / upVectorRotated[1] * 2f;
        laserPoint[1] = upVectorRotated[2] / upVectorRotated[1] * 2f;

        return laserPoint;
    }

    public static void scaleLaserPoint(float[] point, float width, float height) {
        point[1] *= width / height;

        point[0] = ((point[0] + 1) / 2) * width;
        point[1] = ((-point[1] + 1) / 2) * height;
    }


    private int getOrientation(float[] vector, int direction) {

        float[][] orientationsArr;
        double sensitivity;

        if (direction == DIRECTION_FRONT) {
            orientationsArr = orientationsFront;
            sensitivity = 0.95;
        } else if (direction == DIRECTION_TOP) {
            orientationsArr = orientationsTop;
            sensitivity = 0.98;
        } else {
            throw new IllegalArgumentException(
                    "arrayDirection must be either DIRECTION_FRONT or DIRECTION_TOP"
            );
        }

        float[] orientation;
        for (int i = 0; i < orientationsArr.length; i++) {
            orientation = orientationsArr[i];

            float cosa = vector[0] * orientation[0]
                    + vector[1] * orientation[1]
                    + vector[2] * orientation[2];

            if (cosa > sensitivity) {
                return i;
            }
        }
        return DIRECTION_UNKNOWN;
    }

}
