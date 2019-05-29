package com.maq.xprize.onecourse.hindi.glstuff;

import android.opengl.GLES20;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by alan on 16/05/16.
 */
public class GradientRect
{
    static final int BYTES_PER_FLOAT = 4;
    static int POSITION_COMPONENT_COUNT = 3;
    static int RGB_COMPONENT_COUNT = 4;
    static int STRIDE = (POSITION_COMPONENT_COUNT + RGB_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    float vertices[] = {
            -1,1,0,1,1,1,1,
            -1,-1,0,1,1,1,1,
            1,-1,0,1,1,1,1,
            -1,1,0,1,1,1,1
    };
    VertexArray vertexArray;

    public GradientRect()
    {
    }

    public static void fillOutRectVertexData(float vertices[],float l,float t,float r,float b,int stride)
    {
        int idx = 0;
        vertices[idx] = l;
        vertices[idx+1] = t;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = l;
        vertices[idx+1] = b;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = r;
        vertices[idx+1] = t;
        vertices[idx+2] = 0f;
        idx += stride;
        vertices[idx] = r;
        vertices[idx+1] = b;
        vertices[idx+2] = 0f;
    }

    public static void fillOutRectColourData(float vertices[],float rgb1[],float rgb2[],int stride)
    {
        int idx = POSITION_COMPONENT_COUNT;
        vertices[idx] = rgb1[0];
        vertices[idx+1] = rgb1[1];
        vertices[idx+2] = rgb1[2];
        vertices[idx+3] = rgb1[3];
        idx += stride;
        vertices[idx] = rgb2[0];
        vertices[idx+1] = rgb2[1];
        vertices[idx+2] = rgb2[2];
        vertices[idx+3] = rgb2[3];
        idx += stride;
        vertices[idx] = rgb1[0];
        vertices[idx+1] = rgb1[1];
        vertices[idx+2] = rgb1[2];
        vertices[idx+3] = rgb1[3];
        idx += stride;
        vertices[idx] = rgb2[0];
        vertices[idx+1] = rgb2[1];
        vertices[idx+2] = rgb2[2];
        vertices[idx+3] = rgb2[3];
    }

    public void draw(OBRenderer renderer, float l, float t, float r, float b,float col1[],float col2[])
    {
        fillOutRectVertexData(vertices,l,t,r,b,POSITION_COMPONENT_COUNT + RGB_COMPONENT_COUNT);
        fillOutRectColourData(vertices,col1,col2,POSITION_COMPONENT_COUNT + RGB_COMPONENT_COUNT);
        if (vertexArray == null)
            vertexArray = new VertexArray(vertices);
        else
            vertexArray.put(vertices);
        bindData((ColorShaderProgram) renderer.colourProgram);
        glBindTexture(GL_TEXTURE_2D,0);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
    }

    public void bindData(ColorShaderProgram shaderProgram)
    {
        vertexArray.setVertexAttribPointer(
                0,
                shaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                shaderProgram.getColorAttributeLocation(),
                RGB_COMPONENT_COUNT,
                STRIDE);
    }

}
