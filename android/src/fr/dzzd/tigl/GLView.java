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

import java.text.DecimalFormat;
import android.util.DisplayMetrics;
import org.appcelerator.titanium.TiApplication;
import android.util.Log;
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
import android.view.MotionEvent;
import java.util.*;
import java.util.Map.Entry;

public class GLView extends GLSurfaceView implements GLSurfaceView.Renderer, GLViewListener
{
    /*
     * IMPORTANT WARNING
     * If any attribute is accessed from JavaScript this will make the whole system laggy
     * To avoid this, all attributes are set as private
     */
    private GLScene scene;

    private int totalFrameCount = 0;
    private long[] timeOpengl = new long[5];
    private long[] timeJavascript = new long[5];
    private long[] timeMatrix = new long[5];
    private long[] timeFps = new long[5];

    private long fpsTime;
    private long fpsFrameCount;
    private long fps = 0;
    
    private int width;
    private int height;

    private GLViewListener glViewListener;

    private int backgroundColor;

    /*
     * String defining unit to use : "dp", "px", "hdp" are accepted values (hdp stand for "half dp")
     * */
    private String units = "px"; 
    private float unitsRatio = 1; 

    private int screenDpi;

    
    public GLView() 
    {
        super(TiApplication.getAppCurrentActivity());
        Log.i("GLSprite", "GLView() Thread ==> " + Thread.currentThread());
        this.setBackgroundColor("#FFFFFF");
        this.setEGLContextClientVersion(2);
        this.setPreserveEGLContextOnPause(true);
        this.setRenderer(this);
        this.setGLViewListener(this);
        this.screenDpi = this.getContext().getResources().getDisplayMetrics().densityDpi;
        this.setUnits("px");
    }

    /*
     * GLViewListener interface onInit(), should be overrided or set to another GLViewListener
     */
    public void onInit()
    {

    }

    /*
     * GLViewListener interface onResize(), should be overrided or set to another GLViewListener
     */
    public void onResize(float width, float height, String units)
    {
        
        
    }

    /*
     * GLViewListener interface onLoop(), should be overrided or set to another GLViewListener
     */
    public void onLoop()
    {
        
    }

    /*
     * GLViewListener interface onTouch(), should be overrided or set to another GLViewListener
     */
    public void onTouch(GLTouchEvent glTouchEvent)
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
        
        switch(this.units)
        {
            case "dp" :
                this.unitsRatio = this.screenDpi/160f;
            break;
            
            case "hdp" :
                this.unitsRatio = this.screenDpi/320f;
            break;

            default :
                this.unitsRatio = 1f;
            break;
        }
             
        Log.i("TIGL", "GLView - Units modified to " + this.units + " Units ratio is " + this.unitsRatio + " dpi is " + this.screenDpi);
        
