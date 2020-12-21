package fr.dzzd.glsprite;



import java.util.HashMap;

import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollFunction;
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
   
    private GLView glView = null;

    private int width;
    private int height;

    private GLEntity scene;

    private int n = 5;

    public GLRenderer(GLView glView)
    {
        this.glView = glView;
        this.width = -1;
        this.height = -1;
    }


    public int getWidth()
    {
        return this.width;
    }

    
    public int getHeight()
    {
        return this.width;
    }


    public GLEntity getScene()
    {
        return this.scene;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) 
    {
        Log.i("GLSprite", "GLRenderer.onSurfaceCreated(GL10, EGLConfig)");

        GLShader.initShaders();
        
        
        this.scene= new GLEntity();

        /*
         * Add some test sprites
         * */
        GLEntity sprite = new GLSprite("Resources/appicon.png");
        this.scene.add(sprite);
        GLSprite sprite2 = new GLSprite("Resources/robot.png");
        sprite2.x = 200;
        sprite2.y = 200;
        this.scene.add(sprite2);

        this.glView.onCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) 
    {
        Log.i("GLSprite", "GLRenderer.onSurfaceChanged(" + width + "," + height + ")");

        this.width = width;
        this.height = height;

        this.glView.onChanged(width, height);
    }


    int c=0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(2f/width,-2f/height);
        matrix.postTranslate(-1f, 1f);
        this.scene.updateMatrix(matrix);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        
        this.scene.draw();

        this.glView.onDraw();
        
    }
   
    
} 