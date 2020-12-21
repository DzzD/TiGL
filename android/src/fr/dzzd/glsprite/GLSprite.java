package fr.dzzd.glsprite;


import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;

import android.graphics.Bitmap;
import java.util.HashMap;
import org.appcelerator.kroll.common.Log;
import android.graphics.Matrix;
import android.util.*;
import java.nio.*;


public class GLSprite extends GLEntity
{
    private int textureHandle;

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

    private FloatBuffer vertexBuffer;
    private FloatBuffer uvsBuffer;

    private float width;
    private float height;

    private int textureWidth;
    private int textureHeight;

    private int spriteColCount;
    private int spriteRowCount;

   /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(String filePath)
    {   
        this(filePath, -1, -1);
    }

    /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(String filePath, int width, int height)
    {
        super();

        Bitmap bitmap = null;
        this.spriteColCount = 0;
        this.spriteRowCount = 0;

        /*
         * Initialze vertices nd uvs buffers 
         */
        this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertexBuffer.put(this.vertices).position(0); 

        this.uvsBuffer = ByteBuffer.allocateDirect(this.uvs.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uvsBuffer.put(this.uvs).position(0);

        try 
        {
            bitmap = BitmapCache.load(filePath);

            HashMap<String, Object> options = new HashMap<String, Object>();
            options.put("uid",filePath);
            options.put("bitmap",bitmap);
            options.put("tile",false);
            this.textureHandle = GLTextureCache.create(options);
            
            this.textureWidth = bitmap.getWidth();
            this.textureHeight = bitmap.getHeight();

            if(width == -1)
            {
                width = this.textureWidth;
            }

            if(height == -1)
            {
                height = this.textureHeight;
            }

            this.setSize(width, height);
            this.spriteColCount = this.textureWidth / (int)this.width;
            this.spriteRowCount = this.textureHeight / (int)this.height;
            this.setFrame(0);

            Log.i("GLSprite", "GLSprite()");
            Log.i("GLSprite", "sprite width = " + width);
            Log.i("GLSprite", "sprite height = " + height);
            Log.i("GLSprite", "spriteColCount = " + this.spriteColCount);
            Log.i("GLSprite", "spriteRowCount = " + this.spriteRowCount);
        }
        catch( Exception e)
        {
            Log.e("GLSprite", "Error - GLSpriteBitmap.load(Resources/appicon.png)" + e);
        }

        

    }

    public GLSprite setFrame(int spriteNum)
    {
        int frameCount = this.spriteColCount * this.spriteRowCount;
        if(spriteNum < 0 )
        {
            spriteNum = (-spriteNum);
        }
        spriteNum %= frameCount;
        
        int spriteCol = spriteNum % this.spriteColCount;
        int spriteRow = spriteNum / this.spriteColCount;

        float left = (spriteCol * this.width) / this.textureWidth;
        float top = (spriteRow * this.height) / this.textureHeight;

        this.setUvs(left, top, this.width / this.textureWidth, this.height / this.textureHeight);

        return this;
    }

    public GLSprite setSize(float width, float height)
    {
        this.width = width;
        this.height = height;

        this.vertexBuffer.clear();

        //bottom left
        this.vertexBuffer.put(0);
        this.vertexBuffer.put(height);
        this.vertexBuffer.put(0);
        
        //top left        
        this.vertexBuffer.put(0);
        this.vertexBuffer.put(0);
        this.vertexBuffer.put(0);
        
        //top right 
        this.vertexBuffer.put(width);
        this.vertexBuffer.put(0);
        this.vertexBuffer.put(0);
        
        //bottom right 
        this.vertexBuffer.put(width);
        this.vertexBuffer.put(height);
        this.vertexBuffer.put(0);

        this.vertexBuffer.position(0);

        return this;
    }

    
    public GLSprite setUvs(float left, float top, float width, float height)
    {
        this.vertexBuffer.clear();
        

        //bottom left
        this.uvsBuffer.put(left);
        this.uvsBuffer.put(top + height);
        
        //top left        
        this.uvsBuffer.put(left);
        this.uvsBuffer.put(top);
        
        //top right 
        this.uvsBuffer.put(left + width);
        this.uvsBuffer.put(top);
        
        //bottom right 
        this.uvsBuffer.put(left + width);
        this.uvsBuffer.put(top +height);

        this.uvsBuffer.position(0);

        return this;
    }

    @Override
    public void draw()
    {
        super.draw();
        GLShader.drawTexture(this.matrix, this.vertexBuffer, this.uvsBuffer, this.textureHandle);
    }

    

}