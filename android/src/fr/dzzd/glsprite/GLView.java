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
import android.opengl.GLSurfaceView;

public class GLView extends GLSurfaceView
{
    //private GLSurface glSurface;
    private GLRenderer glRenderer;

    
    public GLView() 
    {
        super(TiApplication.getAppCurrentActivity());
        Log.i("GLSprite", "GLView()");

        //this.glSurface = new GLSurface(TiApplication.getAppCurrentActivity());
        
        //this.glSurface.setRenderer(this.glRenderer);
        //this.setRenderer(this.glRenderer);
    }

    public void start()
    {
        this.setEGLContextClientVersion(2);
        this.glRenderer = new GLRenderer(this);
        this.setRenderer(this.glRenderer);

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
/*
    public GLSurface getGlSurface()
    {
        return this.glSurface;
    }
    */
}