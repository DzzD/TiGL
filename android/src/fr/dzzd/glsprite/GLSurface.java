package fr.dzzd.glsprite;


import org.appcelerator.kroll.common.Log;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES20;
import android.content.Context ;

public class GLSurface extends GLSurfaceView
{

    public GLSurface(Context context)
    {
        super(context);

        Log.i("GLSprite", "GLSurface(context)");

        // Create an OpenGL ES 2.0 context
        this.setEGLContextClientVersion(2);
        //this.setEGLContextClientVersion(1);
    }   
}