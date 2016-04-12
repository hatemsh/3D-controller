package com.example.controller.cast;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import com.example.controller.R;
import com.example.controller.controller.ControllerSensorManager;
import com.example.controller.controller.MatrixCalculator;
import com.example.controller.gl.MyGLSurfaceView;
import com.example.controller.gl.glUtil.TextResourceReader;
import com.google.android.gms.cast.CastPresentation;

/**
 * Created by Hatem on 11-Apr-16.
 */
public class ModelViewerPresentation extends CastPresentation implements CastPresentationController {
    private static final String TAG = "FirstScreenPresentation";
    private MyGLSurfaceView surfaceView;
    private MatrixCalculator matrixCalculator;
    private ControllerSensorManager controllerSensorManager;
    private TextView mOverlayTextView;

    public ModelViewerPresentation(Context context, Display display) {
        super(context, display, R.style.AppTheme_NoActionBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextResourceReader.getReader().setContext(getContext());
        setContentView(R.layout.tv_layout_viewer_demo);
        mOverlayTextView = (TextView) findViewById(R.id.tip_text_view);
        surfaceView = (MyGLSurfaceView) findViewById(R.id.surface_view);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        matrixCalculator = surfaceView.getMatrixCalculator();
        controllerSensorManager = new ControllerSensorManager(getContext(), matrixCalculator);
        controllerSensorManager.start();

//            // Use TrueType font to get best looking text on remote display
//            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
//            titleTextView.setTypeface(typeface);
    }

    @Override
    protected void onStop() {
        super.onStop();
        controllerSensorManager.stop();
    }

    @Override
    public void onButtonClick() {
        if (matrixCalculator != null) {
            matrixCalculator.offset();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                    mOverlayTextView.setText(R.string.model_viewer_tip);
                }
            }, 500);
        }
    }

    @Override
    public void onSecondButtonClick() {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }
}
