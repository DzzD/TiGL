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

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import android.util.Log;
import org.appcelerator.kroll.common.TiConfig;


@Kroll.module(name="Tigl", id="fr.dzzd.tigl")
public class TiglModule extends KrollModule
{

	// Standard Debugging variables
	private static final String LCAT = "TiglModule";
	private static final boolean DBG = TiConfig.LOGD;



	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;

	public TiglModule()
	{
		super();
		Log.i("TIGL", "TiglModule() -- constructor");
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.i("TIGL", "TiglModule.onAppCreate(TiApplication)");
		// put module init code that needs to run when the application is created
		
        BitmapCache.setContext(app);
        BitmapFont.setContext(app);
		/*
		try
		{
			BitmapFont.load("Resources/Kalam.fnt");
		}
		catch(Exception e)
		{
			Log.e("TIGL", e.getMessage());
			e.printStackTrace();
		}*/
	}

	// Methods
	@Kroll.method
	public String example()
	{
		Log.i("TIGL", "TiglModule.example()");
		return "hello world";
	}

	// Properties
	@Kroll.method
	@Kroll.getProperty
	public String getExampleProp()
	{
		Log.i("TIGL", "TiglModule.getExampleProp()");
		return "hello world";
	}


	@Kroll.method
	@Kroll.setProperty
	public void setExampleProp(String value)
	{
		Log.i("TIGL", "TiglModule.setExampleProp(" + value + ")");
	}

}

