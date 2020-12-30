package fr.dzzd.glsprite;


import android.graphics.Bitmap;
import java.util.HashMap;
import org.appcelerator.kroll.common.Log;
import android.graphics.Matrix;
import java.util.*;
import java.nio.*;


public class GLSprite extends GLEntity
{
    public int textureHandle;

    /*
     * Sprite vertices 
     */
    public float[] vertices = 
	{        
		 0f,    200f,        
	 	 0f,    0f,          
		200f,   0f,      
		200f,   200f
    };

   
    
    /*
     * Texture UV coordinates 
     */
    
    public float[] uvs=
	{
		    0f,  1f,
            0f,  0f,
            1f,  0f,
            1f,  1f
    };
    
     
    public FloatBuffer vertexBuffer;
    public FloatBuffer uvsBuffer;

    private float width;
    private float height;

    private int textureWidth;
    private int textureHeight;

    private int spriteColCount;
    private int spriteRowCount;
    private int frameCount;

    public HashMap<String, Object> options;

  
    /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(HashMap<String,Object> options)
    {
        super(options);
        this.type = GL_SPRITE;
        this.width = options.get("width") != null ? (int)options.get("width") : 0;
        this.height = options.get("height") != null ? (int)options.get("height") : 0;
        this.spriteColCount = 0;
        this.spriteRowCount = 0;
        this.textureHandle = -1;
        this.textureWidth = -1;
        this.textureHeight = -1;
        this.options = new HashMap<String, Object>();

        /*
         * Initialze vertices and uvs buffers 
         */
        this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertexBuffer.put(this.vertices).position(0); 

        this.uvsBuffer = ByteBuffer.allocateDirect(this.uvs.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uvsBuffer.put(this.uvs).position(0);

        try 
        {
            String url = (String)options.get("url");
            Bitmap bitmap = BitmapCache.load(url);
            this.options.put("textureUid",url);
            this.options.put("textureBitmap",bitmap);
            this.options.put("textureTile",options.get("tile"));
            this.textureWidth = bitmap.getWidth();
            this.textureHeight = bitmap.getHeight();
        }
        catch( Exception e)
        {
            Log.e("TIGL", "GLSprite: BitmapCache.load(" + options.get("url") + ") : " + e, e);
        }

       
        if(this.width == 0)
        {
            this.width = this.textureWidth;
        }

        if(this.height == 0)
        {
            this.height = this.textureHeight;
        }

        this.setSize(this.width, this.height);

        this.spriteColCount = this.textureWidth / (int)this.width;
        this.spriteRowCount = this.textureHeight / (int)this.height;
        this.frameCount = this.spriteColCount * this.spriteRowCount;
        if(this.frameCount <= 0)
        {
            this.frameCount = 1;
            this.spriteColCount = 1;
            this.spriteRowCount = 1;
        }
        this.setFrame(0);

        
    }

    /*
     * Return an unique identifier for this sprite material
     *  could be a combinaison of texture, opacity, effects, etc...
     * This identifier is used to know wich entities/sprite can be draw together
     */
    @Override
    public int getMaterialUid()
    {
        return Integer.valueOf(this.textureHandle);
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
        
        //top left        
        this.vertexBuffer.put(0);
        this.vertexBuffer.put(0);
        
        //top right 
        this.vertexBuffer.put(width);
        this.vertexBuffer.put(0);
        
        //top right 
        this.vertexBuffer.put(width);
        this.vertexBuffer.put(height);

        this.vertexBuffer.position(0);
        this.vertexBuffer.get(this.vertices);
        this.vertexBuffer.position(0);

        return this;
    }

    
    public GLSprite setUvs(float left, float top, float width, float height)
    {
        this.uvsBuffer.clear();

        //bottom left
        this.uvsBuffer.put(left);
        this.uvsBuffer.put(top + height);
        
        //top left        
        this.uvsBuffer.put(left);
        this.uvsBuffer.put(top);
        
        //top right 
        this.uvsBuffer.put(left + width);
        this.uvsBuffer.put(top);
        
        //top right 
        this.uvsBuffer.put(left + width);
        this.uvsBuffer.put(top + height);
        
        this.uvsBuffer.position(0);
        this.uvsBuffer.get(this.uvs);
        this.uvsBuffer.position(0);

        return this;
    }

    
    @Override
    public void prepareDrawing()
    {
        if(this.textureHandle == -1)
        {
            this.textureHandle = GLTextureCache.create(this.options);
        }
    }
    

    static int tmpBufferCapacity = 1;
    static float[] verts= new float[6 * 2 * tmpBufferCapacity];
    static FloatBuffer uvsb = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    static FloatBuffer vbuff = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

    @Override
    public void drawSingle()
    {
        //Log.i("TIGL", "GLSprite : drawSingle()");
        this.matrix.mapPoints(verts, 0, this.vertices, 0, 4);
        vbuff.clear();
        vbuff.put(verts, 0, 8);
        vbuff.rewind();
        GLShader.drawTexture(vbuff, this.uvsBuffer, this.textureHandle, 1, true);
    }
    
    @Override
    public void drawBatch(ArrayList<GLEntity> entities)
    {
        if(tmpBufferCapacity<entities.size())
        {
            tmpBufferCapacity = entities.size() + 1000;
            Log.i("TIGL", "GLSprite : increase draw buffer size to " + tmpBufferCapacity);
            verts= new float[6 * 2 * tmpBufferCapacity];
            uvsb = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vbuff = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        uvsb.clear();
        int count = 0;
        //for (Enumeration<GLEntity> e = entities.elements(); e.hasMoreElements();)
        Iterator<GLEntity> childIterator = entities.iterator();
        while (childIterator.hasNext()) 
        {
            GLSprite sprite = (GLSprite)childIterator.next();
            sprite.matrix.mapPoints(verts, count * 2 * 6, sprite.vertices, 0, 4);
            verts[count * 2 * 6 + 8 ] = verts[count * 2 * 6 + 0 ];
            verts[count * 2 * 6 + 9 ] = verts[count * 2 * 6 + 1 ];
            verts[count * 2 * 6 + 10 ] = verts[count * 2 * 6 + 4 ];
            verts[count * 2 * 6 + 11 ] = verts[count * 2 * 6 + 5 ];

            sprite.uvsBuffer.position(0);
            uvsb.put(sprite.uvsBuffer);
            uvsb.put(sprite.uvs[0]);
            uvsb.put(sprite.uvs[1]);
            uvsb.put(sprite.uvs[4]);
            uvsb.put(sprite.uvs[5]);

            
            count++;
            
        }
      

        vbuff.clear();
        vbuff.put(verts, 0, count * 12);
        vbuff.rewind();
        uvsb.position(0);
        GLShader.drawTexture(vbuff, uvsb, this.textureHandle, count, false);


        /*
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(entities.size() * this.vertices.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.vertexBuffer.position(0); 
        this.vertexBuffer.put()
        */

        /*
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(entities.size() * 6 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        FloatBuffer uvsBuffer = ByteBuffer.allocateDirect(entities.size() * 6 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (Vector<GLEntity> e = entities.elements(); e.hasMoreElements();)
        {
            Vector<GLEntity> entity = e.nextElement();
            
        }

        
        GLShader.drawTextureBatch(vertexBuffer, uvsBuffer, entities.size(), this.textureHandle);
        */
    }
    

}