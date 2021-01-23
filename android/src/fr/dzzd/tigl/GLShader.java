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

import android.util.Log;
import android.opengl.GLES20;
import android.graphics.Matrix;
import java.lang.System;
import android.util.*;
import java.nio.*;


public class GLShader 
{
    public static int minimalBuffersCapacity = 0;
    public static float[] xTransforms;
    public static float[] yTransforms;
    public static float[] vertices;
    public static float[] uvs;
    public static int[] colors;
    public static int[] outlineColors;
    public static FloatBuffer xTransformsBuff;
    public static FloatBuffer yTransformsBuff;
    public static FloatBuffer verticesBuff;
    public static FloatBuffer uvsBuff;
    public static IntBuffer colorsBuff;
    public static IntBuffer outlineColorsBuff;
   
    public static void setBuffersMinimalCapacity(int verticesCount)
    {
        if(verticesCount > minimalBuffersCapacity)
        {
            minimalBuffersCapacity = verticesCount;
            xTransforms = new float[minimalBuffersCapacity * 3];
            yTransforms = new float[minimalBuffersCapacity * 3];
            vertices = new float[minimalBuffersCapacity * 2];
            uvs = new float[minimalBuffersCapacity  * 2];
            colors = new int[minimalBuffersCapacity];
            outlineColors = new int[minimalBuffersCapacity];
            xTransformsBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * 3 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            yTransformsBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * 3 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            verticesBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            uvsBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            colorsBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
            outlineColorsBuff = ByteBuffer.allocateDirect(minimalBuffersCapacity * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();

        }
    }

    /*
     * Initialize all OpenGL programs shaders
     */
    public final static void initShaders()
    {
        
        /*
         * Initialize texture OpenGL program
         */
        progTexture=GLES20.glCreateProgram();
        GLES20.glAttachShader(progTexture, loadShader(GLES20.GL_VERTEX_SHADER, vShaderTexture));
       	GLES20.glAttachShader(progTexture, loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderTexture));
        GLES20.glLinkProgram(progTexture);
        progTextureXTransform = GLES20.glGetAttribLocation(progTexture, "xTransform");
        progTextureYTransform = GLES20.glGetAttribLocation(progTexture, "yTransform");
        progTextureVertices = GLES20.glGetAttribLocation(progTexture, "vertices");
        progTextureUvs = GLES20.glGetAttribLocation(progTexture, "textureUvs");

  
        /*
         * Initialize bitmap font OpenGL program
         */
        progFont=GLES20.glCreateProgram();
        GLES20.glAttachShader(progFont, loadShader(GLES20.GL_VERTEX_SHADER, vShaderFont));
       	GLES20.glAttachShader(progFont, loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderFont));
        GLES20.glLinkProgram(progFont);
        progFontXTransform = GLES20.glGetAttribLocation(progFont, "xTransform");
        progFontYTransform = GLES20.glGetAttribLocation(progFont, "yTransform");
        progFontVertices = GLES20.glGetAttribLocation(progFont, "vertices");
        progFontUvs = GLES20.glGetAttribLocation(progFont, "textureUvs");
        progFontColors = GLES20.glGetAttribLocation(progFont, "colors");
        // progFontOutlineColors = GLES20.glGetAttribLocation(progFont, "outlineColors");

        
        progFontOutline=GLES20.glCreateProgram();
        GLES20.glAttachShader(progFontOutline, loadShader(GLES20.GL_VERTEX_SHADER, vShaderFontOutline));
       	GLES20.glAttachShader(progFontOutline, loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderFontOutline));
        GLES20.glLinkProgram(progFontOutline);
        progFontOutlineXTransform = GLES20.glGetAttribLocation(progFontOutline, "xTransform");
        progFontOutlineYTransform = GLES20.glGetAttribLocation(progFontOutline, "yTransform");
        progFontOutlineVertices = GLES20.glGetAttribLocation(progFontOutline, "vertices");
        progFontOutlineUvs = GLES20.glGetAttribLocation(progFontOutline, "textureUvs");
        progFontOutlineColors = GLES20.glGetAttribLocation(progFontOutline, "colors");
        // progFontOutlineColors = GLES20.glGetAttribLocation(progFont, "outlineColors");


    }

    /*
    * Texture shaders & program
    */
    private final static String vShaderTexture =
        "attribute vec3 xTransform;" +
        "attribute vec3 yTransform;" +
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(dot(vec3(vertices,1.0), xTransform), dot(vec3(vertices,1.0), yTransform), 0.0,1.0) ;" +
        "  textureUv = textureUvs;" +
        "}";

    private final static String fShaderTexture =
        "precision mediump float;" +
        "uniform sampler2D u_Texture;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +    
        "   gl_FragColor = texture2D(u_Texture, textureUv);" +   //+vec4(0.1,0.1,0.1,0.1);
        "}";


    public static int progTexture = -1;
    private static int progTextureVertices;
    private static int progTextureUvs;
    private static int progTextureXTransform;
    private static int progTextureYTransform;

    public final static void drawTexture(FloatBuffer xTransforms, FloatBuffer yTransforms, FloatBuffer vertices, FloatBuffer textureUvs, int textureHandle, int count, boolean triangleFan)
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progTexture);
        GLES20.glEnableVertexAttribArray(progTextureXTransform);
        GLES20.glVertexAttribPointer(progTextureXTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, xTransforms);
        GLES20.glEnableVertexAttribArray(progTextureYTransform);
        GLES20.glVertexAttribPointer(progTextureYTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, yTransforms);
        GLES20.glEnableVertexAttribArray(progTextureVertices);
        GLES20.glVertexAttribPointer(progTextureVertices, 2, GLES20.GL_FLOAT, false,  2*Float.BYTES, vertices);
        GLES20.glEnableVertexAttribArray(progTextureUvs);
        GLES20.glVertexAttribPointer(progTextureUvs, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, textureUvs);
      
        if(triangleFan)
        {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4 * count); //Faster 10%
        }
        else
        {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * count);
        }
        GLES20.glDisableVertexAttribArray(progTextureXTransform);
        GLES20.glDisableVertexAttribArray(progTextureYTransform);
        GLES20.glDisableVertexAttribArray(progTextureVertices);
        GLES20.glDisableVertexAttribArray(progTextureUvs);

    }


    /*
    * Font shaders & program
    */
    private final static String vShaderFont =
        "attribute vec3 xTransform;" +
        "attribute vec3 yTransform;" +
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "attribute vec4 colors;" +
        "varying vec2 textureUv;" +
        "varying vec4 color;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(dot(vec3(vertices,1.0), xTransform), dot(vec3(vertices,1.0), yTransform), 0.0,1.0) ;" +
        "  textureUv = textureUvs;" +
        "  color = colors / 255.0;" +
        "}";

    private final static String fShaderFont =
        "precision mediump float;" +
        "uniform sampler2D u_Texture;" +
        "varying vec2 textureUv;" +
        "varying vec4 color;" +
        "void main()" +
        "{" +   
        "   vec4 textureColor =   texture2D(u_Texture, textureUv);" +
        // "   if(textureColor.r>0.5) " +
        // "   { " +
        "       gl_FragColor = vec4(color.rgb, textureColor.r);" +   //+vec4(0.1,0.1,0.1,0.1);
        // "   }else" +
        // "   {" +
        // "       gl_FragColor = vec4(outlineColor.rgb, textureColor.g);" +   //+vec4(0.1,0.1,0.1,0.1);
        // "   }" +
        "}";


    private final static String vShaderFontOutline =
        "attribute vec3 xTransform;" +
        "attribute vec3 yTransform;" +
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "attribute vec4 colors;" +
        "varying vec2 textureUv;" +
        "varying vec4 color;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(dot(vec3(vertices,1.0), xTransform), dot(vec3(vertices,1.0), yTransform), 0.0,1.0) ;" +
        "  textureUv = textureUvs;" +
        "  color = colors / 255.0;" +
        "}";

    private final static String fShaderFontOutline =
        "precision mediump float;" +
        "uniform sampler2D u_Texture;" +
        "varying vec2 textureUv;" +
        "varying vec4 color;" +
        "void main()" +
        "{" +   
        "   vec4 textureColor =   texture2D(u_Texture, textureUv);" +
        "   gl_FragColor = vec4(color.rgb, textureColor.g);" +
        "}";

    public static int progFont = -1;
    private static int progFontVertices;
    private static int progFontUvs;
    private static int progFontXTransform;
    private static int progFontYTransform;
    private static int progFontColors;

    public static int progFontOutline = -1;
    private static int progFontOutlineVertices;
    private static int progFontOutlineUvs;
    private static int progFontOutlineXTransform;
    private static int progFontOutlineYTransform;
    private static int progFontOutlineColors;


    public final static void drawFont(int textureHandle, int charCount)
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progFontOutline);
        GLES20.glEnableVertexAttribArray(progFontOutlineXTransform);
        GLES20.glVertexAttribPointer(progFontOutlineXTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, xTransformsBuff);
        GLES20.glEnableVertexAttribArray(progFontOutlineYTransform);
        GLES20.glVertexAttribPointer(progFontOutlineYTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, yTransformsBuff);
        GLES20.glEnableVertexAttribArray(progFontOutlineVertices);
        GLES20.glVertexAttribPointer(progFontOutlineVertices, 2, GLES20.GL_FLOAT, false,  2*Float.BYTES, verticesBuff);
        GLES20.glEnableVertexAttribArray(progFontOutlineUvs);
        GLES20.glVertexAttribPointer(progFontOutlineUvs, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, uvsBuff);
        // GLES20.glEnableVertexAttribArray(progFontOutlineColors);
        // GLES20.glVertexAttribPointer(progFontOutlineColors, 4, GLES20.GL_UNSIGNED_BYTE, false, 4 * Byte.BYTES, colorsBuff);
        GLES20.glEnableVertexAttribArray(progFontOutlineColors);
        GLES20.glVertexAttribPointer(progFontOutlineColors, 4, GLES20.GL_UNSIGNED_BYTE, false, 4 * Byte.BYTES, outlineColorsBuff);
      
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * charCount);
        
        GLES20.glDisableVertexAttribArray(progFontOutlineXTransform);
        GLES20.glDisableVertexAttribArray(progFontOutlineYTransform);
        GLES20.glDisableVertexAttribArray(progFontOutlineVertices);
        GLES20.glDisableVertexAttribArray(progFontOutlineUvs);
        GLES20.glDisableVertexAttribArray(progFontOutlineColors);
        // GLES20.glDisableVertexAttribArray(progFontOutlineColors);

