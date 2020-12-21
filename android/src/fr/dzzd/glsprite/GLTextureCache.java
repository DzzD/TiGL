package fr.dzzd.glsprite;


import org.appcelerator.kroll.common.Log;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;


public class GLTextureCache 
{
    private static HashMap<String,Integer> caches = new HashMap<String,Integer>();
    
    public static int create(HashMap<String,Object> options, boolean usecache)
    {
        Integer handle = null;
        String key = (String)options.get("uid");

        if(usecache)
		{
            handle = caches.get(key);

            if(handle == null)
            {
                handle = createGLTexture(options);
                caches.put(key, handle);
                
            }
            return handle.intValue();
        }

        
        handle = createGLTexture(options);
        return handle.intValue();

    }

    public static Integer create(HashMap<String,Object> options)
    {
        Integer handle = create(options, true);
         return handle.intValue();
    }

    private static Integer createGLTexture(HashMap<String,Object> options)
    {
        int[] handlePtr = new int[1];
        GLES20.glGenTextures(1, handlePtr, 0);
        int handle = handlePtr[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            
        if((Boolean)options.get("tile"))
        {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        }
        else 
        {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        if(options.get("bitmap") != null)
        {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, (Bitmap)options.get("bitmap"), 0);
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
        return new Integer(handle);
    }
    
}
