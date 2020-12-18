package fr.dzzd.glsprite;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.HashMap;
import org.appcelerator.titanium.TiApplication;


public class BitmapCache
{

    private static HashMap<String,Bitmap> caches = new HashMap<String,Bitmap>();


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
    			bitmap=BitmapFactory.decodeStream(TiApplication.getAppCurrentActivity().getApplicationContext().getAssets().open(filePath), null, bitmapFactoryOptions);
    			caches.put(filePath,bitmap);
    		}
			return bitmap;
		}
		
		BitmapFactory.Options bitmapFactoryOptions=new BitmapFactory.Options();
		bitmapFactoryOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
		bitmapFactoryOptions.inScaled=false;
		return BitmapFactory.decodeStream(TiApplication.getAppCurrentActivity().getApplicationContext().getAssets().open(filePath), null, bitmapFactoryOptions);
    }
}