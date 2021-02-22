/*
*	© Copyright DzzD, Bruno Augier 2013-2021 (bruno.augier@dzzd.net)
*	 This file is part of TIGL.
*
*    TIGL is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    any later version.
*
*    TIGL is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*	 along with TIGL.  If not, see <https://www.gnu.org/licenses/>
*/

package fr.dzzd.tigl;


import android.graphics.Bitmap;
import java.util.HashMap;
import android.util.Log;
import java.util.*;
import java.nio.*;


public class GLShape extends GLEntity
{

    public int textureHandle;

    /*
     * Shape vertices 
     */
    public float[] vertices;
    
    /*
     * Texture UV coordinates 
     */
    public float[] uvs;

    
    /*
     * Texture size, actually bitmap size used for this sprite
     */
    private int textureWidth;
    private int textureHeight;

    
    public HashMap<String, Object> options;

    /*
    * Construct a new Shape
    */
    public GLShape(HashMap<String,Object> options)
    {
        super(options);
        this.type = GL_SHAPE;

        this.textureHandle = -1;
        this.textureWidth = -1;
        this.textureHeight = -1;
        this.options = new HashMap<String, Object>();
        try 
        {
            String url = (String)options.get("url");
            Bitmap bitmap = BitmapCache.load(url);
            this.options.put("textureUid",url);
            this.options.put("textureBitmap",bitmap);
            this.options.put("textureTile",options.get("tile"));
            this.textureWidth = bitmap.getWidth();
            this.textureHeight = bitmap.getHeight();
        }
        catch( Exception e)
        {
            Log.e("TIGL", "GLShape: BitmapCache.load(" + options.get("url") + ") : " + e, e);
        }


        Object[] verticesTmp = (Object[])options.get("vertices");
        Object[] uvsTmp = (Object[])options.get("uvs");
        float[] verticesIn = new float[verticesTmp.length];
        float[] uvsIn = new float[uvsTmp.length];
        for(int n = 0; n < verticesTmp.length / 2; n++)
        {
            verticesIn[n * 2] = Properties.propertyToFloat(verticesTmp[n * 2]);
            verticesIn[n * 2 + 1] = Properties.propertyToFloat(verticesTmp[n * 2 + 1]);
            uvsIn[n * 2] = Properties.propertyToFloat(uvsTmp[n * 2]) / this.textureWidth;
            uvsIn[n * 2 + 1] = Properties.propertyToFloat(uvsTmp[n * 2 + 1]) / this.textureHeight;
        }
       
        this.setVertices(verticesIn, uvsIn);
    }


    static float[] matrix3x3 = new float[9];
    
    /*
     * Return an unique identifier for this entity material/draw
     *  could be a combinaison of texture, opacity, effects, layer number, etc...
     * This identifier is used to know wich entities/sprite can be drawn together
     */
    @Override
    public Integer getMaterialUid()
    {
        int uid = super.getMaterialUid();
        return Integer.valueOf(uid | (this.textureHandle<<16));
    }

    public void setVertices(float[] newVertices, float newUvs[])
    {
        PolygonTriangulate pt = new PolygonTriangulate(newVertices, newUvs);
        this.vertices = pt.verticesOut;
        this.uvs = pt.uvsOut;
        
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for(int n = 0; n < newVertices.length; n+=2)
        {
            if( newVertices[n] < minX) minX = newVertices[n];
            if( newVertices[n] > maxX) maxX = newVertices[n];
            if( newVertices[n + 1] < minY) minY = newVertices[n + 1];
            if( newVertices[n + 1] > maxY) maxY = newVertices[n + 1];
        }
        if(newVertices.length == 0)
        {
            minX = minY = maxX = maxY = 0;
        }
        this.width = maxX - minX;
        this.height = maxY - minY;
    }

    @Override
    public void prepareDrawing()
    {
        if(this.textureHandle == -1)
        {
            this.textureHandle = GLTextureCache.create(this.options);
        }
    }

    @Override
    public void drawSingle()
    {
        GLShader.setBuffersMinimalCapacity(vertices.length);

        this.matrix.getValues(matrix3x3);

        float xtx = matrix3x3[0];
        float xty = matrix3x3[1];
        float xtz = matrix3x3[2];

        float ytx = matrix3x3[3];
        float yty = matrix3x3[4];
        float ytz = matrix3x3[5];

        for(int n = 0; n < vertices.length; n++)
        {
            int indexV = n * 3;
            GLShader.xTransforms[indexV + 0] = xtx;
            GLShader.xTransforms[indexV + 1] = xty;
            GLShader.xTransforms[indexV + 2] = xtz;

            GLShader.yTransforms[indexV + 0] = ytx;
            GLShader.yTransforms[indexV + 1] = yty;
            GLShader.yTransforms[indexV + 2] = ytz;
        }


        GLShader.xTransformsBuff.clear();
        GLShader.xTransformsBuff.put(GLShader.xTransforms, 0, this.vertices.length * 3);
        GLShader.xTransformsBuff.flip();

        GLShader.yTransformsBuff.clear();
        GLShader.yTransformsBuff.put(GLShader.yTransforms, 0, this.vertices.length * 3);
        GLShader.yTransformsBuff.flip();
        
        GLShader.verticesBuff.clear();
        GLShader.verticesBuff.put(this.vertices);
        GLShader.verticesBuff.flip();

        GLShader.uvsBuff.clear();
        GLShader.uvsBuff.put(this.uvs);
        GLShader.uvsBuff.flip();
        
        GLShader.drawTexture(GLShader.xTransformsBuff, GLShader.yTransformsBuff, GLShader.verticesBuff, GLShader.uvsBuff, this.textureHandle, this.vertices.length / 6, false);
        this.lastDrawOrder = this.getScene().currentDrawCount++;
    }


}