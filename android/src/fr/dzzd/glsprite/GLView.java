package fr.dzzd.glsprite;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import android.view.SurfaceHolder;
import android.view.View.OnFocusChangeListener;
import android.view.View;
import android.graphics.Matrix;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView;

public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer, GLViewListener
{
    /*
     * IMPORTANT WARNING
     * If any attribute is accessed from JavaScript this will make the whole system laggy
     * To avoid this, all attributes are set as private
     */
    private GLScene scene;

    private int frameCount = 0;
    private long oglTime = 0;
    private long jsTime = 0;
    private long matTime = 0;

    private long fpsTime;
    private long fpsFrameCount;
    private long fps = 0;
    
    private int width;
    private int height;

    private GLViewListener glViewListener;

    // private boolean windowVisibility;
    private boolean paused = false;
    
    public GLView() 
    {
        super(TiApplication.getAppCurrentActivity());
        Log.i("GLSprite", "GLView() Thread ==> " + Thread.currentThread());
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setRenderer(this);
        //this.setOnFocusChangeListener(this);
        this.setGLViewListener(this);
       // TiApplication.getAppCurrentActivity().addEventListener
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

    public void setGLViewListener(GLViewListener glViewListener)
    {
        this.glViewListener = glViewListener;
    }

    public GLScene getScene()
    {
        return this.scene;
    }

    public int getFps()
    {
       return (int)this.fps;
    }

    /*
    @Override
    protected void onWindowVisibilityChanged (int visibility)
    {
        switch(visibility)
        {
            case GONE:
                //Log.i("GLSprite", "GLView.onWindowVisibilityChanged(GONE)");
            break;
            case INVISIBLE:
                //Log.i("GLSprite", "GLView.onWindowVisibilityChanged(INVISIBLE)");
                //this.paused = true;
            break;
            case VISIBLE:
                //Log.i("GLSprite", "GLView.onWindowVisibilityChanged(VISIBLE)");
                //this.onResume();
                //this.paused = false;
            break;
        }
        //super.onWindowVisibilityChanged(visibility);
    }
    */

/*
    public void onFocusChange(View v, boolean hasFocus)
    {
        //Log.i("GLSprite", "GLView.onFocusChange(View," + hasFocus + ")");
      
        
    }
    */

    /*
     * The folowing override broke the app
     */
    /*
    public void surfaceDestroyed (SurfaceHolder holder)
    {
        super.surfaceDestroyed(holder);
        //Log.i("GLSprite", "GLView() surfaceDestroyed()");
        this.onPause();
    }
    */



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onSurfaceCreated)");
        }
        GLShader.initShaders();
        this.scene = new GLScene();
        this.glViewListener.onCreated();
        this.fpsFrameCount = 0;
        this.fpsTime = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) 
    {
        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onSurfaceChanged)");
        }

        this.width = width;
        this.height = height;
        this.glViewListener.onChanged(width, height);
    }


    int c=0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
    //     if(true)
    //         return;
        /*
        if(this.paused)
        {
            return;
        }*/

        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onDrawFrame)");
        }

/*
        if(!this.isShown())
        {
            Log.i("IIGL", "GLView - View not visible pausing thread");
            this.onPause();
        }
*/
        

        long t0 = System.nanoTime();
        
        /*
         * Call callback
         */
        this.glViewListener.onDraw();

        long t1 = System.nanoTime();

        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(2f/width,-2f/height);
        matrix.postTranslate(-1f, 1f);
        this.scene.updateMatrix(matrix);
        long t2 = System.nanoTime();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        
        
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

        if(frameCount == 100)
        {
            this.fps = 1000000000 * this.fpsFrameCount / (System.nanoTime() - this.fpsTime);
            this.fpsFrameCount = 0;
            this.fpsTime = System.nanoTime();


            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            BigDecimal openglTimeBd = new BigDecimal((oglTime/frameCount) / 1000000f);
            BigDecimal javascriptTimeBd = new BigDecimal((jsTime/frameCount) / 1000000f);
            BigDecimal matrixTimeBd = new BigDecimal((matTime/frameCount) / 1000000f);
            BigDecimal fpsBd = new BigDecimal(fps);
            openglTimeBd.setScale(2, BigDecimal.ROUND_HALF_UP);
            javascriptTimeBd.setScale(2, BigDecimal.ROUND_HALF_UP);
            matrixTimeBd.setScale(2, BigDecimal.ROUND_HALF_UP);
            fpsBd.setScale(2, BigDecimal.ROUND_HALF_UP);
            Log.i("TIGL", "GLRenderer (time/frame)  OpenGL: " + df.format(openglTimeBd) + " ms");
            Log.i("TIGL", "GLRenderer (time/frame)  JavaScript: " + df.format(javascriptTimeBd) + " ms");
            Log.i("TIGL", "GLRenderer (time/frame)  Matrices: " + df.format(matrixTimeBd) + " ms");
            Log.i("TIGL", "GLRenderer Framerate: " + df.format(fpsBd) + " FPS");
            frameCount = 0;
            oglTime = 0;
            jsTime = 0;
            matTime = 0;

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