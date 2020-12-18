package fr.dzzd.glsprite;


import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;


public class GLView extends TiUIView
{
    private GLSurface glSurface;
    private GLRenderer glRenderer;

    public GLView(ExampleProxy proxy)
    {
        super(proxy);

        Log.i("GLSprite", "GLView(TiViewProxy)");

        this.glSurface = new GLSurface(proxy.getActivity());
        this.glRenderer = new GLRenderer(proxy);
		this.glSurface.setRenderer(this.glRenderer);
		this.setNativeView(this.glSurface);

    }

    @Override
    public void processProperties(KrollDict d)
    {
        super.processProperties(d);

        Log.i("GLSprite", "GLView.processProperties(KrollDict)");
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