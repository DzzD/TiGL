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
        this.glView.setUnits(proxy.getUnits());
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
