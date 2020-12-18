package fr.dzzd.glsprite;


import android.graphics.Matrix;
import java.util.*;

public class GLSprite
{
    public float x;
    public float y;

    public float px;
    public float py;

    public float scaleX;
    public float scaleY;

    public float rotate;

    protected Vector<GLSprite> childs;
    protected GLSprite parent;

    
    protected Matrix matrix;
    protected float[] matrix3;
    protected float[] matrix4x4;

    public GLSprite()
    {
        this.x = 0;
        this.y = 0;
        this.px = 0;
        this.py = 0;
        this.scaleX = 1;
        this.scaleY = 1;
        this.rotate = 0;
        this.parent= null;
        this.childs = new Vector<GLSprite>();
        this.matrix =  new Matrix();
        this.matrix3 = new float[9];
        this.matrix4x4 = new float[16];
    }

    public GLSprite getParent()
    {
    	return this.parent;
    }

    
    public void updateMatrix(Matrix matrix)
    {
        this.matrix.reset();
        this.matrix.postTranslate(-this.px, -this.py);
        this.matrix.postScale(this.scaleX, this.scaleY);
        this.matrix.postRotate(this.rotate * ((float)Math.PI)/180f);
        this.matrix.postTranslate(this.px, this.py);
        
        this.matrix.postTranslate(this.x, this.y);
        //this.matrix.set(matrix);
        //this.matrix.preScale(this.scaleX, this.scaleY);
        //this.matrix.preRotate(this.rotate * ((float)Math.PI)/180f);
        //this.matrix.preTranslate(this.x, this.y);
        //this.matrix.postScale(this.scaleX, this.scaleY);
        //this.matrix.postTranslate(this.x, this.y);
        this.matrix.postConcat(matrix);

        this.matrix.getValues(this.matrix3);
            for(int y=0;y<4;y++)
                for(int x=0;x<4;x++)
                    this.matrix4x4[x+y*4]=(x==y)?1:0;
            
        this.matrix4x4[0]=this.matrix3[0];
        this.matrix4x4[1]=this.matrix3[1];
        this.matrix4x4[4]=this.matrix3[3];
        this.matrix4x4[5]=this.matrix3[4];
        this.matrix4x4[3]=this.matrix3[2];
        this.matrix4x4[7]=this.matrix3[5];

        for (Enumeration<GLSprite> child = this.childs.elements(); child.hasMoreElements();)
        {
            ((GLSprite)child).updateMatrix(this.matrix);
        }

    }

    public void draw()
    {
        for (Enumeration<GLSprite> child = this.childs.elements(); child.hasMoreElements();)
        {
            ((GLSprite)child).draw();
        }

    }


}