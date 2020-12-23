package fr.dzzd.glsprite;

import org.appcelerator.kroll.KrollDict;

import android.graphics.Matrix;
import java.util.*;

public class GLEntity
{
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
    public float scaleX;
    public float scaleY;

    /*
     * Rotation (centered on the pivot)
     */
    public float rotate;

    /*
     * Children list
     */
    protected Vector<GLEntity> childs;

    /*
     * Parent of this GLEntity
     */
    protected GLEntity parent;

    /*
     * Transformation Matrix for this GLEntity.
     * This matrix is updated by "updateMatrix(Matrix)" and used to retrieve position in world coordinate aswell as for drawing.
     */
    protected Matrix matrix;

    /*
     * Helper matrices to convert to OpenGL parameter
     *
     * USED INTERNALLY, MUST NOT BE MODIFIED DIRECTLY
     *
    */
    private float[] matrix3x3;
    protected float[] matrix4x4;


    /*
     * Create a ne GLEntity
     *
     * GLEntity represent an object in the hierarchy of the GLScene.
     */
    public GLEntity()
    {
        this.x = 0;
        this.y = 0;
        this.px = 0;
        this.py = 0;
        this.scaleX = 1;
        this.scaleY = 1;
        this.rotate = 0;
        this.parent= null;
        this.childs = new Vector<GLEntity>();
        this.matrix =  new Matrix();
        this.matrix3x3 = new float[9];
        this.matrix4x4 = new float[16];
    }


    /*
     * Return this GLEntity's parent
     */
    public GLEntity getParent()
    {
    	return this.parent;
    }

    
    /*
     * Adds a new child
     */
    public int add(GLEntity glEntity)
    {
        this.childs.add(glEntity);
        return this.childs.size() - 1;
    }

       
    /*
     * Gets childrens 
     */
    public Vector<GLEntity> getChildrens()
    {
        return this.childs;
    }

    /*
     * Gets children at Nth pos
     * */
    public GLEntity getChildAt(int n)
    {
        return this.childs.elementAt(n);
    }
	
