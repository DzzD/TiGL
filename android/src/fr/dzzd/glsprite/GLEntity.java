package fr.dzzd.glsprite;


import android.graphics.Matrix;
import java.util.*;

public class GLEntity
{
    
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


    protected boolean drawFlattenedEnabled = true;

    
    public int type;

    public final static int GL_ENTITY = 0;
    public final static int GL_SCENE = 1;
    public final static int GL_SPRITE = 2;


    /*
     * Create a ne GLEntity
     *
     * GLEntity represent an object in the hierarchy of the GLScene.
     */
    public GLEntity()
    {
        this.type = GL_ENTITY;
        this.x = 0;
        this.y = 0;
        this.px = 0;
        this.py = 0;
        this.sx = 1;
        this.sy = 1;
        this.r = 0;
        this.parent= null;
        this.childs = new Vector<GLEntity>();
        this.matrix =  new Matrix();
    }

    
    /*
     * Return an unique identifier for this sprite material
     *  could be a combinaison of texture, opacity, effects, etc...
     * */
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
     */
    public void updateMatrix(Matrix matrix)
    {
        /*
         * Compute matrix and combine it with its parent's matrix
         */
        this.matrix.reset();
        this.matrix.postTranslate(-this.px, -this.py);
        this.matrix.postScale(this.sx, this.sy);
        this.matrix.postRotate(this.r);
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

    public void getFlattenedEntities(Vector<GLEntity> flattenedEntities)
    {
        flattenedEntities.add(this);
        for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
        {
            (child.nextElement()).getFlattenedEntities(flattenedEntities);
        }
    }

    public int getChildrenCount()
    {
        int count = this.childs.size();
        for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
        {
            count+=(child.nextElement()).getChildrenCount();
        }
        return count;
    }




    /*
     * Draw this GLEntity on the GLView.
     *
     *  This method is called within the GLThread with an active context.
     *  GLES20 functions are available for drawing and can be called within this method. 
     *  Drawing must be done in object space as the transformation Matrix is already applied.
     */
    /*
    public void drawSingle()
    {
        if(!this.drawFlattenedEnabled)
        {
            for (Enumeration<GLEntity> child = this.childs.elements(); child.hasMoreElements();)
            {
                (child.nextElement()).draw();
            }
        }
        else
        {*/
            /*
             * Flatten this entity and all its children in a single Vector
             */
            // Vector<GLEntity> flattenedEntities = new Vector<GLEntity>();
            // this.getFlattenedEntities(flattenedEntities);

            /*
             * Arrange entities in different layers depending on their materials
             */
           /* HashMap<Integer><Vector<GLEntity>> materialLayers=new HashMap<Integer><Vector<GLEntity>>();
            for (Enumeration<GLEntity> entities = this.flattenedEntities.elements(); entities.hasMoreElements();)
            {
                GLEntity entity = entities.nextElement();
                Vector<GLEntity> layer = materialLayers.get(entity.getMaterialUid());
                if(!layer)
                {
                    layer = new Vector<GLEntity>();
                    materialLayers.put(entity.getMaterialUid(),layer);
                }
                layer.put(entity);
            }
        }

    }
*/


    public void prepareDrawing()
    {
    }

    public void drawSingle()
    {

    }

    public void drawBatch(Vector<GLEntity> entities)
    {
        for (Enumeration<GLEntity> e = entities.elements(); e.hasMoreElements();)
        {
            e.nextElement().drawSingle();
        }

    }

    


}