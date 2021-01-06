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


import android.graphics.Matrix;
import org.appcelerator.kroll.common.Log;
import java.util.*;
import org.appcelerator.kroll.KrollDict;

public class GLEntity
{
    private static int idGenerator = 0;

    public final static int GL_ENTITY = 0;
    public final static int GL_SCENE = 1;
    public final static int GL_SPRITE = 2;
    
    /*
     * Unique identifier for this entity
     */
    public int id;

    /*
     * An integer that identify the underlying implementation
     */
    public int type;

    /*
     * Helper matrices used to convert from Android matrix
     *   USED INTERNALLY, MUST NOT BE MODIFIED DIRECTLY
     */
    public float[] matrix4x4 = new float[16];
    public float[] matrix3x3 = new float[16];

    /*
     * Position in parent space
     */
    public float x;
    public float y;

    /*
     * Pivot location for rotation and scale
     */
    public float px;
    public float py;

    /*
     * Scale factors (centered on the pivot)
     */
    public float sx;
    public float sy;


    /*
     * Rotation (centered on the pivot)
     */
    public float r;
    
    /*
     * width and height of this entity
     */
    public float width;
    public float height;

    /*
     * TouchEnabled (set true if touch listener may be added to this entity)
     */
    public boolean touchEnabled;

    /*
     * When scene is rendered this is populated with the order of drawing (lowers are the ones that was drawn first)
     */
    public int lastDrawOrder;

    /*
     * Children list
     */
    protected ArrayList<GLEntity> childs;

    /*
     * Parent of this GLEntity
     */
    protected GLEntity parent;

    /*
     * Transformation Matrix for this GLEntity.
     * This matrix is updated by "updateMatrix(Matrix)" and used to retrieve position in world coordinate 
     * It is also used to transform vertices when drawing on screen.
     */
    protected Matrix matrix;
    protected Matrix matrixInvert;


    

    public static int getNewUid()
    {
        return GLEntity.idGenerator++;
    }

    public static int resetUid()
    {
        return GLEntity.idGenerator=0;
    }

    public static float propertyToFloat(Object obj)
    {

        if(obj == null)
        {
            return 0;
        }

        
        if(obj instanceof Float)
        {
            return (float)obj;
        }

        if(obj instanceof Double)
        {
            return ((Double)obj).floatValue();
        }


        if(obj instanceof Integer)
        {
            return ((Integer)obj).floatValue();
        }

        if(obj instanceof String)
        {
            return Float.parseFloat((String)obj);
        }

        return 0;

    }

    
    public static int propertyToInt(Object obj)
    {

        if(obj == null)
        {
            return 0;
        }

        
        if(obj instanceof Integer)
        {
            return (int)obj;
        }

        if(obj instanceof Double)
        {
            return ((Double)obj).intValue();
        }


        if(obj instanceof Float)
        {
            return ((Float)obj).intValue();
        }

        if(obj instanceof String)
        {
            return Integer.parseInt((String)obj);
        }

        return 0;

    }

    
    public static boolean propertyToBoolean(Object obj)
    {

        if(obj == null)
        {
            return false;
        }

        
        if(obj instanceof Boolean)
        {
            return (boolean)obj;
        }

        if(obj instanceof Double)
        {
            return ((Double)obj) != 0;
        }

        if(obj instanceof Float)
        {
            return ((Float)obj) != 0;
        }

        if(obj instanceof Integer)
        {
            return ((Integer)obj) != 0;
        }

        if(obj instanceof String)
        {
            return Boolean.parseBoolean((String)obj);
        }

        return false;

    }