	/*
	* Sets sprite position
	*/
	public void setPos(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	

    public void setChildProperties(int index, HashMap<String,String> properties)
    {
        this.getChildAt(index).setProperties(properties);
    }
     
    
    public void setChildsProperties(HashMap<String,HashMap<String,String>> childsProperties)
    {
        for(Map.Entry<String, HashMap<String,String>> entry : childsProperties.entrySet())
        {
            int index = Integer.parseInt(entry.getKey());
            HashMap<String,String> properties = entry.getValue();
            this.setChildProperties(index, properties);
        }
    }

    public HashMap<String,String> getProperties()
    {
        HashMap<String,String> properties =new HashMap<String,String>();
        properties.put("x",Float.toString(this.x));
        properties.put("y",Float.toString(this.y));
        properties.put("px",Float.toString(this.px));
        properties.put("py",Float.toString(this.py));
        properties.put("scaleX",Float.toString(this.scaleX));
        properties.put("scaleY",Float.toString(this.scaleY));
        properties.put("rotate",Float.toString(this.rotate));
        return properties;

    }
	 
    public HashMap<String,HashMap<String,String>> getChildsProperties()
    {
        HashMap<String,HashMap<String,String>> properties = new HashMap<String,HashMap<String,String>>();
        GLEntity[] glEntities = this.getChildrensAsArray();
        
        for(int i = 0; i < glEntities.length; i++)
        {
            properties.put(Integer.toString(i), glEntities[i].getProperties());
            //properties[i] = glEntities[i].getProperties();
        }
        return properties;
    }


    public void setProperties(HashMap<String,String> properties)
    {
        for(Map.Entry<String, String> entry : properties.entrySet()) 
        {
            String key = entry.getKey();
            switch(key)
            {
                case "x" :
                    this.x = Float.parseFloat(entry.getValue());
                break;

                case "y" :
                    this.y = Float.parseFloat(entry.getValue());
                break;

                case "px" :
                    this.px = Float.parseFloat(entry.getValue());
                break;

                case "py" :
                    this.py = Float.parseFloat(entry.getValue());
                break;

                case "scaleX" :
                    this.scaleX = Float.parseFloat(entry.getValue());
                break;

                case "scaleY" :
                    this.scaleY = Float.parseFloat(entry.getValue());
                break;

                case "rotation" :
                    this.rotate = Float.parseFloat(entry.getValue());
                break;

            }
        }
    }
    

    /*
    public GLEntityProps getProperties()
    {
        GLEntityProps props = new GLEntityProps();
        props.x = this.x;
        props.y = this.y;
        props.rotate = this.rotate;
        return props;
    }

    public GLEntityProps[] getChildsProperties()
    {
        GLEntity[] glEntities= this.getChildrensAsArray();
        GLEntityProps[] props = new GLEntityProps[glEntities.length];
        for(int n = 0; n < glEntities.length; n++)
        {
            props[n] = glEntities[n].getProperties();
        }
        return props;
    }

    
    public void setProperties(GLEntityProps props)
    {
        this.x = props.x;
        this.y = props.y;
        this.rotate = props.rotate;
    }

    
    public void setChildProperties(GLEntityProps props[])
    {
        GLEntity[] glEntities= this.getChildrensAsArray();
        for(int index = 0; index < glEntities.length; index++)
        {
            glEntities[index].setProperties(props[index]);
        }
    }
    */
	
	/*
	* Sets sprite position
	*/
	public void setChildPos(int index, float x, float y)
	{
		GLEntity glEntity = this.getChildAt(index);
		glEntity.x = x;
		glEntity.y = y;
    }
    
    
	/*
	* Sets sprite position
    */
    
	public void setChildsPos(float[] datas)
	{
        for(int n = 0; n < datas.length / 3; n++)
        {
            //int index = indexes[n];
            GLEntity glEntity = this.getChildAt(n);
            glEntity.x=datas[n*3];
            glEntity.y=datas[n*3 + 1];
            glEntity.rotate=datas[n*3 + 2];
        }
    }

    
	public void setChildsPosInt(int[] datas)
	{
        for(int n = 0; n < datas.length; n++)
        {
            //int index = indexes[n];
            GLEntity glEntity = this.getChildAt(n);
            glEntity.x=(((datas[n] >> 16) & 0xFFFF) - 32768 ) / 4f;
            glEntity.y=((datas[n] & 0xFFFF) - 32768) / 4f;
            //glEntity.rotate=datas[n*2 + 1];
        }
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

    
    
	public void updateBulkModeXYRString(String datas)
	{
        /*
        for(int n = 0; n < datas.length/2 ; n++)
        {
            int index = datas[n*2];
            GLEntity glEntity = this.getChildAt(index);
            glEntity.x=(((datas[n*2+1] >> 16) & 0xFFFF) - 32768 ) / 2f;
            glEntity.y=((datas[n*2+1] & 0xFFFF) - 32768) / 2f;
            //glEntity.rotate=datas[n*2 + 1];
        }
        */
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
     */
    public void updateMatrix(Matrix matrix)
    {
        /*
         * Compute matrix and combine it with its parent's matrix
         */
        this.matrix.reset();
        this.matrix.postTranslate(-this.px, -this.py);
        this.matrix.postScale(this.scaleX, this.scaleY);
        this.matrix.postRotate(this.rotate);
        this.matrix.postTranslate(this.px + this.x, this.py + this.y);
        this.matrix.postConcat(matrix);

        /*
         * Compute all childrens matrix
         */
        for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
        {
            (child.nextElement()).updateMatrix(this.matrix);
        }

    }

    /*
     * Draw this GLEntity on the GLView.
     *
     *  This method is called within the GLThread with an active context.
     *  GLES20 functions are available for drawing and can be called within this method. 
     *  Drawing must be done in object space as the transformation Matrix is already applied.
     */
    public void draw()
    {
        for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
        {
            (child.nextElement()).draw();
        }

    }


}