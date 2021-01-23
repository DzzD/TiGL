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


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.HashMap;
import android.util.Log;


public class BitmapCache
{

	public static Context context;
    private static HashMap<String,Bitmap> caches = new HashMap<String,Bitmap>();


	public static void setContext(Context context)
	{
		BitmapCache.context = context;
	}

	public static Bitmap load(String filePath) throws Exception
	{
		return load(filePath, true);
	}

	public static void clear()
	{
		caches.clear();
	}


    public static Bitmap load(String filePath, boolean useCache) throws Exception
    {
		if(useCache)
		{
    		Bitmap bitmap = caches.get(filePath);
    		if(bitmap == null)
    		{
				bitmap = _load(filePath);
				caches.put(filePath, bitmap);
    		}
			return bitmap;
		}
		
		return _load(filePath);
    }

	private static Bitmap _load(String filePath) throws Exception
	{
		BitmapFactory.Options bitmapFactoryOptions=new BitmapFactory.Options();
		bitmapFactoryOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
		bitmapFactoryOptions.inScaled=false;
		Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(filePath), null, bitmapFactoryOptions);
		Log.i("TIGL","BitmapCache.load(" + filePath + ") size = " + bitmap.getWidth() + "px, " + bitmap.getHeight() + "px");
		return bitmap;
	}
}