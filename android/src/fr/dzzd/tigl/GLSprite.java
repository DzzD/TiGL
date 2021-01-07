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
    public float[] vertices = new float[8];
    
    /*
     * Texture UV coordinates 
     */
    public float[] uvs = new float[8];
    
    public FloatBuffer vertexBuffer;
    public FloatBuffer uvsBuffer;

    private int pixelWidth;
    private int pixelHeight;

    /*
     * Texture size, actually bitmap size used for this sprite
     */
    private int textureWidth;
    private int textureHeight;

    /*
    * Part of this bitmap used for this sprite
    *  => using the same texture for multiple sprites can 
    *     geatly improve performance via batch rendering
    */
    private int subTextureLeft;
    private int subTextureTop;
    private int subTextureWidth;
    private int subTextureHeight;

    
    /*
     * @todo : move everything about animation to GLEntity
     */
    private int animationFrameCount;
    private int animationFrameColCount;
    private int animationFrameRowCount;
    private int animationFrame;
    private int animationFrameStart = 0;
    private int animationFrameEnd = 0;
    private int animationLoop = 1;
    private boolean animationPingPong = false;
    private long animationStartTime = 0;
    private int animationDuration = 0;

    public HashMap<String, Object> options;

  
    /*
    * Construct a new SpriteBitmap using the specified image file
    */
    public GLSprite(HashMap<String,Object> options)
    {
        super(options);
        this.type = GL_SPRITE;
        this.pixelWidth = options.get("pixelWidth") != null ? Properties.propertyToInt(options.get("pixelWidth")) : -1;
        this.pixelHeight = options.get("pixelHeight") != null ? Properties.propertyToInt(options.get("pixelHeight")) : -1;
        this.subTextureWidth = options.get("subTextureWidth") != null ? Properties.propertyToInt(options.get("subTextureWidth")) : -1;
        this.subTextureHeight = options.get("subTextureHeight") != null ? Properties.propertyToInt(options.get("subTextureHeight")) : -1;
        this.subTextureLeft = options.get("subTextureLeft") != null ? Properties.propertyToInt(options.get("subTextureLeft")) : 0;
        this.subTextureTop = options.get("subTextureTop") != null ? Properties.propertyToInt(options.get("subTextureTop")) : 0;
        this.animationFrameCount = 1;
        this.animationFrameColCount = 0;
        this.animationFrameRowCount = 0;

        this.animationFrame = -1;
        this.animationFrameStart = 0;
        this.animationFrameEnd = 0;
        this.animationDuration = 0;
        this.animationLoop = 0;
        this.animationPingPong = false;
        
        this.textureHandle = -1;
        this.textureWidth = -1;
        this.textureHeight = -1;

        this.options = new HashMap<String, Object>();

        /*
         * Initialze vertices and uvs buffers 
         */
        this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uvsBuffer = ByteBuffer.allocateDirect(this.uvs.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

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

        if(this.subTextureWidth == -1)
        {
            this.subTextureWidth = this.textureWidth - this.subTextureLeft;
        }
        
        if(this.subTextureHeight == -1)
        {
            this.subTextureHeight = this.textureHeight - this.subTextureTop;
        }
       
        if(this.width == -1)
        {
            this.width = this.subTextureWidth;
        }

        if(this.height == -1)
        {
            this.height = this.subTextureHeight;
        }

        if(this.pixelWidth == -1)
        {
            this.pixelWidth = (int)this.width;
        }

        if(this.pixelHeight == -1)
        {
            this.pixelHeight = (int)this.height;
        }

        this.setSize(this.width, this.height);

        this.computeAnimationFrames();

        this.setAnimationFrame(0);
    }

    /*
     * @todo : move everything about animation to GLEntity or create class animation
     */
    private void computeAnimationFrames()
    {
        
        this.animationFrameColCount = this.subTextureWidth / this.pixelWidth;
        this.animationFrameRowCount = this.subTextureHeight / this.pixelHeight;
        this.animationFrameCount = this.animationFrameColCount * this.animationFrameRowCount;
        if(this.animationFrameCount <= 0)
        {
            this.animationFrameCount = 1;
            this.animationFrameColCount = 1;
            this.animationFrameRowCount = 1;
        }
        this.setAnimationFrame(0);
    }


    public void playAnimation(HashMap<String,Object> options)
    {
        this.animationFrameStart = options.get("start") != null ? Properties.propertyToInt(options.get("start")) : 0;
        this.animationFrameEnd = options.get("end") != null ? Properties.propertyToInt(options.get("end")) : this.animationFrameCount - 1;
        this.animationDuration = options.get("duration") != null ? Properties.propertyToInt(options.get("duration")) : 1000;
        this.animationLoop = options.get("loop") != null ? Properties.propertyToInt(options.get("loop")) : 1;
        this.animationPingPong = options.get("pingpong") != null ? Properties.propertyToBoolean(options.get("pingpong")) : false;
        this.animationStartTime = System.currentTimeMillis();  
    }


    public void updateAnimationFrame()
    {
        if(this.animationStartTime == 0)
        {
            return;
        }

        int frameCount = Math.abs(this.animationFrameEnd - this.animationFrameStart) + 1;
        float timePerFrame = this.animationDuration / (float)frameCount;
        long elapsedTime = System.currentTimeMillis() - this.animationStartTime;
        int frameIndex = (int) (elapsedTime / timePerFrame);

        int animationFrameStart = this.animationFrameStart;
        int animationFrameEnd = this.animationFrameEnd;

        if(this.animationPingPong)
        {
            int loopFrame = frameIndex % (frameCount - 1);
            int loopDone = frameIndex / (frameCount - 1);
            boolean pong = (loopDone%2 != 0);

            if(loopDone >= this.animationLoop * 2 && this.animationLoop != 0)
            {
                this.animationStartTime = 0;
                this.setAnimationFrame(this.animationFrameStart);
                return;
            }

            if(pong)
            {
                animationFrameStart = this.animationFrameEnd;
                animationFrameEnd = this.animationFrameStart;
            }

            if(animationFrameEnd > animationFrameStart )
            {
                this.setAnimationFrame(animationFrameStart  + loopFrame);
            }
            else
            {
                this.setAnimationFrame(animationFrameStart  - loopFrame);
            }
            return;
        }


        int loopFrame = frameIndex % frameCount;
        int loopDone = frameIndex / frameCount;
        if(loopDone >= this.animationLoop && this.animationLoop != 0)
        {
            this.animationStartTime = 0;
            this.setAnimationFrame(this.animationFrameEnd);
            return;
        }

        if(animationFrameEnd > animationFrameStart )
        {
            this.setAnimationFrame(animationFrameStart  + loopFrame);
        }
        else
        {
            this.setAnimationFrame(animationFrameStart  - loopFrame);
        }
        return;
    }
    

    /*
     * Return an unique identifier for this sprite material/draw
     *  could be a combinaison of texture, opacity, effects, layer number, etc...
     * This identifier is used to know wich entities/sprite can be drawn together
     */
    @Override
    public int getMaterialUid()
    {
        return Integer.valueOf(this.textureHandle);
    }

    public void setAnimationFrame(int animationFrame)
    {
        if(animationFrame == this.animationFrame)
        {
            return;
        }

        if(animationFrame < 0 )
        {
            animationFrame = (-animationFrame);
        }
        animationFrame %= this.animationFrameCount;

        this.animationFrame = animationFrame ;
        
        int frameCol = this.animationFrame % this.animationFrameColCount;
        int frameRow = this.animationFrame / this.animationFrameColCount;

        float left = (frameCol * this.pixelWidth);
        float top = (frameRow * this.pixelHeight);

        this.setUvs((this.subTextureLeft + left) / (float)this.textureWidth, (top + this.subTextureTop) / (float)this.textureHeight, this.pixelWidth / (float)this.textureWidth, this.pixelHeight / (float)this.textureHeight);

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
        this.updateAnimationFrame();
    }
    

    static int tmpBufferCapacity = 1;
    //static GLSprite[] sprts = new GLSprite[tmpBufferCapacity];
    static float[] verts= new float[6 * 2 * tmpBufferCapacity];
    static float[] uvst= new float[6 * 2 * tmpBufferCapacity];
    // static float[] uvsCached= new float[6 * 2 * tmpBufferCapacity];
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
        this.lastDrawOrder = this.getScene().currentDrawCount++;
    }