    /*
     * Create a ne GLEntity
     *
     * GLEntity represent an object in the hierarchy of the GLScene.
     */
    public GLEntity(HashMap<String,Object> options)
    {
        this.id = GLEntity.getNewUid();
        this.type = GL_ENTITY;
        this.x = options.get("x") != null ? GLEntity.propertyToFloat(options.get("x")) : 0;
        this.y = options.get("y") != null ? GLEntity.propertyToFloat(options.get("y")) : 0;
        this.r = options.get("r") != null ? GLEntity.propertyToFloat(options.get("r")) : 0;
        this.px = options.get("py") != null ? GLEntity.propertyToFloat(options.get("px")) : 0;
        this.py = options.get("px") != null ? GLEntity.propertyToFloat(options.get("py")) : 0;
        this.sx = options.get("sx") != null ? GLEntity.propertyToFloat(options.get("sx")) : 1;
        this.sy = options.get("sy") != null ? GLEntity.propertyToFloat(options.get("sy")) : 1;
        this.width = options.get("width") != null ? GLEntity.propertyToFloat(options.get("width")) : -1;
        this.height = options.get("height") != null ? GLEntity.propertyToFloat(options.get("height")) : -1;
        this.touchEnabled = options.get("touchEnabled") != null ? GLEntity.propertyToBoolean(options.get("touchEnabled")) : false;
        this.parent= null;
        this.childs = new ArrayList<GLEntity>();
        this.matrix =  new Matrix();
        this.matrixInvert =  new Matrix();
/*
        Log.i("TIGL", "GLEntity: x :" + this.x);
        Log.i("TIGL", "GLEntity: y :" + this.y);
        Log.i("TIGL", "GLEntity: r :" + this.r);
        Log.i("TIGL", "GLEntity: px :" + this.px);
        Log.i("TIGL", "GLEntity: py :" + this.py);
        Log.i("TIGL", "GLEntity: sx :" + this.sx);
        Log.i("TIGL", "GLEntity: sy :" + this.sy);
        */
    }

    
    /*
     * Return an unique identifier for this sprite material
     *  could be a combinaison of texture, opacity, effects, etc...
     * This identifier is used to know wich entities/sprite can be draw together
     */
    public int getMaterialUid()
    {
        return Integer.valueOf(-1); //No material
    }

    /*
     * Return this GLEntity's parent
     */
    public GLEntity getParent()
    {
    	return this.parent;
    }

    
    /*
     * Remove a child
     *  this should also remove it from the scene entities (as well as its childrens)
     * @todo : process child entities !!
     */
    public synchronized void remove(GLEntity glEntity)
    {
        this.childs.remove(glEntity);
        glEntity.parent = null;
        HashMap<Integer,GLEntity> entities = this.getScene().getEntities();
        synchronized(entities)
        {
            entities.remove(glEntity.id);
        }
    }

    
    /*
     * Adds a new child
     *  This also add it to the scene entities (as well as its childrens)
     * @todo : process child entities !!
     */
    public synchronized void add(GLEntity glEntity)
    {
        glEntity.parent = this;
        this.childs.add(glEntity);
        HashMap<Integer,GLEntity> entities = this.getScene().getEntities();
        synchronized(entities)
        {
            entities.put(glEntity.id, glEntity);
        }
    }

       
    /*
     * Gets childrens 
     */
    public synchronized ArrayList<GLEntity> getChildrens()
    {
        return this.childs;
    }

    /*
     * Gets children at Nth pos
     * */
    public synchronized GLEntity getChildAt(int n)
    {
        if(n < 0 || n >= this.childs.size())
        {
            return null;
        }
        return this.childs.get(n);
    }
	
	/*
	* Sets sprite position
	*/
	public void setPos(float x, float y)
	{
		this.x = x;
		this.y = y;
    }
    
	


    
	public void updateBulkModeXY(int[] datas, boolean fullUpdate)
	{
        if(fullUpdate)
        {   //Full update mode
            for(int n = 0; n < datas.length ; n++)
            {
                GLEntity glEntity = this.getChildAt(n);
                glEntity.x=(((datas[n] >> 16) & 0xFFFF) - 32768 ) / 2f;
                glEntity.y=((datas[n] & 0xFFFF) - 32768) / 2f;
            }
        }
        else 
        {   //Indexed update mode
            for(int n = 0; n < datas.length/2 ; n++)
            {
                int index = datas[n*2];
                GLEntity glEntity = this.getChildAt(index);
                glEntity.x=(((datas[n*2+1] >> 16) & 0xFFFF) - 32768 ) / 2f;
                glEntity.y=((datas[n*2+1] & 0xFFFF) - 32768) / 2f;
            }
        }
    }

    
    
