package fr.dzzd.glsprite;

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
    
        private final static String vShaderTexture =
        "attribute vec2 vertices;" +
        "attribute vec2 textureUvs;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +
        "  gl_Position = vec4(vertices,0.0,1.0);" +
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

    

    private static int loadShader(int type, String shaderCode)
	{    
		int shader = GLES20.glCreateShader(type);  
		GLES20.glShaderSource(shader, shaderCode);    
		GLES20.glCompileShader(shader);    
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
        //progTextureMatrix = GLES20.glGetUniformLocation(progTexture, "matrix");
        progTextureVertices = GLES20.glGetAttribLocation(progTexture, "vertices");
        progTextureUvs = GLES20.glGetAttribLocation(progTexture, "textureUvs");



    }

    static long ta =0;
    static long tb =0;
    static long tc =0;
    static long td =0;
    static int callCount = 0;
    
    public final static void drawTexture(FloatBuffer vertices, FloatBuffer textureUvs, int textureHandle, int count, boolean triangleFan)
    {
        /*
         * Performs OpenGL drawing
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progTexture);
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
        GLES20.glDisableVertexAttribArray(progTextureVertices);
        GLES20.glDisableVertexAttribArray(progTextureUvs);

    }

    


    


}
    