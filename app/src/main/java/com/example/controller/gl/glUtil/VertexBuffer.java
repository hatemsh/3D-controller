package com.example.controller.gl.glUtil;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glBufferSubData;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class VertexBuffer {

    private final int bufferId;

    public VertexBuffer(int numOfFloats) {

        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);
        bufferId = buffers[0];

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);

        glBufferData(GL_ARRAY_BUFFER, numOfFloats * 4, null, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void setVertexAttributePointer(int dataOffset, int attributeLocation,int componentCount, int stride) {

        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, dataOffset);
        glEnableVertexAttribArray(attributeLocation);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }


    public void updateBuffer(int offset, FloatBuffer data){

        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferSubData(GL_ARRAY_BUFFER, offset, data.capacity() * 4, data);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
