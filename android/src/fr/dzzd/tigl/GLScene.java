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
import android.util.Log;
import java.util.*;
import java.util.Map.Entry;
import java.nio.*;

public class GLScene extends GLEntity
{
    private boolean batchRenderingMode;
    private HashMap<Integer,GLEntity> entities;
    public static final int MAX_LAYER = 256;
    
    private ArrayList<GLEntity> flattenedEntities;
    private ArrayList<ArrayList<GLEntity>> entitiesLayers;
    private ArrayList<ArrayList<ArrayList<GLEntity>>> entitiesBatchLayers;
    public int currentDrawCount;

    public GLScene()
    {
        super(new HashMap<String,Object>());
        this.touchEnabled = true;
        this.type = GL_SCENE;
        this.batchRenderingMode = true;
        this.flattenedEntities = new ArrayList<GLEntity>();
        this.entities = new HashMap<Integer,GLEntity>();
        this.entitiesLayers = new ArrayList<ArrayList<GLEntity>>(MAX_LAYER);
        this.entitiesBatchLayers = new ArrayList<ArrayList<ArrayList<GLEntity>>>(MAX_LAYER);
        for(int n=0; n<MAX_LAYER; n++)
        {
            this.entitiesLayers.add(new ArrayList<GLEntity>());
            this.entitiesBatchLayers.add(new ArrayList<ArrayList<GLEntity>>());
        }
        
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


    public synchronized void  draw()
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
            
            Iterator<ArrayList<GLEntity>> layersIterator = this.entitiesLayers.iterator();
            while (layersIterator.hasNext()) 
            {
                layersIterator.next().clear();
            }


            entitiesIterator = this.flattenedEntities.iterator();
            while (entitiesIterator.hasNext()) 
            {
                GLEntity entity = entitiesIterator.next();
                //entity.drawSingle();
                int layer = entity.layer >= 0 ? entity.layer : 0;
                layer = layer < MAX_LAYER ? layer : MAX_LAYER - 1;
                this.entitiesLayers.get(layer).add(entity);
            }

            layersIterator = this.entitiesLayers.iterator();
            while (layersIterator.hasNext()) 
            {
                ArrayList<GLEntity> entities = layersIterator.next();
                entitiesIterator = entities.iterator();
                while (entitiesIterator.hasNext()) 
                {
                    GLEntity entity = entitiesIterator.next();
                    entity.drawSingle();
                }
            }
            return;
        }
        
        /*
         * Draw all entities with the same material (based uppon texture, opacity, effects, etc...) together in a single pass
         * @todo : implements ordering based on entity drawing/z priority
         */
        HashMap<Integer,ArrayList<GLEntity>> materialLayers = new HashMap<Integer,ArrayList<GLEntity>>();
        
        Iterator<ArrayList<ArrayList<GLEntity>>> layersBatchIterator = this.entitiesBatchLayers.iterator();
        while (layersBatchIterator.hasNext()) 
        {
            layersBatchIterator.next().clear();
        }

        /*
         * Arrange entities in different groups depending on their materials
         */
        entitiesIterator = this.flattenedEntities.iterator();
        while (entitiesIterator.hasNext()) 
        {
                GLEntity entity = entitiesIterator.next();
                ArrayList<GLEntity> group = materialLayers.get(entity.getMaterialUid());
                if(group == null)
                {
                   
                    group = new ArrayList<GLEntity>();
                    materialLayers.put(entity.getMaterialUid(), group);
                }
                group.add(entity);
        }
       

        /* 
         * Set all layers within an ArrayList
         * @todo : implement ordering of layers
         */
        //Vector<ArrayList<GLEntity>> layers = new Vector<ArrayList<GLEntity>>(materialLayers.size());
        for (Map.Entry<Integer, ArrayList<GLEntity>> group : materialLayers.entrySet()) 
        {
            ArrayList<GLEntity> entities = group.getValue();
            GLEntity entity = entities.get(0);
            int layer = entity.layer >= 0 ? entity.layer : 0;
            layer = layer < MAX_LAYER ? layer : MAX_LAYER - 1;
            this.entitiesBatchLayers.get(layer).add(entities);
        }
        
        /*
         * 
         */
        layersBatchIterator = this.entitiesBatchLayers.iterator();
        while (layersBatchIterator.hasNext()) 
        {
            ArrayList<ArrayList<GLEntity>> entitiesArrayOfArray = layersBatchIterator.next();

            Iterator<ArrayList<GLEntity>> entitiesArrayIterator = entitiesArrayOfArray.iterator();
            while (entitiesArrayIterator.hasNext()) 
            {
                ArrayList<GLEntity> entities = entitiesArrayIterator.next();
                GLEntity entity = entities.get(0);
                entity.drawBatch(entities);
            }
        }

        
    }

    
}
