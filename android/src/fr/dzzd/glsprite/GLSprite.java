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
    private int frameCount;

   /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(String filePath)
    {   
        this(filePath, new HashMap<String,String>());
    }

    /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(String filePath, HashMap<String,String> options)
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

            HashMap<String, Object> optionsBitmap = new HashMap<String, Object>();
            optionsBitmap.put("uid",filePath);
            optionsBitmap.put("bitmap",bitmap);
            
            optionsBitmap.put("tile",(Boolean.parseBoolean(options.get("tile"))));
            this.textureHandle = GLTextureCache.create(optionsBitmap);
            
            this.textureWidth = bitmap.getWidth();
            this.textureHeight = bitmap.getHeight();


            String oWidth = options.get("width");
            if(oWidth == null)
            {
                this.width = this.textureWidth;
            }
            else
            {
                this.width = Integer.parseInt(oWidth);
            }

            String oHeight = options.get("height");
            if(oHeight == null)
            {
                this.height = this.textureHeight;
            }
            else
            {
                this.height = Integer.parseInt(oHeight);
            }

            this.setSize(this.width, this.height);
            this.spriteColCount = this.textureWidth / (int)this.width;
            this.spriteRowCount = this.textureHeight / (int)this.height;
            this.frameCount = this.spriteColCount * this.spriteRowCount;
            if(this.frameCount == 0)
            {
                this.frameCount = 1;
            }
            this.setFrame(0);

            //Log.i("GLSprite", "GLSprite()");
            //Log.i("GLSprite", "sprite width = " + this.width);
            //Log.i("GLSprite", "sprite height = " + this.height);
            //Log.i("GLSprite", "spriteColCount = " + this.spriteColCount);
            //Log.i("GLSprite", "spriteRowCount = " + this.spriteRowCount);
        }
        catch( Exception e)
        {
            Log.e("GLSprite", "Error : GLSprite::BitmapCache.load(" + filePath + ") =>" + e, e);
        }

        

    }

    public GLSprite setFrame(int spriteNum)
    {
      
        if(spriteNum < 0 )
        {
            spriteNum = (-spriteNum);
        }
        spriteNum %= this.frameCount;
        
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