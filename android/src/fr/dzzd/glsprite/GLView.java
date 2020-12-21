package fr.dzzd.glsprite;


import org.appcelerator.kroll.common.Log;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import org.appcelerator.kroll.KrollProxy;



import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;

public class GLView 
{
    private GLSurface glSurface;
    private GLRenderer glRenderer;

    
    public GLView()
    {
        super();
        Log.i("GLSprite", "GLView()");

        this.glSurface = new GLSurface(TiApplication.getAppCurrentActivity());
        this.glRenderer = new GLRenderer(this);
		this.glSurface.setRenderer(this.glRenderer);
    }

    public void onCreated()
    {
    }

    
    public void onChanged(int width, int height)
    {
    }

    
    public void onDraw()
    {
    }

    public GLRenderer getGlRenderer()
    {
        return this.glRenderer;
    }

    public GLSurface getGlSurface()
    {
        return this.glSurface;
    }
}