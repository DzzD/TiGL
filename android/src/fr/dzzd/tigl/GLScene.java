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
import java.util.Map.Entry;
import java.nio.*;

public class GLScene extends GLEntity
{
    private boolean batchRenderingMode;
    private ArrayList<GLEntity> flattenedEntities;
    private HashMap<Integer,GLEntity> entities;
    public int currentDrawCount;

    public GLScene()
    {
        super(new HashMap<String,Object>());
        this.touchEnabled = true;
        this.type = GL_SCENE;
        this.batchRenderingMode = true;
        this.flattenedEntities = new ArrayList<GLEntity>();
        this.entities = new HashMap<Integer,GLEntity>();
    }

    public void setBatchRenderingMode(boolean flag)
    {
        this.batchRenderingMode = flag;
    }

    public HashMap<Integer,GLEntity> getEntities()
    {
        return this.entities;
    }

    public GLEntity getEntityById(int id)
    {
        return this.entities.get(id);
    }


    public void draw()
    {        
       
        this.currentDrawCount = 0;
        this.flattenedEntities.clear();
        this.flattenedEntities.ensureCapacity(this.entities.size());
        synchronized(this.entities)
        {
            for (Map.Entry<Integer, GLEntity> entity : this.entities.entrySet()) 
            {
                this.flattenedEntities.add(entity.getValue());
            }
        }
        

        /*
         * Prepare all entities for drawing
         */
        Iterator<GLEntity> entitiesIterator = this.flattenedEntities.iterator();
        while (entitiesIterator.hasNext()) 
        {
            (entitiesIterator.next()).prepareDrawing();
        } 


        if(!this.batchRenderingMode)
        {
            /*
            * Draw each entity alone independently of others
            * @todo : implements ordering based on entity drawing/z priority
            */
            entitiesIterator = this.flattenedEntities.iterator();
            while (entitiesIterator.hasNext()) 
            {
                (entitiesIterator.next()).drawSingle();
            }
            return;
        }
        
        /*
         * Draw all entities with the same material (based uppon texture, opacity, effects, etc...) together in a single pass
         * @todo : implements ordering based on entity drawing/z priority
         */
        HashMap<Integer,ArrayList<GLEntity>> materialLayers = new HashMap<Integer,ArrayList<GLEntity>>();
        

        /*
         * Arrange entities in different layers depending on their materials
         * @todo : add drawing/z priority for arranging
         */
        entitiesIterator = this.flattenedEntities.iterator();
        while (entitiesIterator.hasNext()) 
        {
                GLEntity entity = entitiesIterator.next();
                ArrayList<GLEntity> layer = materialLayers.get(entity.getMaterialUid());
                if(layer == null)
                {
                   
                    layer = new ArrayList<GLEntity>();
                    materialLayers.put(entity.getMaterialUid(),layer);
                }
                layer.add(entity);
        }
       

        /* 
         * Set all layers within an ArrayList
         * @todo : implement ordering of layers
         */
        Vector<ArrayList<GLEntity>> layers = new Vector<ArrayList<GLEntity>>(materialLayers.size());
        for (Map.Entry<Integer, ArrayList<GLEntity>> layer : materialLayers.entrySet()) 
        {
            
            layers.add(layer.getValue());
        }
        

        /*
         * Draw all layers
         */
        for (Enumeration<ArrayList<GLEntity>> layer = layers.elements(); layer.hasMoreElements();)
        {
            ArrayList<GLEntity> entities = layer.nextElement();
            if(entities.get(0).type == GL_SPRITE)
            {
                GLSprite entity = (GLSprite)entities.get(0);
                entity.drawBatch(entities);
            }
            

        }
        
    }

    
}