/*
    private void computeVertices(int startIndex, int count)
    {
        for(int index = 0; index < count; index++)
        {
            GLSprite sprite = sprts[startIndex + index];
            int vIndex =  (startIndex + index) * 2 * 6;
            sprite.matrix.mapPoints(verts, vIndex, sprite.vertices, 0, 4);
            verts[vIndex + 8 ] = verts[vIndex + 0 ];
            verts[vIndex + 9 ] = verts[vIndex + 1 ];
            verts[vIndex + 10 ] = verts[vIndex + 4 ];
            verts[vIndex + 11 ] = verts[vIndex + 5 ];

            uvst[vIndex] = sprite.uvs[0];
            uvst[vIndex+1] = sprite.uvs[1];
            uvst[vIndex+2] = sprite.uvs[2];
            uvst[vIndex+3] = sprite.uvs[3];
            uvst[vIndex+4] = sprite.uvs[4];
            uvst[vIndex+5] = sprite.uvs[5];
            uvst[vIndex+6] = sprite.uvs[6];
            uvst[vIndex+7] = sprite.uvs[7];
            uvst[vIndex+8] = sprite.uvs[0];
            uvst[vIndex+9] = sprite.uvs[1];
            uvst[vIndex+10] = sprite.uvs[4];
            uvst[vIndex+11] = sprite.uvs[5];
        }
    }
    */
    @Override
    public void drawBatch(ArrayList<GLEntity> entities)
    {
        if(tmpBufferCapacity<entities.size())
        {
            tmpBufferCapacity = entities.size() + 1000;
           // sprts = new GLSprite[tmpBufferCapacity];
            Log.i("TIGL", "GLSprite : increase draw buffer size to " + tmpBufferCapacity);
            verts= new float[6 * 2 * tmpBufferCapacity];
            uvst= new float[6 * 2 * tmpBufferCapacity];
            // uvsCached= new float[6 * 2 * tmpBufferCapacity];
            uvsb = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vbuff = ByteBuffer.allocateDirect(tmpBufferCapacity * 6 * 2 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        uvsb.clear();
        int entityCount = 0;
        Iterator<GLEntity> childIterator = entities.iterator();
        while (childIterator.hasNext()) 
        {
            GLSprite sprite = (GLSprite)childIterator.next();
            sprite.lastDrawOrder = sprite.getScene().currentDrawCount++;
            int vIndex = entityCount * 2 * 6;
            sprite.matrix.mapPoints(verts, vIndex, sprite.vertices, 0, 4);
            verts[vIndex + 8 ] = verts[vIndex + 0 ];
            verts[vIndex + 9 ] = verts[vIndex + 1 ];
            verts[vIndex + 10 ] = verts[vIndex + 4 ];
            verts[vIndex + 11 ] = verts[vIndex + 5 ];

            uvst[vIndex] = sprite.uvs[0];
            uvst[vIndex+1] = sprite.uvs[1];
            uvst[vIndex+2] = sprite.uvs[2];
            uvst[vIndex+3] = sprite.uvs[3];
            uvst[vIndex+4] = sprite.uvs[4];
            uvst[vIndex+5] = sprite.uvs[5];
            uvst[vIndex+6] = sprite.uvs[6];
            uvst[vIndex+7] = sprite.uvs[7];
            uvst[vIndex+8] = sprite.uvs[0];
            uvst[vIndex+9] = sprite.uvs[1];
            uvst[vIndex+10] = sprite.uvs[4];
            uvst[vIndex+11] = sprite.uvs[5];
            entityCount++;
        }
           
        
      

        vbuff.clear();
        vbuff.put(verts, 0, entityCount * 12);
        vbuff.flip();
        
        uvsb.clear();
        uvsb.put(uvst, 0, entityCount * 12);
        uvsb.flip();

        GLShader.drawTexture(vbuff, uvsb, this.textureHandle, entityCount, false);

    }
    

}