/*
*	© Copyright DzzD, Bruno Augier 2013-2021 (bruno.augier@dzzd.net)
*	This file is part of TIGL.
*
*   TIGL is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    TIGL is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*	 along with TIGL.  If not, see <https://www.gnu.org/licenses/>
*/


package fr.dzzd.glsprite;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;


// This proxy can be created by calling Glsprite.createTIGLView()
@Kroll.proxy(creatableInModule=GlspriteModule.class)
public class TIGLViewProxy extends TiViewProxy implements GLViewListener
{
	// Standard Debugging variables
	private static final String LCAT = "ExampleProxy";
	private static final boolean DBG = TiConfig.LOGD;

	private String backgroundColor = "white";
	private String units = "px";
	private TIGLView tiglView;


	@Override
	public TiUIView createView(Activity activity)
	{
		Log.i("GLSprite", "GLViewProxy.createView(activity)");
		this.tiglView = new TIGLView(this);
		return this.tiglView;
	}

	/*
	 * Handle creation options (Alloy tag attributes)
	 * Parent call is disabled as it is bugged for backgroundColor
	 * */
	@Override
	public void handleCreationDict(KrollDict options)
	{
		//super.handleCreationDict(options); //Should not be called


		if (options.containsKey("units")) 
		{
			Log.i("TIGL", "TIGLView units detected " + options.get("units"));
			this.setUnits((String)options.get("units"));
		}

		if (options.containsKey("backgroundcolor")) 
		{
			Log.i("TIGL", "TIGLView backgroundcolor detected " + options.get("backgroundcolor"));
			this.setBackgroundcolor((String)options.get("backgroundcolor"));
		}

		
		if (options.containsKey("backgroundColor")) 
		{
			Log.i("TIGL", "TIGLView backgroundColor detected " + options.get("backgroundColor"));
			this.setBackgroundcolor((String)options.get("backgroundColor"));
		}

	}


	/*
	 * GLViewListener callback onInit
	 *  fireEvent "init" to Javascript
	 * */
    public void onInit()
    {
		this.fireEvent("init", new Object());
    }
	
	
	/*
	 * GLViewListener callback onResize
	 *  fireEvent "resize" to Javascript
	 * */
    public void onResize(float width, float height, String units)
    {
		HashMap<String, String> event = new HashMap<String, String>();
		event.put("width", String.valueOf(width));
		event.put("height", String.valueOf(height));
		event.put("units", units);
		this.fireEvent("resize", event);
	}
	
	/*
	 * GLViewListener callback onLoop
	 *  fireEvent "loop" to Javascript
	 * */
    public void onLoop()
    {
		this.fireEvent("loop", new Object());
	}

	/* 
	 * Pause the OpenGL rendering thread
	 */
	@Kroll.method
	public void pause()
	{
		this.tiglView.getGLView().onPause();
	}

