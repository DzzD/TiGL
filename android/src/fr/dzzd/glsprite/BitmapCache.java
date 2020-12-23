package fr.dzzd.glsprite;


import android.app.Activity;
import org.appcelerator.titanium.TiApplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.HashMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.res.AssetManager;


public class BitmapCache
{

    private static HashMap<String,Bitmap> caches = new HashMap<String,Bitmap>();
	public static Context context;

	public static Bitmap load(String filePath) throws Exception
	{
		return load(filePath, true);
	}


    public static Bitmap load(String filePath, boolean useCache) throws Exception
    {
		if(useCache)
		{
    		Bitmap bitmap = caches.get(filePath);
    		if(bitmap==null)
    		{
	    		BitmapFactory.Options bitmapFactoryOptions=new BitmapFactory.Options();
    			bitmapFactoryOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
				bitmapFactoryOptions.inScaled=false;
				BitmapCache.context = TiApplication.getAppCurrentActivity().getApplicationContext();
				bitmap=BitmapFactory.decodeStream(BitmapCache.context.getAssets().open(filePath), null, bitmapFactoryOptions);
				//bitmap=BitmapFactory.decodeFile(filePath, bitmapFactoryOptions);
    			// bitmap=BitmapFactory.decodeStream(TiApplication.getAppCurrentActivity().getApplicationContext().getAssets().open(filePath), null, bitmapFactoryOptions);
				Log.i("GLSprite","BitmapCache.load(" + filePath + ")");
				//bitmap=BitmapFactory.decodeStream(TiApplication.getAppCurrentActivity().getAssets().open(filePath), null, bitmapFactoryOptions);
				Log.i("GLSprite","Bitmap width = " + bitmap.getWidth() + "px");
				Log.i("GLSprite","Bitmap height = " + bitmap.getHeight() + "px");
				
				
				caches.put(filePath,bitmap);
				/*
				String[] list = TiApplication.getAppCurrentActivity().getApplicationContext().getAssets().list(".");
				for(int i = 0;i<list.length; i++)
				{
					Log.i("GLSprite", "BitmapCache:file " + list);
				}*/
    		}
			return bitmap;
		}
		
		BitmapFactory.Options bitmapFactoryOptions=new BitmapFactory.Options();
		bitmapFactoryOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
		bitmapFactoryOptions.inScaled=false;
		return BitmapFactory.decodeStream(TiApplication.getAppCurrentActivity().getApplicationContext().getAssets().open(filePath), null, bitmapFactoryOptions);
    }
}