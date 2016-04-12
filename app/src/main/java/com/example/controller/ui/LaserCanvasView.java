package com.example.controller.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.controller.R;
import com.example.controller.controller.MatrixCalculator;
import com.example.controller.controller.OrientationDetector;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Hatem on 26-Mar-16.
 */
public class LaserCanvasView extends View implements MatrixCalculator.OrientationListener {

    private Paint mFillPaint;

    private int mWidth;
    private float laserX;
    private float laserY;

    private int mTargetRadius;
    private int targetRadiusSquared;

    private Paint mTargetPaint;
    private Paint mFallingTargetPaint;
    private Paint greyPaint;

    public static final int POSITION_X = 0;
    public static final int START_TIME = 1;
    public static final int DURATION = 2;
    public static final int MAX_HEIGHT = 3;
    public static final int SCALE = 4;

    private int mHeight;
    private static final String TAG = "CanvasView";

    private float time;
    private float[][] targets;
    private ArrayList<float[]> fallingTargets;

    private ArrayList<Integer> randomSequence;
    private int startTime;

    private float missedShotX;
    private float missedShotY;
    private float missedShotRadius;

    private Bitmap spiderBitmap;
    private Bitmap upsideDownSpiderBitmap;
    private Bitmap crosshairBitmap;

    private float fallingAcceleration;

    public LaserCanvasView(Context context) {
        this(context, null, 0);
    }

    public LaserCanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LaserCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(Color.BLACK);
        mFillPaint.setStrokeWidth(3);

        greyPaint = new Paint(mFillPaint);
        greyPaint.setColor(Color.GRAY);

        Drawable d1 = ContextCompat.getDrawable(context, R.drawable.spider);
        Drawable d2 = ContextCompat.getDrawable(context, R.drawable.spider_tilted);
        Drawable d3 = ContextCompat.getDrawable(context, R.drawable.crosshair);

        spiderBitmap = MatchCanvasView.drawableToBitmap(d1);
        upsideDownSpiderBitmap = MatchCanvasView.drawableToBitmap(d2);
        crosshairBitmap = MatchCanvasView.drawableToBitmap(d3);
        //TODO move the convert utility method outside of matchCanvasView

        mTargetPaint = new Paint(mFillPaint);
        mTargetPaint.setColor(ContextCompat.getColor(context, R.color.red_500));

        mFallingTargetPaint = new Paint(mFillPaint);
        mFallingTargetPaint.setColor(ContextCompat.getColor(context, R.color.grey_800));

        mTargetRadius = 40;
        targetRadiusSquared = mTargetRadius * mTargetRadius;

        mWidth = 1;
        mHeight = 1;

        targets = new float[12][5];
        fallingTargets = new ArrayList<>();
        randomSequence = new ArrayList<>();

        missedShotRadius = 5;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        laserX = w / 2;
        laserY = h / 2;

        // falls 1 screen height in the first second
        fallingAcceleration = mHeight;

        generateTargets();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        time += 0.016;

        // draw the missed shot
        canvas.drawCircle(missedShotX, missedShotY, missedShotRadius, greyPaint);

        // draw targets
        for (int i = 0; i < targets.length; i++) {

            float timePassed = time - targets[i][START_TIME];

            if (timePassed > 0) {

                float x = targets[i][POSITION_X];
                float y = (targets[i][DURATION] * timePassed - timePassed * timePassed) * targets[i][SCALE];

                canvas.drawLine(x, y, x, 0, mFillPaint);
                canvas.drawBitmap(spiderBitmap, x - spiderBitmap.getWidth() / 2, y - spiderBitmap
                        .getHeight() / 2, mFillPaint);
            }

            if (timePassed > targets[i][DURATION]) {

                updateTarget(i);
            }

        }


        // draw falling targets
        for (int i = 0; i < fallingTargets.size(); i++) {

            float timePassed = time - fallingTargets.get(i)[2];
            float yPosition = fallingTargets.get(i)[1] + timePassed * timePassed * fallingAcceleration;

            //canvas.drawCircle(fallingTargets.get(i)[0], yPosition, mTargetRadius,mFallingTargetPaint);
            canvas.drawBitmap(
                    upsideDownSpiderBitmap,
                    fallingTargets.get(i)[0] - upsideDownSpiderBitmap.getWidth() / 2,
                    yPosition - upsideDownSpiderBitmap.getHeight() / 2, mFillPaint
            );

            // if it falls beyond the bottom of the screen
            if (yPosition > mHeight + mTargetRadius) {

                fallingTargets.remove(i);
                i--;
            }
        }

        // draw the laser pointer
        canvas.drawBitmap(
                crosshairBitmap,
                laserX - crosshairBitmap.getWidth() / 2,
                laserY - crosshairBitmap.getHeight() / 2, mFillPaint
        );


    }

    @Override
    public void onOrientationChanged(int newOrientation) {
        //do nothing
    }

    public boolean shoot() {

        for (int i = 0; i < targets.length; i++) {

            float timePassed = time - targets[i][START_TIME];

            // you can't shoot targets that haven't been drawn yet
            if (timePassed > 0) {

                float x = targets[i][POSITION_X];
                float y = (targets[i][DURATION] * timePassed - timePassed * timePassed) *
                        targets[i][SCALE];

                // if you shoot close enough to a target
                if (Math.pow((laserX - x), 2) + Math.pow((laserY - y), 2) < targetRadiusSquared) {

                    fallingTargets.add(createFallingTarget(x, y));

                    // set a new target in its place
                    updateTarget(i);

                    //if you hit, hide the last missed shot
                    missedShotX = -10;
                    missedShotY = -10;

                    return true;
                } else {

                    // if you miss, show the position of the missed shot
                    missedShotX = laserX;
                    missedShotY = laserY;
                }
            }
        }
        return false;
    }

    @Override
    public void onLaserPointChanged(float[] point) {

        OrientationDetector.scaleLaserPoint(point, mWidth, mHeight);
        laserX = point[0];
        laserY = point[1];
    }


    public void generateTargets() {

        time = 0;
        startTime = 0;
        generateRandomSequence();

        for (int i = 0; i < targets.length; i++) {

            updateTarget(randomSequence.get(i));
        }

        missedShotX = -10;
        missedShotX = -10;
    }


    // generates a new random target
    private void updateTarget(int i) {

        startTime += 1.2;

        targets[i][POSITION_X] = (i + 0.5f) * mWidth / targets.length;

        targets[i][START_TIME] = startTime;
        targets[i][DURATION] = 3.5f + (float) Math.random();
        targets[i][MAX_HEIGHT] = mHeight / 5 * (2 * (float) Math.random() + 2);

        targets[i][SCALE] = targets[i][MAX_HEIGHT] * 4 / (targets[i][DURATION] * targets[i][DURATION]);
    }


    private float[] createFallingTarget(float x, float y) {

        float[] fallingTarget = new float[3];

        fallingTarget[0] = x;

        // start falling from
        fallingTarget[1] = y;

        fallingTarget[2] = time;

        return fallingTarget;
    }


    private void generateRandomSequence(){

        randomSequence.clear();

        for (int i = 0; i < targets.length; i++) {

            randomSequence.add(i);
        }

        Collections.shuffle(randomSequence);
    }
}
