/*
*	© Copyright DzzD, Bruno Augier 2013-2021 (bruno.augier@dzzd.net)
*	 This file is part of TIGL.
*
*    TIGL is free software: you can redistribute it and/or modify
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


/* 
* Conversion of parameters (see https://github.com/appcelerator/titanium_mobile/blob/07592855ee22082c16f25f94155f3c759ba477c5/android/titanium/src/java/org/appcelerator/titanium/util/TiConvert.java)
*/
package fr.dzzd.tigl;


import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;
import java.util.LinkedList;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import android.util.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;


// This proxy can be created by calling Tigl.createTIGLView()
@Kroll.proxy(creatableInModule=TiglModule.class)
public class TIGLViewProxy extends TiViewProxy implements GLViewListener
{
	// Standard Debugging variables
	private static final String LCAT = "ExampleProxy";
	private static final boolean DBG = TiConfig.LOGD;

	private String backgroundColor = "white";
	private String units = "px";
	private TIGLView tiglView;

	private LinkedList<TiglManagerPacket> tiglManagerPackets = new LinkedList<TiglManagerPacket>();


	public TIGLViewProxy()
	{
		super();
		Log.i("TIGL", "TiViewProxy() -- constructor");

	}

	@Override
	public TiUIView createView(Activity activity)
	{
		Log.i("TIGL", "TIGLViewProxy.createView(activity)");
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
		Object backgroundcolor = options.get("backgroundcolor");
		Object backgroundColor = options.get("backgroundColor");
		options.remove("backgroundcolor");
		options.remove("backgroundColor");
		super.handleCreationDict(options); //Should not be called with backgroundcolor
		if(backgroundcolor != null)	options.put("backgroundcolor",backgroundcolor);
		if(backgroundColor != null) options.put("backgroundColor",backgroundColor);

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
		this.processTiglManagerPackets();
		this.fireEvent("loop", new Object());
		this.fireEvent("loopFinished", new Object());
	}

	
	/*
	 * GLViewListener callback onTouch
	 *  fireEvent "touch" to Javascript
	 * */
	public void onTouch(GLTouchEvent glTouchEvent)
	{
		KrollDict e = new KrollDict();
		switch(glTouchEvent.action)
		{
			case GLTouchEvent.ACTION_DOWN :
				e.put("action","down");
			break;
			case GLTouchEvent.ACTION_MOVE :
				e.put("action","move");
			break;
			case GLTouchEvent.ACTION_UP :
				e.put("action","up");
			break;
			case GLTouchEvent.ACTION_CANCEL :
				e.put("action","cancel");
			break;
			default:
				e.put("action","unknown");
			break;
				
		}
		e.put("pointer",glTouchEvent.pointer);
		e.put("x",glTouchEvent.x);
		e.put("y",glTouchEvent.y);
		e.put("sceneX",glTouchEvent.sceneX);
		e.put("sceneY",glTouchEvent.sceneY);
		e.put("entityId",glTouchEvent.entityId);
		this.fireEvent("touch", e);
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

	private void processTiglManagerPackets()
	{
		GLScene scene = this.tiglView.getScene();
		HashMap<Integer,GLEntity> entities= scene.getEntities();
		synchronized(this.tiglManagerPackets)
		{
			while(!this.tiglManagerPackets.isEmpty())
			{
				TiglManagerPacket packet = this.tiglManagerPackets.removeFirst();
				
				switch(packet.getType())
				{
					case TiglManagerPacket.POSITIONS_PACKED :
					{
						int[] datas = (int[]) packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id=datas[n];
							int packed=datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							glEntity.x=((packed >> 16) & 0xFFFF) - 32768;
							glEntity.y=(packed &0xFFFF) - 32768;
						}
					}
					break;
					
					case TiglManagerPacket.ROTATIONS_PACKED :
					{
						int[] datas = (int[]) packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id=datas[n];
							int packed=datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							glEntity.r= packed * 360.0f / 0x1000000;
						}
					}				
					break;

					case TiglManagerPacket.SCALES_PACKED :
					{
						int[] datas = (int[]) packet.getDatas();
						for(int n = 0; n < datas.length; n+=3)
						{
							int id=datas[n];
							int scaleX=datas[n+1];
							int scaleY=datas[n+2];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							glEntity.sx= scaleX / 10000f;
							glEntity.sy= scaleY / 10000f;
						}
					}
					break;
					
					case TiglManagerPacket.PIVOTS_PACKED :
					{
						int[] datas = (int[]) packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id=datas[n];
							int packed=datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							glEntity.px=((packed >> 16) & 0xFFFF) - 32768;
							glEntity.py=(packed &0xFFFF) - 32768;
						}
					}
					break;
/*
					case TiglManagerPacket.TEXTS_PACKED :
					{
						Object[] datas = (Object[])packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id=(int)datas[n];
							String text=(String)datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							if(glEntity instanceof GLText)
							{
								((GLText)glEntity).setText(text);
							}
						}
					}
					break;
*/
					
					case TiglManagerPacket.COLORS_PACKED :
					{
						long[] datas = (long[])packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id = (int)datas[n];
							int color = (int)datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							if(glEntity instanceof GLText)
							{
								((GLText)glEntity).color = color;
							}
						}
					}
					break;

					
					case TiglManagerPacket.OUTLINE_COLORS_PACKED :
					{
						long[] datas = (long[])packet.getDatas();
						for(int n = 0; n < datas.length; n+=2)
						{
							int id = (int)datas[n];
							int color = (int)datas[n+1];
							GLEntity glEntity =  entities.get(id);
							if(glEntity == null) continue;
							if(glEntity instanceof GLText)
							{
								((GLText)glEntity).outlineColor = color;
							}
						}
					}
					break;
				}
			}
		}
	}
	
		

	
	@Kroll.method
	public void removeEntityById(int id)
	{
		GLScene scene = this.tiglView.getScene();
		synchronized(scene)
		{
			scene.getEntityById(id).remove();
		}
	}

	@Kroll.method
	public void setEntityLayerById(int id, int layer)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.layer = layer;
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
	public KrollDict setEntityTextById(int id, String text)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		KrollDict properties= new KrollDict();
		if(glEntity instanceof GLText)
		{
			((GLText)glEntity).setText(text);
			properties.put("width", glEntity.width);
			properties.put("height", glEntity.height);
		}
		return properties;
	}

	@Kroll.method
	public void setEntityColorById(int id, int color)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		if(glEntity instanceof GLText)
		{
			((GLText)glEntity).color = color;
		}
	}

	
	
	@Kroll.method
	public void setEntityOutlineColorById(int id, int color)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		if(glEntity instanceof GLText)
		{
			((GLText)glEntity).outlineColor = color;
		}
	}

	@Kroll.method
	public void setTouchEnabledById(int id, boolean touchEnabled)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		glEntity.touchEnabled = touchEnabled;
	}
	
	@Kroll.method
	public void playEntityAnimationById(int id, KrollDict options)
	{
		GLSprite glSprite = (GLSprite) this.tiglView.getScene().getEntityById(id);
		glSprite.playAnimation(options);
	}



	/*
	 * Receive packets of positions and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesPositionsPacked(int[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.POSITIONS_PACKED, datas));	
		}
	}
	
	/*
	 * Receive packets of rotations and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesRotationsPacked(int[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.ROTATIONS_PACKED, datas));	
		}	
	}

	
	/*
	 * Receive packets of scales and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesScalesPacked(int[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.SCALES_PACKED, datas));		
		}
	}

	/*
	 * Receive packets of pivots and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesPivotsPacked(int[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.PIVOTS_PACKED, datas));		
		}
	}

	
	/*
	 * Receive packets of text and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    // @Kroll.method
	// public void setEntitiesTextsPacked(Object[] datas)
	// {
	// 	synchronized(this.tiglManagerPackets)
	// 	{
	// 		this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.TEXTS_PACKED, datas));		
	// 	}
	// }

	/*
	 * Receive packets of colors and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesColorsPacked(long[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.COLORS_PACKED, datas));		
		}
	}


	/*
	 * Receive packets of outline colors and add it to tiglManagerPackets
	 *  => datas will be processed on the beginning of the next loop
	 */
    @Kroll.method
	public void setEntitiesOutlineColorsPacked(long[] datas)
	{
		synchronized(this.tiglManagerPackets)
		{
			this.tiglManagerPackets.add(new TiglManagerPacket(TiglManagerPacket.OUTLINE_COLORS_PACKED, datas));		
		}
	}


