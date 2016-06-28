package org.onebillion.xprz.glstuff;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLUtils.texImage2D;

/**
 * Created by alan on 17/04/16.
 */
public class TextureRect
{
    static final int BYTES_PER_FLOAT = 4;
    static int POSITION_COMPONENT_COUNT = 3;
    static int UV_COMPONENT_COUNT = 2;
    static int STRIDE = (POSITION_COMPONENT_COUNT + UV_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    float vertices[] = {
            -1,1,0,0,0,
            -1,-1,0,0,1,
            1,-1,0,1,1,
            -1,1,0,0,0
    };
    public float uvLeft,uvTop,uvRight,uvBottom;
    VertexArray vertexArray;

    public TextureRect()
    {
        uvLeft = uvTop = 0;
        uvRight = uvBottom = 1;
    }

    public void setUVs(float uvl,float uvt,float uvr,float uvb)
    {
        uvLeft = uvl;
        uvTop = uvt;
        uvRight = uvr;
        uvBottom = uvb;
    }
    public void draw(OBRenderer renderer, float l, float t, float r, float b, Bitmap bitmap)
    {
        fillOutRectVertexData(vertices,l,t,r,b,POSITION_COMPONENT_COUNT + UV_COMPONENT_COUNT);
        fillOutRectTextureData(vertices,uvLeft,uvTop,uvRight,uvBottom,POSITION_COMPONENT_COUNT + UV_COMPONENT_COUNT);
        if (vertexArray == null)
            vertexArray = new VertexArray(vertices);
        else
            vertexArray.put(vertices);
        bindData((TextureShaderProgram) renderer.textureProgram);
        glBindTexture(GL_TEXTURE_2D, renderer.textureObjectIds[0]);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);

    }

    public void bindData(TextureShaderProgram textureProgram)
    {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                UV_COMPONENT_COUNT,
                STRIDE);
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
    public static void fillOutRectTextureData(float vertices[],float uvl,float uvt,float uvr,float uvb,int stride)
    {
        int idx = POSITION_COMPONENT_COUNT;
        vertices[idx] = uvl;
        vertices[idx+1] = uvt;
        idx += stride;
        vertices[idx] = uvl;
        vertices[idx+1] = uvb;
        idx += stride;
        vertices[idx] = uvr;
        vertices[idx+1] = uvt;
        idx += stride;
        vertices[idx] = uvr;
        vertices[idx+1] = uvb;
    }

}
