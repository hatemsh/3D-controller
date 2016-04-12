package com.example.controller.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.controller.controller.MatrixCalculator;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private GLRenderer mRenderer;

    private static final String TAG = "MyGLSurfaceView";
    private MatrixCalculator matrixCalculator;

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GLRenderer getRenderer() {
        return mRenderer;
    }

    public MyGLSurfaceView(Context context) {
        super(context, null);
    }

    private void init(Context context) {
        matrixCalculator = new MatrixCalculator();
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new GLRenderer(context, matrixCalculator, this);

        setEGLConfigChooser(new CustomConfigChooser());


        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public MatrixCalculator getMatrixCalculator() {
        return matrixCalculator;
    }



    private final class CustomConfigChooser implements GLSurfaceView.EGLConfigChooser {

        private int[] mValue = new int[1];
        protected int mRedSize = 8;
        protected int mGreenSize = 8;
        protected int mBlueSize = 8;
        protected int mAlphaSize = 8;
        protected int mDepthSize = 16;
        protected int mStencilSize = 0;

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] configSpec = {
                    EGL10.EGL_RED_SIZE, mRedSize,
                    EGL10.EGL_GREEN_SIZE, mGreenSize,
                    EGL10.EGL_BLUE_SIZE, mBlueSize,
                    EGL10.EGL_ALPHA_SIZE, mAlphaSize,
                    EGL10.EGL_DEPTH_SIZE, mDepthSize,
                    EGL10.EGL_STENCIL_SIZE, mStencilSize,
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, 4,
                    EGL10.EGL_NONE
            };
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, configSpec, null, 0, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig1 failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                // Don't do anti-aliasing
                configSpec = new int[]{
                        EGL10.EGL_RED_SIZE, mRedSize,
                        EGL10.EGL_GREEN_SIZE, mGreenSize,
                        EGL10.EGL_BLUE_SIZE, mBlueSize,
                        EGL10.EGL_ALPHA_SIZE, mAlphaSize,
                        EGL10.EGL_DEPTH_SIZE, mDepthSize,
                        EGL10.EGL_STENCIL_SIZE, mStencilSize,
                        EGL10.EGL_RENDERABLE_TYPE, 4,
                        EGL10.EGL_NONE
                };

                if (!egl.eglChooseConfig(display, configSpec, null, 0, num_config)) {
                    throw new IllegalArgumentException("eglChooseConfig2 failed");
                }
                numConfigs = num_config[0];

                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig3 failed");
            }
            EGLConfig config = findConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        private EGLConfig findConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == mRedSize) && (g == mGreenSize) && (b == mBlueSize) && (a
                            == mAlphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config,
                                     int attribute,
                                     int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }
    }
}