/*
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
*/
	

/*
	@Kroll.method
	public int testPerf(int test)
	{
		return test+1;
	}

	*/

	@Kroll.method
	public GLScene getScene()
	{
		return this.tiglView.getScene();
	}

	
	@Kroll.method
	public void setSceneScale(float sx, float sy)
	{
		GLScene glScene =  this.tiglView.getScene();
		glScene.sx = sx;
		glScene.sy = sy;
	}

	@Kroll.method
	public int addEntity(KrollDict options)
	{
			GLEntity entity = new GLEntity(options);
			this.tiglView.getScene().add(entity);
			return entity.id;
	}


	
	@Kroll.method
	public int addShape(KrollDict options)
	{
			GLShape shape = new GLShape(options);
			this.tiglView.getScene().add(shape);
			return shape.id;
	}
	
	@Kroll.method
	public int addSprite(KrollDict options)
	{
			GLSprite sprite = new GLSprite(options);
			this.tiglView.getScene().add(sprite);
			return sprite.id;
	}

	
	@Kroll.method
	public int addText(KrollDict options)
	{
			GLText text = new GLText(options);
			this.tiglView.getScene().add(text);
			return text.id;
	}

	
	@Kroll.method
	public KrollDict getEntitySizeById(int id)
	{
		GLEntity glEntity =  this.tiglView.getScene().getEntityById(id);
		KrollDict properties= new KrollDict();
		properties.put("width", glEntity.width);
		properties.put("height", glEntity.height);
		return properties;
	}

	@Kroll.method
	public void setEntityParentById(int id, int idParent)
	{
		GLScene glScene = this.tiglView.getScene();
		GLEntity glEntity = glScene.getEntityById(id);
		GLEntity glEntityParent = glScene.getEntityById(idParent);
		glEntityParent.add(glEntity);
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



/*
	@Kroll.method
	public void updateBulkModeXY(int[] datas, boolean packetFull)
	{
		this.tiglView.getScene().updateBulkModeXY(datas, packetFull);
	}
	*/

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

	
	@Kroll.getProperty @Kroll.method
	public KrollDict getTimersUs()
	{
		KrollDict kd = new KrollDict();
		kd.put("opengl", this.tiglView.getGLView().getTimeOpenglUs());
		kd.put("matrix", this.tiglView.getGLView().getTimeMatrixUs());
		kd.put("javascript", this.tiglView.getGLView().getTimeJavascriptUs());
		kd.put("fps", this.tiglView.getGLView().getTimeFpsUs());
		kd.put("idle", this.tiglView.getGLView().getTimeIdleUs());
		return kd;
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



}
