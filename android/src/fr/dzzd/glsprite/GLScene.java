package fr.dzzd.glsprite;


import org.appcelerator.kroll.common.Log;
import java.util.*;
import java.util.Map.Entry;
import java.nio.*;

public class GLScene extends GLEntity
{
    private boolean batchRenderingMode;
    private Vector<GLEntity> flattenedEntities;

    public GLScene()
    {
        super(new HashMap<String,Object>());
        this.type = GL_SCENE;
        this.batchRenderingMode = true;
        this.flattenedEntities = new Vector<GLEntity>();
    }

    public void setBatchRenderingMode(boolean flag)
    {
        this.batchRenderingMode = flag;
    }

    public void draw()
    {        
        int entityCount = 1 + this.getChildrenCount();
        
        // Log.i("GLSprite", "Entities count A = " + entityCount);
        this.flattenedEntities.clear();
        this.flattenedEntities.ensureCapacity(entityCount);
        this.getFlattenedEntities(this.flattenedEntities);


        /*
         * Prepare each entity for drawing
         */
        for (Enumeration<GLEntity> entities = this.flattenedEntities.elements(); entities.hasMoreElements();)
        {
            (entities.nextElement()).prepareDrawing();
        }


        if(!this.batchRenderingMode)
        {
            /*
            * Draw each entity alone independently of others
            * @todo : implements ordering based on entity drawing/z priority
            */
            for (Enumeration<GLEntity> entities = this.flattenedEntities.elements(); entities.hasMoreElements();)
            {
                GLEntity entity = entities.nextElement();
                entity.drawSingle();
            }
            return;
        }
        
        /*
         * Draw all entities with the same material (based uppon texture, opacity, effects, etc...) together in a single pass
         * @todo : implements ordering based on entity drawing/z priority
         */
        HashMap<Integer,Vector<GLEntity>> materialLayers = new HashMap<Integer,Vector<GLEntity>>();
        

        /*
         * Arrange entities in different layers depending on their materials
         * @todo : add drawing/z priority for arranging
         */
        for (Enumeration<GLEntity> entities = this.flattenedEntities.elements(); entities.hasMoreElements();)
        {
            //Log.i("GLSprite", "GLScene.draw() entities.nextElement()");
                GLEntity entity = entities.nextElement();
                Vector<GLEntity> layer = materialLayers.get(entity.getMaterialUid());
                if(layer == null)
                {
                   
                    layer = new Vector<GLEntity>();
                    materialLayers.put(entity.getMaterialUid(),layer);
                }
                layer.add(entity);
        }
       

        /* 
         * Set all layers within a Vector
         * @todo : implement ordering of layers
         */
        Vector<Vector<GLEntity>> layers = new Vector<Vector<GLEntity>>(materialLayers.size());
        for (Map.Entry<Integer, Vector<GLEntity>> entry : materialLayers.entrySet()) 
        {
            
            layers.add(entry.getValue());
        }

        /*
         * Draw all layers
         */
        for (Enumeration<Vector<GLEntity>> layer = layers.elements(); layer.hasMoreElements();)
        {
            Vector<GLEntity> entities = layer.nextElement();
            if(entities.get(0).type == GL_SPRITE)
            {
                GLSprite entity = (GLSprite)entities.elementAt(0);
                entity.drawBatch(entities);
                
            }
            

        }
        
    }

    
}