        this.glViewListener.onResize(this.width  / this.unitsRatio, this.height  / this.unitsRatio, this.units);
    }

    
    private float xy[] = new float[2];
    private final int MAX_POINTER = 5;
    private float lastTouchPositionX[] = new float[MAX_POINTER];
    private float lastTouchPositionY[] = new float[MAX_POINTER];
    @Override
    public boolean onTouchEvent(MotionEvent e) 
    {
        
        int actionId = e.getActionMasked();

        /*
         * For move we search for the real pointer Index (https://gamedev.stackexchange.com/questions/56271/android-multitouch-how-to-detect-movement-on-non-primary-pointer-finger)
         */
        int pointerCount = 0;
        int[] pointerIndexes = new int[MAX_POINTER];
        if (actionId == MotionEvent.ACTION_MOVE) 
        {
            
            for (int i = 0; i < e.getPointerCount() && i < MAX_POINTER; i++) 
            {
                if (lastTouchPositionX[i] != e.getX(i) || lastTouchPositionY[i] != e.getY(i)) 
                {
                    lastTouchPositionX[i] = e.getX(i);
                    lastTouchPositionY[i] = e.getY(i);
                    pointerIndexes[pointerCount] = i;
                    pointerCount++;
                    //break;
                }
            }
            // Log.i("TIGL", "GLView Pointer moved count = " + pointerCount);
        }
        else
        {
            pointerCount = 1;
            pointerIndexes[0] = e.getActionIndex(); ;
        }
 


        //Log.i("TIGL", "GLView MotionEvent (" + xy[0] + "," + xy[1] +")");

        int action = -1;
        switch (actionId) 
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                action = GLTouchEvent.ACTION_DOWN;
            break;
            case MotionEvent.ACTION_MOVE:
                action = GLTouchEvent.ACTION_MOVE;
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                action = GLTouchEvent.ACTION_UP;
            break;
            case MotionEvent.ACTION_CANCEL:
                action = GLTouchEvent.ACTION_CANCEL;
            break;
    
        }

        for(int n = 0; n<pointerCount; n++)
        {
            int pointerIndex = pointerIndexes[n];
            int pointerId = e.getPointerId(pointerIndex);
            float x = e.getX(pointerIndex);
            float y = e.getY(pointerIndex);

            /* 
            * Normalize coordinate to OpenGL
            */
            x = 2f * x / this.width - 1f;
            y = 1f - 2f * y / this.height ;

            /*
            * Compute scene position
            */
            xy[0] = x;
            xy[1] = y;
            this.scene.matrixInvert.mapPoints(xy);
            float sceneX = xy[0];
            float sceneY = xy[1];

            /*
            * Do an event for the scene if touch enabled as it is always touched
            */
            if(this.scene.touchEnabled)
            {
                GLTouchEvent glTouchEvent = new GLTouchEvent();
                glTouchEvent.action = action; 
                glTouchEvent.pointer = pointerId;
                glTouchEvent.sceneX = sceneX;
                glTouchEvent.sceneY = sceneY;
                glTouchEvent.x = sceneX;
                glTouchEvent.y = sceneY;
                glTouchEvent.entityId = this.scene.id;
                this.glViewListener.onTouch(glTouchEvent);
            }
            
            /*
            * For each touch enabled entities compute local coordinates and launch event
            * */
            HashMap<Integer,GLEntity> entities= this.scene.getEntities();
            int entityDrawOrder = -1;
            GLTouchEvent glTouchEvent = new GLTouchEvent();

            for (Map.Entry<Integer, GLEntity> entityMap : entities.entrySet()) 
            {
                GLEntity entity = entityMap.getValue();

                if(!entity.touchEnabled || entity.lastDrawOrder < entityDrawOrder)
                {
                    continue;
                }

                xy[0] = x;
                xy[1] = y;
                entity.matrixInvert.mapPoints(xy);
                
                // Log.i("TIGL", "GLView MotionEvent (" + xy[0] + "," + xy[1] +")");
                if(xy[0] < 0 || xy[0] > entity.width || xy[1] < 0 || xy[1] > entity.height)
                {
                    continue;
                }

                // Log.i("TIGL", "*******************");
                //GLTouchEvent glTouchEvent = new GLTouchEvent();
                glTouchEvent.action = action; 
                glTouchEvent.pointer = pointerId;
                glTouchEvent.sceneX = sceneX;
                glTouchEvent.sceneY = sceneY;
                glTouchEvent.x = xy[0];
                glTouchEvent.y = xy[1];
                glTouchEvent.entityId = entity.id;
                entityDrawOrder = entity.lastDrawOrder;
            }
            if(entityDrawOrder != -1)
            {
                this.glViewListener.onTouch(glTouchEvent);
            }
        }

        return true;
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
        GLEntity.resetUid();

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
        Log.i("TIGL", "GLView - onSurfaceChanged(" + width + "," + height + ")");
        this.width = width;
        this.height = height;
        this.glViewListener.onResize(width  / this.unitsRatio, height  / this.unitsRatio, this.units);
    }

    public int getTimeJavascriptUs()
    {
        long total = 0;
        for(int n=0;n<this.timeJavascript.length;n++)
        {
            total+=this.timeJavascript[n];
        }
        return (int)(total/(this.timeJavascript.length*1000));
    }

    
    public int getTimeMatrixUs()
    {
        long total = 0;
        for(int n=0;n<this.timeMatrix.length;n++)
        {
            total+=this.timeMatrix[n];
        }
        return (int)(total/(this.timeMatrix.length * 1000));
    }

    
    public int getTimeOpenglUs()
    {
        long total = 0;
        for(int n=0;n<this.timeOpengl.length;n++)
        {
            total+=this.timeOpengl[n];
        }
        return (int)(total/(this.timeOpengl.length * 1000));
    }

    
    public int getTimeFpsUs()
    {
        long total = 0;
        for(int n=0;n<this.timeFps.length;n++)
        {
            total+=this.timeFps[n];
        }
        return (int)(total/(this.timeFps.length * 1000));
    }
    
    public int getTimeIdleUs()
    {
        long result = this.getTimeFpsUs();
        result -= this.getTimeOpenglUs();
        result -= this.getTimeMatrixUs();
        result -= this.getTimeJavascriptUs();
        return (int)result;
    }


    int c=0;
    long timeFrameStart = 0;
    @Override
    public void onDrawFrame(GL10 gl) 
    {
        if(!this.isCurrentContext())
        {
            Log.i("TIGL", "GLView - NO CURRENT CONTEXT EGL(onDrawFrame)");
        }

        long T0 = System.nanoTime();
        this.timeFps[this.totalFrameCount%this.timeFps.length] = this.timeFrameStart != 0 ? T0 - this.timeFrameStart : 0;
        this.timeFrameStart = T0;
        
        /*
         * Call callback
         */
        this.glViewListener.onLoop();

        long T1 = System.nanoTime();

        /*
         * Apply matrix transformation on all objects of the scene
         * */
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(2f/(this.width / this.unitsRatio),-2f/(this.height / this.unitsRatio));
        matrix.postTranslate(-1f, 1f);
        this.scene.updateMatrix(matrix);
        
        long T2 = System.nanoTime();
        
        
        /*
         * Draw OpenGL scene
         */
        GLES20.glClearColor(((this.backgroundColor>>16)&0xFF)/255f,
                            ((this.backgroundColor>>8)&0xFF)/255f,
                            ((this.backgroundColor)&0xFF)/255f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        this.scene.draw();      

        long T3 = System.nanoTime();

        this.timeJavascript[this.totalFrameCount%this.timeJavascript.length] = (T1 - T0);
        this.timeMatrix[this.totalFrameCount%this.timeMatrix.length] = (T2 - T1);
        this.timeOpengl[this.totalFrameCount%this.timeOpengl.length] = (T3 - T2);
        

        this.fpsFrameCount++;

        if(this.totalFrameCount%300 == 299)
        {
            this.fps = 1000000000 * this.fpsFrameCount / (System.nanoTime() - this.fpsTime);
            this.fpsFrameCount = 0;
            this.fpsTime = System.nanoTime();


            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);
            Log.i("TIGL", "GLView OpenGL : " + df.format(this.getTimeOpenglUs()/1000f) + " ms");
            Log.i("TIGL", "GLView Matrices : " + df.format(this.getTimeMatrixUs()/1000f) + " ms");
            Log.i("TIGL", "GLView Javascript (datas read) : " + df.format(this.getTimeJavascriptUs()/1000f) + " ms");
            Log.i("TIGL", "GLView Idle time : " + df.format(this.getTimeIdleUs()/1000f) + " ms");
            Log.i("TIGL", "GLView FPS : " + df.format(1000000f / this.getTimeFpsUs()));
            // Log.i("TIGL", "GLView Framerate: " + df.format(fpsBd) + " FPS");
            // frameCount = 0;
            // oglTime = 0;
            // jsTime = 0;
            // matTime = 0;

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