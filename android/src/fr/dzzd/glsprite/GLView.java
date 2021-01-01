package fr.dzzd.glsprite;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import android.util.DisplayMetrics;
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
import android.graphics.Color;

public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer, GLViewListener
{
    /*
     * IMPORTANT WARNING
     * If any attribute is accessed from JavaScript this will make the whole system laggy
     * To avoid this, all attributes are set as private
     */
    private GLScene scene;

    private int totalFrameCount = 0;
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

    private int backgroundColor;

    /*
     * String defining unit to use : "dp" or "px" are accepted values
     * */
    private String units = "px"; 
    private float unitsRatio = 1; 

    private int screenDpi;

    
    public GLView() 
    {
        super(TiApplication.getAppCurrentActivity());
        Log.i("GLSprite", "GLView() Thread ==> " + Thread.currentThread());
        this.setBackgroundColor("#FFFFFF");
        this.setUnits("px");
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setRenderer(this);
        this.setGLViewListener(this);
        this.screenDpi = this.getContext().getResources().getDisplayMetrics().densityDpi;
    }

    public void onInit()
    {

    }
    
    public void onResize(float width, float height, String units)
    {
        
        
    }
    
    public void onLoop()
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

    public void setBackgroundColor(String color)
    {
        this.backgroundColor = Color.parseColor(color);
    }

    
    public void setUnits(String units)
    {
        this.units = units.toLowerCase();
        
        this.unitsRatio = 1f;
        if("dp".equals(this.units))
        {
            this.unitsRatio = this.screenDpi/160f;
        }       
        Log.i("TIGL", "GLView - Units modified to " + this.units + " Units ratio is " + this.unitsRatio + " dpi is " + this.screenDpi);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onSurfaceCreated)");
        }
        
        GLShader.initShaders();
        BitmapCache.clear();
        GLTextureCache.clear();
        this.scene = new GLScene();
        this.fpsFrameCount = 0;
        this.fpsTime = System.nanoTime();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(((this.backgroundColor>>16)&0xFF)/255f,
                            ((this.backgroundColor>>8)&0xFF)/255f,
                            ((this.backgroundColor)&0xFF)/255f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT|GLES20.GL_STENCIL_BUFFER_BIT);

        
        
        this.glViewListener.onInit();
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
        this.glViewListener.onResize(width  / this.unitsRatio, height  / this.unitsRatio, this.units);
    }


    int c=0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onDrawFrame)");
        }

        long t0 = System.nanoTime();
        
        /*
         * Call callback
         */
        this.glViewListener.onLoop();

        long t1 = System.nanoTime();
        
        

        /*
         * Apply matrix transformation on all objects of the scene
         * */
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(2f/(this.width / this.unitsRatio),-2f/(this.height / this.unitsRatio));
        matrix.postTranslate(-1f, 1f);
        this.scene.updateMatrix(matrix);
        
        

        
        
        long t2 = System.nanoTime();
        
        
        /*
         * Draw OpenGL scene
         */
        GLES20.glClearColor(((this.backgroundColor>>16)&0xFF)/255f,
                            ((this.backgroundColor>>8)&0xFF)/255f,
                            ((this.backgroundColor)&0xFF)/255f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.scene.draw();

        

        long t3 = System.nanoTime();
        this.jsTime += t1 - t0;
        this.matTime += t2 - t1;
        this.oglTime += t3 - t2;
        frameCount++;

        this.fpsFrameCount++;

        if(frameCount == 300)
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

        this.totalFrameCount++;

        
    }
    
    private boolean isCurrentContext()
    {
        if (EGL14.eglGetCurrentContext() == EGL14.EGL_NO_CONTEXT) {
           return false;
        }
        return true;
    }

}