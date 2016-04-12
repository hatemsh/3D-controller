package com.example.controller.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.controller.R;
import com.example.controller.controller.OrientationDetector;

import java.util.Random;

/**
 * Created by Hatem on 26-Mar-16.
 */
public class MatchCanvasView extends View {

    Paint mFillPaint;
    private int mWidth;
    private int mHeight;
    private static final String TAG = "CanvasView";
    private Random mRandom;
    private int currentOrientation;

    private final static int[] ORIENTATIONS = new int[]{
            OrientationDetector.ORIENTATION_FRONT_TOP,
            OrientationDetector.ORIENTATION_FRONT_TOP_RIGHT,
            OrientationDetector.ORIENTATION_FRONT_RIGHT,
            OrientationDetector.ORIENTATION_FRONT_BOTTOM_RIGHT,
            OrientationDetector.ORIENTATION_FRONT_BOTTOM,
            OrientationDetector.ORIENTATION_FRONT_BOTTOM_LEFT,
            OrientationDetector.ORIENTATION_FRONT_LEFT,
            OrientationDetector.ORIENTATION_FRONT_TOP_LEFT,

            OrientationDetector.ORIENTATION_BACK_TOP,
            OrientationDetector.ORIENTATION_BACK_TOP_RIGHT,
            OrientationDetector.ORIENTATION_BACK_RIGHT,
            OrientationDetector.ORIENTATION_BACK_BOTTOM_RIGHT,
            OrientationDetector.ORIENTATION_BACK_BOTTOM,
            OrientationDetector.ORIENTATION_BACK_BOTTOM_LEFT,
            OrientationDetector.ORIENTATION_BACK_LEFT,
            OrientationDetector.ORIENTATION_BACK_TOP_LEFT
    };

    private final static int[] ORIENTATION_DEGREES = new int[]{
            0,
            45,
            90,
            135,
            180,
            225,
            270,
            315,
            360
    };
    private Matrix mBitmapMatrix;

    private Bitmap bitmapFront;
    private Bitmap bitmapBack;

    // to hold the front or the back picture
    private Bitmap bitmap;

    private Paint bitmapPaint;

    //TODO create rotation degree array for each orientation

    public MatchCanvasView(Context context) {
        this(context, null, 0);
    }

    public MatchCanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRandom = new Random();
        setWillNotDraw(false);
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));


        currentOrientation = ORIENTATIONS[0];

        mBitmapMatrix = new Matrix();
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Drawable d1 = ContextCompat.getDrawable(context, R.drawable.device_front);
        Drawable d2 = ContextCompat.getDrawable(context, R.drawable.device_back);
        bitmapFront = drawableToBitmap(d1);
        bitmapBack = drawableToBitmap(d2);

        bitmap = bitmapFront;

        mWidth = 1;
        mHeight = 1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        //int smallest = Math.min(w, h);
        //mCircleRadius = (smallest - (getPaddingBottom() + getPaddingTop())) /2;

    }

    public int randInt(int min, int max) {
        return mRandom.nextInt((max - min) + 1) + min;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO replace 300 magic number
        //canvas.drawCircle(mWidth / 2, mHeight / 2, mCircleRadius, mFillPaint);
        drawPhone(canvas);
    }

    private void drawPhone(Canvas canvas) {

        mBitmapMatrix.postRotate(
                ORIENTATION_DEGREES[currentOrientation % 8],
                bitmap.getWidth() / 2, bitmap.getHeight() / 2
        );
        mBitmapMatrix.postTranslate(mWidth / 2 - bitmap.getWidth() / 2, mHeight / 2 - bitmap
                .getHeight() / 2);
        canvas.drawBitmap(bitmap, mBitmapMatrix, bitmapPaint);
        mBitmapMatrix.reset();
    }

    private int getRandomOrientation() {
        return ORIENTATIONS[randInt(0, ORIENTATIONS.length - 1)];
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public boolean onOrientationChanged(final int newOrientation) {

        if (currentOrientation == newOrientation) {

            generateNewOrientation();
            postInvalidate();
            return true;
        }
        return false;
    }


    public void generateNewOrientation() {

        int randomOrientation = getRandomOrientation();
        while (currentOrientation == randomOrientation) {
            randomOrientation = getRandomOrientation();
        }
        currentOrientation = randomOrientation;

        if(currentOrientation < 8){

            bitmap = bitmapFront;
        }
        else bitmap = bitmapBack;
    }
}
