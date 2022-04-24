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

/*
 * BitmapFont is an implementation to decode, use and display fonts files produced by BMFont tools, see https://www.angelcode.com/products/bmfont/ 
 */

package fr.dzzd.tigl;


import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.io.DataInputStream;
import java.io.File;

public class BitmapFont
{
    

	public static Context context;
    private static HashMap<String,BitmapFont> caches = new HashMap<String,BitmapFont>();

    
    public int version;
    public int fontSize;
    public int format; //bit 0: smooth, bit 1: unicode, bit 2: italic, bit 3: bold, bit 4: fixedHeigth, bits 5-7: reserved
    public int outlineWidth;
    public int lineHeight;
    public int baseLineTop;
    public int width;
    public int height;
    public String textureFileName;
    public int charCount;
    public BitmapChar defaultChar;
    public HashMap<Integer,BitmapChar> chars;

    private BitmapFont()
    {

    }
    
	public static void setContext(Context context)
	{
		BitmapFont.context = context;
	}

	public static BitmapFont load(String filePath) throws Exception
	{
		return load(filePath, true);
	}

	public static void clear()
	{
		caches.clear();
	}

    public static BitmapFont load(String filePath, boolean useCache) throws Exception
    {
        if(useCache)
		{
    		BitmapFont bitmapFont = caches.get(filePath);
    		if(bitmapFont == null)
    		{
				bitmapFont = new BitmapFont(filePath);
				caches.put(filePath, bitmapFont);
    		}
			return bitmapFont;
		}

		BitmapFont bitmapFont = new BitmapFont(filePath);
		return bitmapFont;
    }

	private BitmapFont(String filePath) throws Exception
	{
		this.chars = new HashMap<Integer,BitmapChar>();

        DataInputStream dis = new DataInputStream(context.getAssets().open(filePath));
        Log.i("TIGL","BitmapFont.load(" + filePath +")");

        
        String signature = "" + (char)dis.readUnsignedByte() + (char)dis.readUnsignedByte() + (char)dis.readUnsignedByte();
        Log.i("TIGL","BitmapFont.load(" + filePath +"), file signature is '" + signature + "'");
        if(!"BMF".equals(signature))
        {
            throw new Exception("Error reading font " + filePath + " invalid file signature '" + signature + "' should be BMF");
        }
        
        this.version = dis.readUnsignedByte();
        if(this.version < 3)
        {
            throw new Exception("Error reading font " + filePath + " invalid version " + this.version + " should be 3 or more");
        }

        int blockType = dis.readUnsignedByte();
        int blockSize = this.readInt(dis);
        if(blockType != 1)
        {
            throw new Exception("Error reading font " + filePath + " invalid block type " + blockType + ", should be 1");
        }
        this.fontSize = this.readShort(dis);
        this.format = dis.readUnsignedByte(); //bit 0: smooth, bit 1: unicode, bit 2: italic, bit 3: bold, bit 4: fixedHeigth, bits 5-7: reserved
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), fontSize = " + this.fontSize);

        //Skip block partially
        long skipped = dis.skip(10);
        while (skipped < 10) 
        {
            skipped += dis.skip(10 - skipped);
        }


