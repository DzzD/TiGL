package fr.dzzd.glsprite;



import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.kroll.common.Log;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import android.graphics.Matrix;
import java.nio.*;
import android.os.SystemClock;

import android.graphics.Bitmap;


public class GLRenderer implements GLSurfaceView.Renderer 
{
   
    private ExampleProxy proxy = null;

    private int width;
    private int height;



    private static Matrix matrix = new Matrix();
    private static float[] matrix3 = new float[9];
    private static float[] matrix4 = new float[16];

    private static GLSpriteBitmap sprite;
    private static GLSpriteBitmap sprite2;

    public static int loadShader(int type, String shaderCode)
	{    
		int shader = GLES20.glCreateShader(type);  
		GLES20.glShaderSource(shader, shaderCode);    
		GLES20.glCompileShader(shader);    
		return shader;
	}



    public GLRenderer(ExampleProxy proxy)
    {
        this.proxy = proxy;
        this.width = -1;
        this.height = -1;
    }

    public GLRenderer setWidth(int width)
    {
        this.width = width;
        return this;
    }

    public GLRenderer setHeight(int height)
    {
        this.height = height;
        return this;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) 
    {
        Log.i("GLSprite", "GLRenderer.onSurfaceCreated(GL10, EGLConfig)");

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc (GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
       

        
        sprite = new GLSpriteBitmap("Resources/appicon.png");
        sprite2 = new GLSpriteBitmap("Resources/robot.png");
        
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) 
    {
        //GLES20.glViewport(0, 0, width, height);
        this.setWidth(width);
        this.setHeight(height);
        Log.i("GLSprite", "GLRenderer.onSurfaceChanged(" + width + "," + height + ")");
    }


    int c=0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
        //sprite.rotate = 0.5f;
        sprite.x = 150f;
        sprite.y = 250f;
        matrix.reset();
        matrix.postScale(2f/width,-2f/height);
        matrix.postTranslate(-1f, 1f);
        sprite.updateMatrix(matrix);

        GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        sprite.draw();

        sprite.rotate =(float) ((SystemClock.uptimeMillis()));

        this.proxy.onDrawCallback();
        
    }
   
    
} 