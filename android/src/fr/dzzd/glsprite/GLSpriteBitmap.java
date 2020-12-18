package fr.dzzd.glsprite;

import org.appcelerator.kroll.common.Log;
import android.opengl.GLES20;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.graphics.Matrix;
import android.util.*;
import java.nio.*;


public class GLSpriteBitmap extends GLSprite
{
    /*
    * OpenGL program handle
    */
    private static int prog;

    
    private static int vShaderMatrix;
    private int textureHandle;

    /*
     * Vertex shader
     */
    private final static String vShader =
        "attribute vec2 a_TexCoordinate;" +
		"varying vec2 v_TexCoordinate;" +
        "uniform mat4 matrix;" +
        "attribute vec4 position;" +
        "void main()" +
        "{" +
        "  gl_Position = position * matrix;" +
		"  v_TexCoordinate = a_TexCoordinate;" +
        "}";

    /*
     * Fragment shader
     */
    private final static String fShader =
        "precision mediump float;" +
		"uniform sampler2D u_Texture;" +
		"varying vec2 v_TexCoordinate;" +
		"void main()" +
        "{" +    
		"   gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +   
		"}";
    
    /*
     * Sprite vertices & its Buffer
     */
    private float[] vertices = 
	{        
		 0f, 200f, 0f,        
	 	 0f,   0f, 0f,           
		200f,  0f, 0f,       
		200f,200f, 0f
	};

    private FloatBuffer vertexBuffer;
    private static int vShaderVertex;
    

    /*
     * Texture UV coordinates & its Buffer
     */
    private float[] uvs=
	{
		    0f,  1f,
            0f,  0f,
            1f,  0f,
            1f,  1f
    };

    private FloatBuffer uvsBuffer;
    private static int vShaderUV;

    
    /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSpriteBitmap(String filePath)
    {
        super();

       

        Bitmap bitmap = null;
        try 
        {
            bitmap = BitmapCache.load(filePath);
        }
        catch( Exception e)
        {
            Log.e("GLSprite", "Error - GLSpriteBitmap.load(Resources/appicon.png)" + e);
        }

        /*
         * Load and compile GL shaders program
         */
        prog=GLES20.glCreateProgram();
        GLES20.glAttachShader(prog, GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vShader));
       	GLES20.glAttachShader(prog, GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fShader));
        GLES20.glLinkProgram(prog);

        /*
         * Get pointers to program parameters
         */
        vShaderVertex = GLES20.glGetAttribLocation(prog, "position");
       	vShaderMatrix = GLES20.glGetUniformLocation(prog, "matrix");
        vShaderUV = GLES20.glGetAttribLocation(prog, "a_TexCoordinate");

        /*
         * Create Buffer for program parameters
         */
        this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertexBuffer.put(this.vertices).position(0); 

        this.uvsBuffer = ByteBuffer.allocateDirect(this.uvs.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uvsBuffer.put(this.uvs).position(0); 

        /*
         * Prepare OpenGl texture
         */
        int[] handle = new int[1];
        GLES20.glGenTextures(1, handle, 0);
        this.textureHandle = handle[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureHandle);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

    }

    @Override
    public void draw()
    {
        super.draw();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureHandle);

        GLES20.glUseProgram(prog);

        GLES20.glEnableVertexAttribArray(vShaderVertex);
        GLES20.glVertexAttribPointer(vShaderVertex, 3, GLES20.GL_FLOAT, false,  3*Float.BYTES, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vShaderUV);
        GLES20.glVertexAttribPointer(vShaderUV, 2, GLES20.GL_FLOAT, false, 2*Float.BYTES, uvsBuffer);
        GLES20.glUniformMatrix4fv(vShaderMatrix, 1, false, this.matrix4x4, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        GLES20.glDisableVertexAttribArray(vShaderVertex);
        GLES20.glDisableVertexAttribArray(vShaderUV);




    }

    

}