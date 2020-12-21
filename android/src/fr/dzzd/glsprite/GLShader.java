package fr.dzzd.glsprite;

import org.appcelerator.kroll.common.Log;
import android.opengl.GLES20;
import android.graphics.Matrix;
import android.util.*;
import java.nio.*;


public class GLShader 
{
    /*
     * Helper matrices used to convert from Android matrix
     */
    private static float[] matrix4x4 = new float[16];
    private static float[] matrix3x3 = new float[16];

    /*
     * Shader for basic texture drawing
     */
    private final static String vShaderTexture =
        "uniform mat4 matrix;" +
        "attribute vec4 vertices;" +
        "attribute vec2 textureUvs;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +
        "  gl_Position = vertices * matrix;" +
        "  textureUv = textureUvs;" +
        "}";

    private final static String fShaderTexture =
        "precision mediump float;" +
        "uniform sampler2D u_Texture;" +
        "varying vec2 textureUv;" +
        "void main()" +
        "{" +    
        "   gl_FragColor = texture2D(u_Texture, textureUv)+vec4(0.1,0.1,0.1,0.1);" +   
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
        progTextureMatrix = GLES20.glGetUniformLocation(progTexture, "matrix");
        progTextureVertices = GLES20.glGetAttribLocation(progTexture, "vertices");
        progTextureUvs = GLES20.glGetAttribLocation(progTexture, "textureUvs");



    }

    public final static void drawTexture(Matrix matrix, FloatBuffer vertices, FloatBuffer textureUvs, int textureHandle)
    {
        
        /*
         * Convert Android Matrix to OpenGL matrix (array of floats)
         */
        matrix.getValues(matrix3x3);
            for(int y=0;y<4;y++)
                for(int x=0;x<4;x++)
                    matrix4x4[x+y*4]=(x==y)?1:0;
        matrix4x4[0]=matrix3x3[0];
        matrix4x4[1]=matrix3x3[1];
        matrix4x4[4]=matrix3x3[3];
        matrix4x4[5]=matrix3x3[4];
        matrix4x4[3]=matrix3x3[2];
        matrix4x4[7]=matrix3x3[5];

        /*
         * Performs OpenGL drawing
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glUseProgram(progTexture);
        GLES20.glEnableVertexAttribArray(progTextureVertices);
        GLES20.glVertexAttribPointer(progTextureVertices, 3, GLES20.GL_FLOAT, false,  3*Float.BYTES, vertices);
        GLES20.glEnableVertexAttribArray(progTextureUvs);
        GLES20.glVertexAttribPointer(progTextureUvs, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, textureUvs);
        GLES20.glUniformMatrix4fv(progTextureMatrix, 1, false, matrix4x4, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
        GLES20.glDisableVertexAttribArray(progTextureVertices);
        GLES20.glDisableVertexAttribArray(progTextureUvs);

    }

}
    