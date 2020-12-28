/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2017 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package fr.dzzd.glsprite;



import java.util.HashMap;
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

import android.app.Activity;


// This proxy can be created by calling Glsprite.createTIGLView()
@Kroll.proxy(creatableInModule=GlspriteModule.class)
public class TIGLViewProxy extends TiViewProxy implements GLViewListener
{
	// Standard Debugging variables
	private static final String LCAT = "ExampleProxy";
	private static final boolean DBG = TiConfig.LOGD;

	private TIGLView tiGlView;


	@Override
	public TiUIView createView(Activity activity)
	{
		Log.i("GLSprite", "GLViewProxy.createView(activity)");
		this.tiGlView = new TIGLView(this);
		return this.tiGlView;
	}
/*
	public class TView extends TiUIView
	{

		public TView(GLViewProxy proxy, Activity activity)
		{
			super(proxy);
			proxy.glView = new GLView();
			this.setNativeView(proxy.glView);
			this.getLayoutParams().autoFillsHeight = true;
			this.getLayoutParams().autoFillsWidth = true;

			Log.i("GLSprite", "TView(TiViewProxy, GLView)");

		}
	}
*/
	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);

		Log.i("GLSprite", "GLViewProxy.handleCreationDict(KrollDict)");

		if (options.containsKey("message")) 
		{
			Log.i("GLSprite", "GLViewProxy.handleCreationDict(KrollDict) => option.containsKey('message')");
		}
		

	}



    public void onInit()
    {
		this.fireEvent("init", new Object());
    }
    
    public void onResize(int width, int height)
    {
		HashMap<String, String> event = new HashMap<String, String>();
		event.put("width", String.valueOf(width));
		event.put("height", String.valueOf(height));
		this.fireEvent("resize", event);
	}
	
    public void onLoop()
    {
		this.fireEvent("loop", new Object());
	}
	

	@Kroll.method
	public void setEntityPos(int index, float x, float y, float r, float sx, float sy, float px, float py)
	{
		GLEntity glEntity =  this.getScene().getChildAt(index);
		glEntity.x = x;
		glEntity.y = y;
		glEntity.r = r;
		glEntity.sx = sx;
		glEntity.sy = sy;
		glEntity.px = px;
		glEntity.py = py;
	}

	
	@Kroll.method
	public void setEntityPos(int index[], int x[], int y[], int count)//, float r, float sx, float sy, float px, float py)
	{
		Vector<GLEntity> flattenedEntities = new Vector<GLEntity>();
		this.getScene().getFlattenedEntities(flattenedEntities);
		for(int n = 0; n < count; n++)
		{
			GLEntity glEntity =  this.getScene().getChildAt(index[n]);
			glEntity.x=x[n];
			glEntity.y=y[n];
		}
	}

	
	@Kroll.method
	public int addSprite(HashMap<String,Object> options)
	{
		GLSprite sprite = new GLSprite(options);
		this.getScene().add(sprite);
		return sprite.id;
	}

	@Kroll.method
	public KrollDict[] getFullScene()
	{
		Vector<KrollDict> v = new Vector<KrollDict>();
		this.getScene().getProperties(v);
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
		return this.tiGlView.getScene();
	}

	@Kroll.method
	public int addSprite(KrollDict options)
	{
		GLSprite sprite = new GLSprite(options);
		this.tiGlView.getScene().add(sprite);
		return sprite.id;
	}

	@Kroll.method
	public void addSprite2()
	{
	}

	@Kroll.method
	public void updateBulkModeXY(int[] datas, boolean packetFull)
	{
		this.tiGlView.getScene().updateBulkModeXY(datas, packetFull);
	}
	

	
	

	@Kroll.setProperty @Kroll.method
	public void setMessage(String message)
	{
	    Log.i("GLSprite", "ExampleProxy.setMessage(" + message + ")");
	}

}