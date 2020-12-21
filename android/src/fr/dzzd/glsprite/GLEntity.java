package fr.dzzd.glsprite;


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
    public GLEntity add(GLEntity glEntity)
    {
        this.childs.add(glEntity);
        return this;
    }

       
    /*
     * Gets childrens 
     */
    public Vector<GLEntity> getChildrens()
    {
        return this.childs;
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
        this.matrix.postRotate(this.rotate * ((float)Math.PI)/180f);
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