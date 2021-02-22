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
import android.graphics.Matrix;
import java.util.*;
import java.nio.*;


public class GLText extends GLEntity
{
    public int textureHandle;

    /*
     * Sprite vertices 
     */
    public float[] vertices;
    
    /*
     * Texture UV coordinates 
     */
    public float[] uvs;
    
    /*
     * Current text
     */
     private String text;

     
    /*
     * Font size
     */
     private float fontSize;

    /*
    * BitmapFont
    */
    private BitmapFont font;

    
    public HashMap<String, Object> options;

    public int color;
    public int outlineColor;
       
    /*
    * Construct a new GLText
    */
    public GLText(HashMap<String,Object> options)
    {
        super(options);
        this.type = GL_TEXT;
        this.textureHandle = -1;

        this.fontSize = options.get("fontSize") != null ? Properties.propertyToFloat(options.get("fontSize")) : -1;
        this.color = options.get("color") != null ? Properties.propertyToColor(options.get("color")) : 0x88888888;
        this.outlineColor = options.get("outlineColor") != null ? Properties.propertyToColor(options.get("outlineColor")) : 0x0;

        this.options = new HashMap<String, Object>();
        try 
        {
            String fontUrl = (String)options.get("font");
            if(fontUrl == null)
            {
                fontUrl = "Resources/bitmapfont/Arial.fnt";
                Log.e("TIGL","GLText no font specified, trying Resources/bitmapfont/Arial.fnt");
            }
            this.font = BitmapFont.load(fontUrl);
            Bitmap bitmap = BitmapCache.load(this.font.textureFileName);
            this.options.put("textureUid", this.font.textureFileName);
            this.options.put("textureBitmap", bitmap);
            this.options.put("textureTile", false);
        }
        catch( Exception e)
        {
            Log.e("TIGL", "GLText: BitmapFont.load(" + options.get("font") + ") : " + e, e);
        }

        
        if(this.fontSize == -1)
        {
            this.fontSize = this.font.fontSize;
        }

        this.setText((String)options.get("text"));
    }

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

       
    @Override
    public void prepareDrawing()
    {
        if(this.textureHandle == -1)
        {
            this.textureHandle = GLTextureCache.create(this.options);
        }
    }

    public void setText(String text)
    {
        this.text = text;
        this.uvs = this.font.getUvs(this.text);
        this.vertices = this.font.getVertices(this.text);
        float ratio = this.fontSize / this.font.fontSize;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for(int n = 0; n < this.vertices.length; n+=2)
        {
            this.vertices[n] *= ratio;
            this.vertices[n + 1] *= ratio;
            if( this.vertices[n] < minX) minX = this.vertices[n];
            if( this.vertices[n] > maxX) maxX = this.vertices[n];
            if( this.vertices[n + 1] < minY) minY = this.vertices[n + 1];
            if( this.vertices[n + 1] > maxY) maxY = this.vertices[n + 1];
        }
        if(this.vertices.length == 0)
        {
            minX = minY = maxX = maxY = 0;
        }
        this.width = maxX - minX;
        this.height = maxY - minY;
    }

