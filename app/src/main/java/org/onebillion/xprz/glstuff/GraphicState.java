package org.onebillion.xprz.glstuff;

/**
 * Created by alan on 01/05/16.
 */
public class GraphicState
{
    public float[] projectionMatrix;
    public float[] modelViewMatrix;
    public GraphicState(float projMatrix[],float modelMatrix[])
    {
        projectionMatrix = projMatrix.clone();
        modelViewMatrix = modelMatrix.clone();
    }
}
