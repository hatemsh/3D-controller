package com.example.controller.gl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.support.v4.content.ContextCompat;

import com.example.controller.R;
import com.example.controller.controller.MatrixCalculator;
import com.example.controller.gl.glUtil.TextResourceReader;
import com.example.controller.gl.glUtil.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glClearDepthf;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDepthRangef;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class GLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "GLRenderer";

    private final float[] vertices;
    private final float[] normals;

    private float[] toWorldMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    private float[] mvpMatrix = new float[16];
    private float[] bgColor;


    private GLProgram program;

    public boolean offset;

    private MatrixCalculator matrixCalculator;
    private final MyGLSurfaceView surfaceView;
    private float[] worldXRotation = new float[16];
    private float[] rotationMatrix = new float[16];

    public GLRenderer(Context context, MatrixCalculator matrixCalculator, MyGLSurfaceView surfaceView) {

        this.matrixCalculator = matrixCalculator;
        this.surfaceView = surfaceView;
        vertices = ModelData.chairFancyVerts;
        normals = ModelData.chairFancyNormals;
        int color = ContextCompat.getColor(context, R.color.page2Primary);
        bgColor = new float[]{
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f
        };

    }



    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        glClearColor(bgColor[0], bgColor[1], bgColor[2], 1.0f);
        glClearDepthf(1);

        program = new GLProgram(
                TextResourceReader.getReader().readTextFileFromResource(R.raw.vertex_shader),
                TextResourceReader.getReader().readTextFileFromResource(R.raw.fragment_shader));

        program.useProgram();


        VertexBuffer vertexBuffer = new VertexBuffer(vertices.length + normals.length);

        vertexBuffer.setVertexAttributePointer(0, program.getPositionAttributeLocation(), 3, 12);
        vertexBuffer.setVertexAttributePointer(vertices.length * 4, program.getNormalAttributeLocation(), 3, 12);


        FloatBuffer bufferToGPU = ByteBuffer
                .allocateDirect((vertices.length + normals.length) * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .put(normals);
        bufferToGPU.position(0);

        vertexBuffer.updateBuffer(0, bufferToGPU);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {

        glViewport(0, 0, width, height);

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);
        glDepthRangef(0, 1);

        toWorldMatrix[14] = -3f;

        toWorldMatrix[0] = 3f;
        toWorldMatrix[5] = 3f;
        toWorldMatrix[10] = 1f;
        toWorldMatrix[15] = 1f;

        perspectiveM(projectionMatrix, 0, 90, width / (float) height, 2, 10);

        surfaceView.requestRender();
    }

    public void onDrawFrame(GL10 unused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        rotationMatrix = matrixCalculator.getRotationMatrix();

        multiplyMM(worldXRotation, 0, toWorldMatrix, 0, rotationMatrix, 0);
        multiplyMM(mvpMatrix, 0, projectionMatrix, 0, worldXRotation, 0);

        program.setUniforms(mvpMatrix, worldXRotation);
        glDrawArrays(GL_TRIANGLES, 0, vertices.length / 3);
    }
}