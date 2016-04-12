package com.example.controller.gl;

import com.example.controller.gl.glUtil.ShaderHelper;

import static android.opengl.GLES20.glBindAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class GLProgram {

    public final int program;
    private final int uMatrixLocation;
    private final int uViewMatrixLocation;

    public GLProgram(String vertexShaderSource, String fragmentShaderSource) {

        program = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
        glBindAttribLocation(program, 0, "a_Position");
        glBindAttribLocation(program, 1, "a_Normal");
        ShaderHelper.completeLinking(program);

        uMatrixLocation = glGetUniformLocation(program, "u_Matrix");
        uViewMatrixLocation = glGetUniformLocation(program, "u_View_Matrix");
    }

    public void useProgram() {

        glUseProgram(program);

    }

    public void setUniforms(float[] projection, float[] viewMatrix) {

        glUniformMatrix4fv(uMatrixLocation, 1, false, projection, 0);
        glUniformMatrix4fv(uViewMatrixLocation, 1, false, viewMatrix, 0);
    }

    public int getPositionAttributeLocation() {

        return 0;
    }

    public int getNormalAttributeLocation() {

        return 1;
    }

}
