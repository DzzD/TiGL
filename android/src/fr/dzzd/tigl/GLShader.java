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

import org.appcelerator.kroll.common.Log;
import android.opengl.GLES20;
import android.graphics.Matrix;
import java.lang.System;
import android.util.*;
import java.nio.*;


public class GLShader 
{
    /*
     * Helper matrices used to convert from Android matrix
     *   USED INTERNALLY, MUST NOT BE MODIFIED DIRECTLY
     */
    private static float[] matrix4x4 = new float[16];
    private static float[] matrix3x3 = new float[9];

    /*
     * Shader for basic texture drawing
     */
    /*
    private final static String vShaderTexture =
        "uniform mat4 matrix;" +
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(vertices,0.0,1.0)*matrix ;" +
        "  textureUv = textureUvs;" +
        "}";
        */
    /*
        private final static String vShaderTexture =
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(vertices,0.0,1.0);" +
        "  textureUv = textureUvs;" +
        "}";
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

    private static int progTextureMatrix;
    private static int progTextureVertices;
    private static int progTextureUvs;
    private static int progTextureXTransform;
    private static int progTextureYTransform;

    

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

    public final static void initShaders()
    {
        
        /*
         * Initialize basic texture drawing program
         */
        progTexture=GLES20.glCreateProgram();
        GLES20.glAttachShader(progTexture, loadShader(GLES20.GL_VERTEX_SHADER, vShaderTexture));
       	GLES20.glAttachShader(progTexture, loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderTexture));
        GLES20.glLinkProgram(progTexture);
        
        /*
         * Gets pointers for basic texture drawing program parameters
         */
        progTextureXTransform = GLES20.glGetAttribLocation(progTexture, "xTransform");
        progTextureYTransform = GLES20.glGetAttribLocation(progTexture, "yTransform");
        progTextureVertices = GLES20.glGetAttribLocation(progTexture, "vertices");
        progTextureUvs = GLES20.glGetAttribLocation(progTexture, "textureUvs");



    }

    
    
    public final static void drawTexture(FloatBuffer xTransforms, FloatBuffer yTransforms, FloatBuffer vertices, FloatBuffer textureUvs, int textureHandle, int count, boolean triangleFan)
    {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progTexture);
        GLES20.glEnableVertexAttribArray(progTextureVertices);
        GLES20.glVertexAttribPointer(progTextureVertices, 2, GLES20.GL_FLOAT, false,  2*Float.BYTES, vertices);
        GLES20.glEnableVertexAttribArray(progTextureUvs);
        GLES20.glVertexAttribPointer(progTextureUvs, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, textureUvs);
        GLES20.glEnableVertexAttribArray(progTextureXTransform);
        GLES20.glVertexAttribPointer(progTextureXTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, xTransforms);
        GLES20.glEnableVertexAttribArray(progTextureYTransform);
        GLES20.glVertexAttribPointer(progTextureYTransform, 3, GLES20.GL_FLOAT, false, 3*Float.BYTES, yTransforms);
      
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


    


    


}
    