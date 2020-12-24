package fr.dzzd.glsprite;


import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import android.graphics.Matrix;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView;

public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer 
{
    /*
     * IMPORTANT WARNING
     * If any attribute is accessed from JavaScript this will make the whole system laggy
     * To avoid this, all attributes are set as private
     */
    private GLEntity scene;

    private int n = 5;
    private int frameCount = 0;
    private long oglTime = 0;
    private long jsTime = 0;
    private long matTime = 0;

    private long fpsTime;
    private long fpsFrameCount;
    private long fps = 0;
    
    private int width;
    private int height;

    
    public GLView() 
    {
        super(TiApplication.getAppCurrentActivity());
        Log.i("GLSprite", "GLView() Thread ==> " + Thread.currentThread());
        this.setEGLContextClientVersion(2);
        this.setRenderer(this);
    }

    public void onCreated()
    {

    }
    
    public void onChanged(int width, int height)
    {
        
        
    }
    
    public void onDraw()
    {
        
    }

    public GLEntity getScene()
    {
        return this.scene;
    }

    public int getFps()
    {
       return (int)this.fps;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        if(!this.isCurrentContext())
        {
            Log.i("GLSprite", "GLView - NO CURRENT CONTEXT EGL(onSurfaceCreated)");
        }
        
        GLShader.initShaders();
        this.scene= new GLEntity();
        this.onCreated();
        this.fpsTime = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) 
    {
        if(!this.isCurrentContext())
        {
            Log.i("GLSprite", "GLView - NO CURRENT CONTEXT EGL(onSurfaceChanged)");
        }

        this.width = width;
        this.height = height;
        this.onChanged(width, height);
    }


    int c=0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
        
        if(!this.isCurrentContext())
        {
            Log.i("GLSprite", "GLView - NO CURRENT CONTEXT EGL(onDrawFrame)");
        }

        

        long t0 = System.nanoTime();
        
        /*
         * Call GLView callback
         */
        this.onDraw();

        long t1 = System.nanoTime();

        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(2f/width,-2f/height);
        matrix.postTranslate(-1f, 1f);
        this.scene.updateMatrix(matrix);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        
        
        long t2 = System.nanoTime();
        /*
         * Draw OpenGL scene
         */
        this.scene.draw();

        

        long t3 = System.nanoTime();
        this.jsTime += t1 - t0;
        this.matTime += t2 - t1;
        this.oglTime += t3 - t2;
        frameCount++;

        this.fpsFrameCount++;

        if(frameCount == 1000)
        {
            Log.i("GLSprite", "GLRenderer. OGL time/image : " + (oglTime/frameCount)/1000 + "us");
            Log.i("GLSprite", "GLRenderer.  JS time/image : " + (jsTime/frameCount)/1000 + "us");
            Log.i("GLSprite", "GLRenderer. MAT time/image : " + (matTime/frameCount)/1000 + "us");
            frameCount = 0;
            oglTime=0;
            jsTime=0;
            matTime=0;

            this.fps = 1000000000 * this.fpsFrameCount / (System.nanoTime() - this.fpsTime);
            this.fpsFrameCount = 0;
            this.fpsTime = System.nanoTime();
        }

        
    }
    
    private boolean isCurrentContext()
    {
        if (EGL14.eglGetCurrentContext() == EGL14.EGL_NO_CONTEXT) {
           return false;
        }
        return true;
    }

}