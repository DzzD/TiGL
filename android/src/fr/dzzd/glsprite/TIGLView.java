package fr.dzzd.glsprite;

import android.view.ViewGroup;
import android.view.View;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.view.TiUIView;

public class TIGLView extends TiUIView //implements GLViewListener
{
    private GLView glView;
    private TIGLViewProxy proxy;

    public TIGLView(TIGLViewProxy proxy)
    {
        super(proxy);
        this.proxy = proxy;
		this.getLayoutParams().autoFillsHeight = true;
		this.getLayoutParams().autoFillsWidth = true;
        this.glView = new GLView();
        this.glView.setBackgroundColor(proxy.getBackgroundcolor());
        this.setNativeView(this.glView);
        this.glView.setGLViewListener(proxy);
    }

    public GLScene getScene()
    {
        return this.glView.getScene();
    }

    
    public GLView  getGLView()
    {
        return this.glView;
    }

}
