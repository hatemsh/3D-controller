package com.example.controller.gl.glUtil;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class ShaderHelper {


    private static int compileShader(int type, String shaderCode){

        final int shaderObjectId = glCreateShader(type);

        if(shaderObjectId == 0) {
            return 0;
        }

        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        if(compileStatus[0] == 0){

            glDeleteShader(shaderObjectId);
            return 0;
        }

        return shaderObjectId;
    }


    public static int prepareToLink(int vertexShaderId, int fragmentShaderId){

        final int programObjectId = glCreateProgram();

        if(programObjectId == 0){
            return 0;
        }

        glAttachShader(programObjectId,vertexShaderId);
        glAttachShader(programObjectId,fragmentShaderId);

        return programObjectId;
    }

    public static int completeLinking(int programObjectId){

        glLinkProgram(programObjectId);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus,0);

        if(linkStatus[0] == 0){

            glDeleteProgram(programObjectId);
            return 0;
        }

        return programObjectId;
    }


    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource){

        int unlinkedProgram;

        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

        unlinkedProgram = prepareToLink(vertexShader,fragmentShader);

        return unlinkedProgram;
    }

}