        this.outlineWidth = dis.readUnsignedByte();
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), outline width = " + this.outlineWidth);

        //Skip font name
        String fontName = "";
        byte ch;
        while( (ch = dis.readByte()) != 0) fontName += (char)ch;
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), font name is '" + fontName + "'");
        

        blockType = dis.readUnsignedByte();
        blockSize = this.readInt(dis);
        if(blockType != 2)
        {
            throw new Exception("Error reading font " + filePath + " invalid block type " + blockType + ", should be 2");
        }
        this.lineHeight = this.readShort(dis);
        this.baseLineTop = this.readShort(dis);
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), lineHeight = " + this.lineHeight);
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), baseLineTop = " + this.baseLineTop);

        
        this.width = this.readShort(dis);
        this.height = this.readShort(dis);
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), texture width = " + this.width);
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), texture height = " + this.height);

        int pageCount = this.readShort(dis);
        if(pageCount != 1)
        {
            throw new Exception("Error reading font " + filePath + " invalid number of page " + pageCount + ", should be 1");
        }

        int packed = dis.readByte(); //bits 0-6: reserved, bit 7: packed
        if((packed & 0x80) != 0)
        {
            throw new Exception("Error reading font " + filePath + " , packed mode is not supported");
        }

        int a = dis.readByte();
        int r = dis.readByte();
        int g = dis.readByte();
        int b = dis.readByte();
        
        Log.i("TIGL", "BitmapFont.load(" + filePath +"), ARGB format  = (" + a + "," + r + "," + g + "," + b + ")");
        if(r != 0)
        {
            Log.e("TIGL", "Error reading font " + filePath + " , bad format, red should contains glyph");
        }

        if(g != 1)
        {
            Log.e("TIGL", "Error reading font " + filePath + " , bad format, green should contains outline");
        }

        blockType = dis.readUnsignedByte();
        blockSize = this.readInt(dis);
        if(blockType != 3)
        {
            throw new Exception("Error reading font " + filePath + " invalid block type " + blockType + ", should be 3");
        }

        String fileName = "";
        while( (ch = dis.readByte()) != 0) fileName += (char)ch;
        String path = new File(filePath).getParent();
        this.textureFileName = path + "/" + fileName;
        Log.i("TIGL","BitmapFont.load(" + filePath + "), texture file is " + this.textureFileName);

        blockType = dis.readUnsignedByte();
        blockSize = this.readInt(dis);
        if(blockType != 4)
        {
            throw new Exception("Error reading font " + filePath + " invalid block type " + blockType + ", should be 4");
        }

        this.charCount = blockSize / 20;
        Log.i("TIGL","BitmapFont.load(" + filePath + "), char count is " + this.charCount);

        for(int n = 0; n < this.charCount; n++)
        {
            BitmapChar bChar = new BitmapChar(dis);
            if(n == 0)
            {
                this.defaultChar = bChar;
            }
            this.chars.put(Integer.valueOf(bChar.id), bChar);
        }

        if(dis.available() != 0)
        { 
            blockType = dis.readUnsignedByte();
            if(blockType == 5)
            {
                Log.i("TIGL","BitmapFont.load(" + filePath + "), kerning available but not yet supported");
            }
            else
            {
                Log.i("TIGL","BitmapFont.load(" + filePath + "), unknown remaining data on file");
            }
        }
        else
        {
            Log.i("TIGL","BitmapFont.load(" + filePath + "), no kerning for this font"); 
        }

        dis.close();
	}

    public float[] getUvs(String str)
    {
        int charCount = str.length();
        float[] uvs = new float[charCount * 12];
        for(int n = 0; n < charCount; n++)
        {
            BitmapChar ch = this.chars.get(Integer.valueOf(str.charAt(n)));
            if(ch == null)
            {
                ch = this.defaultChar;
                Log.e("TIGL", "BitmaFont, invalid character " + str.charAt(n) + "'");
            }            
            float[] cUvs = ch.getUvs();
            for(int c = 0; c < 12; c++)
            {
                uvs[n*12 + c] = cUvs[c];
            }
        }
        return uvs;
    }

    
    public float[] getVertices(String str)
    {
        int charCount = str.length();
        float[] vertices = new float[charCount * 12];
        float cursorPosition = 0;
        for(int n = 0; n < charCount; n++)
        {
            BitmapChar ch = this.chars.get(Integer.valueOf(str.charAt(n)));
            if(ch == null)
            {
                ch = this.defaultChar;
                Log.e("TIGL", "BitmaFont, invalid character '" + str.charAt(n) + "'");
            }            
            if(n == 0) {cursorPosition = -ch.xOffset;};
            float[] cVertices = ch.getVertices();
            for(int c = 0; c < 12; c++)
            {
                vertices[n*12 + c] = cVertices[c];
            }
            
            vertices[n*12 + 0] += cursorPosition;
            vertices[n*12 + 2] += cursorPosition;
            vertices[n*12 + 4] += cursorPosition;
            vertices[n*12 + 6] += cursorPosition;
            vertices[n*12 + 8] += cursorPosition;
            vertices[n*12 + 10] += cursorPosition;
            cursorPosition += ch.xAdvance;
        }
        return vertices;
    }
    

    class BitmapChar
    {
        public int id;
        public int x;
        public int y;
        public int width;
        public int height;
        public int xOffset;
        public int yOffset;
        public int xAdvance;
        public int page;
        public int channel;
        private float[] uvs;
        private float[] vertices;

        public BitmapChar(DataInputStream dis) throws Exception
        {
            this.id = BitmapFont.this.readInt(dis);
            this.x = BitmapFont.this.readShort(dis);
            this.y = BitmapFont.this.readShort(dis);
            this.width = BitmapFont.this.readShort(dis);
            this.height = BitmapFont.this.readShort(dis);
            this.xOffset = BitmapFont.this.readShort(dis);
            this.yOffset = BitmapFont.this.readShort(dis);
            this.xAdvance = BitmapFont.this.readShort(dis);
            this.page = dis.readUnsignedByte();
            this.channel = dis.readUnsignedByte();
            this.setUvs();
            this.setVertices();
        }
        
        private void setUvs()
        {
            this.uvs = new float[12];
            float x=(float)this.x / (float)BitmapFont.this.width;
            float y=(float)this.y / (float)BitmapFont.this.height;
            float width = (float)this.width / (float)BitmapFont.this.width;
            float height = (float)this.height / (float)BitmapFont.this.height;
            
            this.uvs[0] = x;
            this.uvs[1] = y + height;
            this.uvs[2] = x;
            this.uvs[3] = y;
            this.uvs[4] = x + width;
            this.uvs[5] = y;
            this.uvs[6] = x + width;
            this.uvs[7] = y + height;
            this.uvs[8] = x;
            this.uvs[9] = y + height;
            this.uvs[10] = x + width;
            this.uvs[11] = y;
        }

        private void setVertices()
        {
            this.vertices = new float[12];
            float x=this.xOffset;
            float y=this.yOffset;
            float width = this.width;
            float height = this.height;
            this.vertices[0] = x;
            this.vertices[1] = y + height;
            this.vertices[2] = x;
            this.vertices[3] = y;
            this.vertices[4] = x + width;
            this.vertices[5] = y;
            this.vertices[6] = x + width;
            this.vertices[7] = y + height;
            this.vertices[8] = x;
            this.vertices[9] = y + height;
            this.vertices[10] = x + width;
            this.vertices[11] = y;
        }

        public float[] getUvs()
        {
            return this.uvs;
        }

        public float[] getVertices()
        {
            return this.vertices;
        }

    }


    private int readInt(DataInputStream dis) throws Exception
    {
        int a = dis.readUnsignedByte();
        int b = dis.readUnsignedByte();
        int c = dis.readUnsignedByte();
        int d = dis.readUnsignedByte();
        return (d<<24&0xFF000000) | (c<<16&0xFF0000) | (b<<8&0xFF00) | (a&0xFF);
    }

    
    private short readShort(DataInputStream dis) throws Exception
    {
        int a = dis.readUnsignedByte();
        int b = dis.readUnsignedByte();
        return (short)((b<<8&0xFF00) | (a&0xFF));
    }


    
}