	public void updateBulkModeXYString(String datas)
	{
    }


    

    
    /*
     * Gets childrens 
     */
    public GLEntity[] getChildrensAsArray()
    {
        
        /*
         * Put all childrens in an array
         */
        Object[] objArray = this.childs.toArray(); 
        GLEntity[] glEntities = Arrays.copyOf(objArray, 
                                       objArray.length, 
                                       GLEntity[].class); 

        return glEntities;
    }

    
    /* 
     * Update the current matrix using GLEntity properties and its parent's matrix
     *  Matrix computation mey be a bit slow, so we only perform necessary transformation
     */
    public synchronized void updateMatrix(Matrix matrix)
    {
        /*
         * Compute matrix and combine it with its parent's matrix
         */
        this.matrix.reset();
        if(this.sx != 1 || this.sy != 1) this.matrix.postScale(this.sx, this.sy, this.px, this.py);
        if(this.r !=0) this.matrix.postRotate(this.r, this.px, this.py);
        this.matrix.postTranslate(this.x - this.px, this.y - this.py);
        this.matrix.postConcat(matrix);


        /*
        * (Try to) invert matrix to compute world to object coodinate (for example for touch event)
        */
        if(this.touchEnabled)
        {
            this.matrix.invert(this.matrixInvert);
        }

        /*
         * Compute all childrens matrix
         */
        Iterator<GLEntity> childIterator = this.childs.iterator();
        while (childIterator.hasNext()) 
        {
            (childIterator.next()).updateMatrix(this.matrix);
        }

    }

    public void getProperties(Vector<KrollDict> v)
    {
        KrollDict props = new KrollDict();
        props.put("x",x);
        props.put("y",y);
        props.put("r",r);
        props.put("x",x);
        v.add(props);

        //for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
        
        Iterator<GLEntity> childIterator = this.childs.iterator();
        while (childIterator.hasNext()) 
        {
            (childIterator.next()).getProperties(v);
        }
    }

    public synchronized int getChildrenCount()
    {
        int count = this.childs.size();
        //for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
       
        Iterator<GLEntity> childIterator = this.childs.iterator();
        while (childIterator.hasNext()) 
        {
            count+=(childIterator.next()).getChildrenCount();
        }
        return count;
    }

    public GLScene getScene()
    {
        if(this instanceof GLScene)
        {
            return (GLScene)this;
        }
        return this.parent.getScene();
    }




    /*
     * Draw this GLEntity on the GLView.
     *
     *  This method is called within the GLThread with an active context.
     *  GLES20 functions are available for drawing and can be called within this method. 
     *  Drawing must be done in object space as the transformation Matrix is already applied.
     */
    public void drawSingle()
    {

    }
    

    /*
     * Draw a list of GLEntity on the GLView.
     *
     *  If this class is not overrided it will call drawSingle for each entry of the given list
     *  For performance reason it is good to implement it in all subclass
     * 
     *  To enable batch drawing, all entitiy in this list should have the same material UID
     */
    public synchronized void drawBatch(ArrayList<GLEntity> entities)
    {
        //for (Enumeration<GLEntity> e = entities.elements(); e.hasMoreElements();)
        Iterator<GLEntity> childIterator = this.childs.iterator();
        while (childIterator.hasNext()) 
        {
            childIterator.next().drawSingle();
        }

    }


    public void prepareDrawing()
    {
    }


    


}