    static int tmpBufferCapacity = 1;
    static float[] matrix3x3 = new float[9];
    
    
    @Override
    public void drawSingle()
    {
        int charCount = this.text.length();
        if(charCount == 0)
        {
            return;
        }

        GLShader.setBuffersMinimalCapacity(charCount * 6);

        this.matrix.getValues(matrix3x3);
        float xtx = matrix3x3[0];
        float xty = matrix3x3[1];
        float xtz = matrix3x3[2];
        float ytx = matrix3x3[3];
        float yty = matrix3x3[4];
        float ytz = matrix3x3[5];

        for(int c = 0; c < charCount; c++)
        {
            int indexC = c * 3 * 6;
            for(int v = 0; v < 6; v++)
            {
                int indexV = indexC + v * 3;
                GLShader.xTransforms[indexV + 0] = xtx;
                GLShader.xTransforms[indexV + 1] = xty;
                GLShader.xTransforms[indexV + 2] = xtz;

                GLShader.yTransforms[indexV + 0] = ytx;
                GLShader.yTransforms[indexV + 1] = yty;
                GLShader.yTransforms[indexV + 2] = ytz;
            }
        }

        for(int v = 0; v < 6 * charCount; v++)
        {
            GLShader.colors[v] = this.color & 0xFF00FF00 | ((this.color & 0xFF0000) >> 16) | ((this.color & 0xFF) << 16);
            GLShader.outlineColors[v] = this.outlineColor & 0xFF00FF00 | ((this.outlineColor & 0xFF0000) >> 16) | ((this.outlineColor & 0xFF) << 16);
        }

        GLShader.xTransformsBuff.clear();
        GLShader.xTransformsBuff.put(GLShader.xTransforms, 0, charCount * 18);
        GLShader.xTransformsBuff.flip();

        GLShader.yTransformsBuff.clear();
        GLShader.yTransformsBuff.put(GLShader.yTransforms, 0, charCount * 18);
        GLShader.yTransformsBuff.flip();
        
        GLShader.verticesBuff.clear();
        GLShader.verticesBuff.put(this.vertices);
        GLShader.verticesBuff.flip();

        GLShader.uvsBuff.clear();
        GLShader.uvsBuff.put(this.uvs);
        GLShader.uvsBuff.flip();
        
        GLShader.outlineColorsBuff.clear();
        GLShader.outlineColorsBuff.put(GLShader.outlineColors, 0, charCount * 6);
        GLShader.outlineColorsBuff.flip();

        GLShader.colorsBuff.clear();
        GLShader.colorsBuff.put(GLShader.colors, 0, charCount * 6);
        GLShader.colorsBuff.flip();
        
        GLShader.drawFont(this.textureHandle, charCount);

        this.lastDrawOrder = this.getScene().currentDrawCount++;
    }

    
    @Override
    public void drawBatch(ArrayList<GLEntity> entities)
    {
        /*
         * Compute number of char
         */
        int totalCharCount = 0;
        Iterator<GLEntity> childIterator = entities.iterator();
        while (childIterator.hasNext()) 
        {
            GLText glText = (GLText)childIterator.next();
            totalCharCount += glText.text.length();
        }
        GLShader.setBuffersMinimalCapacity(totalCharCount * 6);

        int charStart = 0;
        childIterator = entities.iterator();
        while (childIterator.hasNext()) 
        {
            GLText glText = (GLText)childIterator.next();
            int charCount = glText.text.length();
            if(charCount == 0)
            {
                continue;
            }
            glText.lastDrawOrder = glText.getScene().currentDrawCount++;

            glText.matrix.getValues(matrix3x3);
            float xtx = matrix3x3[0];
            float xty = matrix3x3[1];
            float xtz = matrix3x3[2];
            float ytx = matrix3x3[3];
            float yty = matrix3x3[4];
            float ytz = matrix3x3[5];

            for(int c = 0; c < charCount; c++)
            {
                int indexMatrixStart = c * 3 * 6 + charStart * 18;
                int indexVertexStart = c * 2 * 6 + charStart * 12;
                int indexUvStart = c * 2 * 6 + charStart * 12;

                for(int v = 0; v < 6; v++)
                {
                    int indexMatrix = indexMatrixStart + v * 3;
                    GLShader.xTransforms[indexMatrix + 0] = xtx;
                    GLShader.xTransforms[indexMatrix + 1] = xty;
                    GLShader.xTransforms[indexMatrix + 2] = xtz;

                    GLShader.yTransforms[indexMatrix + 0] = ytx;
                    GLShader.yTransforms[indexMatrix + 1] = yty;
                    GLShader.yTransforms[indexMatrix + 2] = ytz;

                    int indexVertex = indexVertexStart + v * 2;
                    GLShader.vertices[indexVertex] = glText.vertices[c * 12 + v * 2];
                    GLShader.vertices[indexVertex + 1] = glText.vertices[c * 12 + v * 2 + 1];
                    
                    int indexUv = indexUvStart + v * 2;
                    GLShader.uvs[indexUv] = glText.uvs[c * 12 + v * 2];
                    GLShader.uvs[indexUv + 1] = glText.uvs[c * 12 + v * 2 + 1];
                }
            }


            for(int c = 0; c < 6 * charCount; c++)
            {
                GLShader.colors[c + charStart * 6] = glText.color & 0xFF00FF00 | ((glText.color & 0xFF0000) >> 16) | ((glText.color & 0xFF) << 16);
                GLShader.outlineColors[c + charStart * 6] = glText.outlineColor & 0xFF00FF00 | ((glText.outlineColor & 0xFF0000) >> 16) | ((glText.outlineColor & 0xFF) << 16);
            }

            charStart += charCount;
        }
        
        if(totalCharCount == 0)
        {
            return;
        }

        GLShader.xTransformsBuff.clear();
        GLShader.xTransformsBuff.put(GLShader.xTransforms, 0, totalCharCount * 18);
        GLShader.xTransformsBuff.flip();

        GLShader.yTransformsBuff.clear();
        GLShader.yTransformsBuff.put(GLShader.yTransforms, 0, totalCharCount * 18);
        GLShader.yTransformsBuff.flip();

        GLShader.verticesBuff.clear();
        GLShader.verticesBuff.put(GLShader.vertices, 0, totalCharCount * 12);
        GLShader.verticesBuff.flip();

        GLShader.uvsBuff.clear();
        GLShader.uvsBuff.put(GLShader.uvs, 0, totalCharCount * 12);
        GLShader.uvsBuff.flip();
        
        GLShader.outlineColorsBuff.clear();
        GLShader.outlineColorsBuff.put(GLShader.outlineColors, 0, totalCharCount * 6);
        GLShader.outlineColorsBuff.flip();

        GLShader.colorsBuff.clear();
        GLShader.colorsBuff.put(GLShader.colors, 0, totalCharCount * 6);
        GLShader.colorsBuff.flip();
        
        GLShader.drawFont(this.textureHandle, totalCharCount);
    }
            
}