	/* 
	 * Resume the OpenGL rendering thread
	 */
	@Kroll.method
	public void resume()
	{
		this.tiglView.getGLView().onResume();
	}

	
	@Kroll.method
	public void setEntityPositionById(int id, float x, float y)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.x = x;
		glEntity.y = y;
	}

	
	@Kroll.method
	public void setEntityRotationById(int id, float r)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.r = r;
	}

	
	@Kroll.method
	public void setEntityScaleById(int id, float sx, float sy)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.sx = sx;
		glEntity.sy = sy;
	}
	
	@Kroll.method
	public void setEntityPivotById(int id, float px, float py)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.px = px;
		glEntity.py = py;
	}



	@Kroll.method
	public void setEntityPos(int index, float x, float y, float r, float sx, float sy, float px, float py)
	{
	

			GLEntity glEntity =  this.tiglView.getScene().getChildAt(index);
			if(glEntity == null)
			{
				return;
			}
			glEntity.x = x;
			glEntity.y = y;
			glEntity.r = r;
			glEntity.sx = sx;
			glEntity.sy = sy;
			glEntity.px = px;
			glEntity.py = py;
		
	}

	@Kroll.method
	public void wakeup() throws Exception
	{
		Log.i("TIGL", " TIGLViewProxy Wakeup");
	}
	
	@Kroll.method
	public void setEntityPos(int index[], float x[], float y[], int count)//, float r, float sx, float sy, float px, float py)
	{
		//ArrayList<GLEntity> flattenedEntities = new ArrayList<GLEntity>();
		//this.tiglView.getScene().getFlattenedEntities(flattenedEntities);
		return;
		/*
		for(int n = 0; n < count; n++)
		{

			GLEntity glEntity =  this.tiglView.getScene().getChildAt(index[n]);
			glEntity.x=x[n];
			glEntity.y=y[n];
		}
		*/
	}

	/*
	 * Set position for a list of entities
	 * 
	 * We use Explicit conversion because Implicit (see https://github.com/appcelerator/titanium_mobile/blob/07592855ee22082c16f25f94155f3c759ba477c5/android/titanium/src/java/org/appcelerator/titanium/util/TiConvert.java)
     *  créate un unecessary array, also explicit conversion enable mixing int and float
	 */
	
    @Kroll.method
	public void setEntityPosPacked(int[] r)//, float r, float sx, float sy, float px, float py)
	{
		//ArrayList<GLEntity> flattenedEntities = new ArrayList<GLEntity>();
		//this.tiglView.getScene().getFlattenedEntities(flattenedEntities);

		//int[] r = TiConvert.toIntArray((Object[])packPosXY[0]);
		//Object[] r = (Object[])packPosXY[0];
		for(int n = 0; n < r.length; n+=2)
		{
			int id=r[n];
			int packedPosXY=r[n+1];
			GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
			glEntity.x=((packedPosXY >> 16) & 0xFFFF) - 32768;
			glEntity.y=(packedPosXY &0xFFFF) - 32768;
		}
		
	}


	@Kroll.method
	public KrollDict[] getFullScene()
	{
		Vector<KrollDict> v = new Vector<KrollDict>();
		this.tiglView.getScene().getProperties(v);
		KrollDict[] k = new KrollDict[v.size()];

		int n =0;
		for (Enumeration<KrollDict> e = v.elements(); e.hasMoreElements();)
        {
            k[n++]=e.nextElement();
		}
		return k;
	}

	


	@Kroll.method
	public int testPerf(int test)
	{
		return test+1;
	}

	

	@Kroll.method
	public GLScene getScene()
	{
		return this.tiglView.getScene();
	}

	
	@Kroll.method
	public int addSprite(KrollDict options)
	{
			GLSprite sprite = new GLSprite(options);
			this.tiglView.getScene().add(sprite);
			return sprite.id;
	}

/*
	
	@Kroll.method
	public int addSprite(HashMap<String,Object> options)
	{
		synchronized(this.getScene())
		{
			GLSprite sprite = new GLSprite(options);
			this.tiglView.getScene().add(sprite);
			return sprite.id;
		}
		
	}

	*/




	@Kroll.method
	public void updateBulkModeXY(int[] datas, boolean packetFull)
	{
		this.tiglView.getScene().updateBulkModeXY(datas, packetFull);
	}

	@Kroll.setProperty @Kroll.method
	public void setBackgroundcolor(String color)
	{
		if(this.tiglView != null)
		{
			this.tiglView.getGLView().setBackgroundColor(color);
		}
		this.backgroundColor = color;
	}

	@Kroll.getProperty @Kroll.method
	public String getBackgroundcolor()
	{
		return this.backgroundColor;
	}

	
	@Kroll.setProperty @Kroll.method
	public void setUnits(String units)
	{
		if(this.tiglView != null)
		{
			this.tiglView.getGLView().setUnits(units);
		}
		this.units = units;
	}

	
	@Kroll.getProperty @Kroll.method
	public String getUnits()
	{
		return this.units;
	}


	@Kroll.setProperty @Kroll.method
	public void setMessage(String message)
	{
	    Log.i("TIGL", "ExampleProxy.setMessage(" + message + ")");
	}

}