xTransformsBuff.rewind();
yTransformsBuff.rewind();
verticesBuff.rewind();
uvsBuff.rewind();




        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progFont);
        GLES20.glEnableVertexAttribArray(progFontXTransform);
        GLES20.glVertexAttribPointer(progFontXTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, xTransformsBuff);
        GLES20.glEnableVertexAttribArray(progFontYTransform);
        GLES20.glVertexAttribPointer(progFontYTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, yTransformsBuff);
        GLES20.glEnableVertexAttribArray(progFontVertices);
        GLES20.glVertexAttribPointer(progFontVertices, 2, GLES20.GL_FLOAT, false,  2*Float.BYTES, verticesBuff);
        GLES20.glEnableVertexAttribArray(progFontUvs);
        GLES20.glVertexAttribPointer(progFontUvs, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, uvsBuff);
        GLES20.glEnableVertexAttribArray(progFontColors);
        GLES20.glVertexAttribPointer(progFontColors, 4, GLES20.GL_UNSIGNED_BYTE, false, 4 * Byte.BYTES, colorsBuff);
        // GLES20.glEnableVertexAttribArray(progFontOutlineColors);
        // GLES20.glVertexAttribPointer(progFontOutlineColors, 4, GLES20.GL_UNSIGNED_BYTE, false, 4 * Byte.BYTES, outlineColorsBuff);
      
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * charCount);
        
        GLES20.glDisableVertexAttribArray(progFontXTransform);
        GLES20.glDisableVertexAttribArray(progFontYTransform);
        GLES20.glDisableVertexAttribArray(progFontVertices);
        GLES20.glDisableVertexAttribArray(progFontUvs);
        GLES20.glDisableVertexAttribArray(progFontColors);
        // GLES20.glDisableVertexAttribArray(progFontOutlineColors);

    }

    

    private static int loadShader(int type, String shaderCode)
	{    
		int shader = GLES20.glCreateShader(type);  
		GLES20.glShaderSource(shader, shaderCode);    
		GLES20.glCompileShader(shader);   
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) 
        {
            String info = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            shader = 0;
            throw new RuntimeException("Could not compile shader " +
                    type + ":" + info);
        } 
		return shader;
	}    



    


    


}
    