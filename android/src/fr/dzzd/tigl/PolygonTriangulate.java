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


import android.util.Log;
import java.util.*;

class PolygonTriangulate
{
    private ArrayList<Integer> polygon;
    private float[] vertices;
    private float[] uvs;
    private int[] triangles;
    private int triangleCount;
    public float[] verticesOut;
    public float[] uvsOut;
    
    

    public PolygonTriangulate(float vertices[], float uvs[])
    {
        this.vertices = vertices;
        this.uvs = uvs;
        this.triangulate();
    }

    private void addTriangle(int a, int b, int c)
    {
        this.triangles[this.triangleCount * 3] = a;
        this.triangles[this.triangleCount * 3 + 1] = b;
        this.triangles[this.triangleCount * 3 + 2] = c;
        this.triangleCount++;
    }

    private float pSide(int v0, int v1, int p)
    {
        float v0x = this.vertices[v0 * 2];
        float v0y = this.vertices[v0 * 2 + 1];
        float v1x = this.vertices[v1 * 2];
        float v1y = this.vertices[v1 * 2 + 1];
        float px = this.vertices[p * 2];
        float py = this.vertices[p * 2 + 1];

        return ((v1x - v0x) * (py - v0y) - (v1y - v0y) * (px - v0x));
    }

    private boolean pInside(int v0, int v1, int v2, int p)
    {
        if(this.pSide(v0, v1, p) <= 0.0 ) return false;
        if(this.pSide(v1, v2, p) <= 0.0 ) return false;
        if(this.pSide(v2, v0, p) <= 0.0 ) return false;

        return true;
    }


    private boolean intersect(int A, int B, int C, int D)
    {
        float Ax = this.vertices[A * 2];
        float Ay = this.vertices[A * 2 + 1];
        float Bx = this.vertices[B * 2];
        float By = this.vertices[B * 2 + 1];
        float Cx = this.vertices[C * 2];
        float Cy = this.vertices[C * 2 + 1];
        float Dx = this.vertices[D * 2];
        float Dy = this.vertices[D * 2 + 1];
        float ABx = Bx - Ax;
        float ABy = By - Ay;
        float CDx = Dx - Cx;
        float CDy = Dy - Cy;


        if((CDx*ABy - CDy*ABx) == 0)
        {
            return false;
        }

        float s = (ABx * (Cy - Ay) + ABy * (Ax - Cx)) / (CDx * ABy - CDy * ABx);
        float t = (CDx * (Ay - Cy) + CDy * (Cx - Ax)) / (CDy * ABx - CDx * ABy);

        return (s>=0 && s<=1 && t>=0 && t<=1);
    }

    private boolean isVerticesCw()
    {
        int pBr = 0;
        int verticesCount = this.vertices.length / 2;
        for(int i = 1; i < verticesCount; i++)
        {
            if(this.vertices[i * 2 + 1] < this.vertices[pBr * 2 + 1]|| 
            (this.vertices[i * 2 + 1] == this.vertices[pBr * 2 + 1] && this.vertices[i * 2] > this.vertices[pBr * 2]))
            {
                pBr = i;
            }
        }
        if(this.pSide((pBr - 1 + verticesCount) % verticesCount, pBr ,(pBr + 1) % verticesCount) < 0)
        {
            // Log.w("TIGL", "Polygon CCW points, need reverse");
            return false;
        }
        return true;
    }

    private boolean isSelfIntersect()
    {
        // Log.i("TIGL", "Polygon triangulate, search self intersection");
        int polygonCount = this.polygon.size();
        int A = this.polygon.get(0);
        int B = this.polygon.get(1);
        for(int n = 2; n <  polygonCount + 2; n++)
        {
            // Log.i("TIGL", "AB: " + A + " " + B);
            int C = this.polygon.get(n % polygonCount);
            int D = this.polygon.get((n + 1) % polygonCount);
            for(int c = n + 2; c < n + polygonCount - 1; c++)
            {
                // Log.i("TIGL", " CD: " + C + " " + D);
                if(this.intersect(A, B, C, D))
                {
                    Log.e("TIGL", "Polygon self intersection found !");
                    return true;
                }
                C = D;
                D = this.polygon.get(c % polygonCount);
                
            }
            A = B;
            B = this.polygon.get(n % polygonCount);
        }
        return false;
    }

    public boolean triangulate()
    {
        int pointCount = this.vertices.length / 2;
        this.polygon = new ArrayList<Integer>(pointCount);
        this.triangles = new int[(pointCount - 2) * 3];
        this.triangleCount = 0;

        if(this.isVerticesCw())
        {
            for(int n = 0; n < pointCount; n++)
            {
                this.polygon.add(n);
            }
        }
        else
        {
            for(int n = pointCount - 1; n >=0 ; n--)
            {
                this.polygon.add(n);
                // Log.i("TIGL", "N " + n);
            }
        }

        if(this.isSelfIntersect())
        {
            this.verticesOut = new float[0];
            this.uvsOut = new float[0];
            this.triangles = new int[0];
            return false;
        }

        int a = this.polygon.get(0);
        int b = this.polygon.get(1);
        int c = this.polygon.get(2);

        int poligonSize = this.polygon.size();
        int i = 3;

        int reflexVertexCount = 0;
        while(true)
        {
            if(poligonSize == 3)
            {
                this.addTriangle(a, b, c);
                break;
            }
            
            if(this.pSide(a, b, c) < 0)
            {
                a = b;
                b = c;
                c = this.polygon.get(i % poligonSize);
                i++;
                continue;
            }
            
            boolean pInsideTriangleFound = false;
            for(int j = i ; j <  (i + poligonSize - 3 ); j++)
            {
                int pIndex = this.polygon.get(j % poligonSize);
                if(this.pInside(a, b, c, pIndex))
                {
                    pInsideTriangleFound = true;
                    break;
                }
            }
            
            if(!pInsideTriangleFound)
            {
                this.addTriangle(a, b, c);
                this.polygon.remove(this.polygon.indexOf(b));
                poligonSize--;
                a = this.polygon.get(i++ % poligonSize);
                b = this.polygon.get(i++ % poligonSize);
                c = this.polygon.get(i++ % poligonSize);
                continue;
            }
            
            a = b;
            b = c;
            c = this.polygon.get( i % poligonSize);
            i++;
        }   

        this.verticesOut= new float[this.triangles.length * 2];
        this.uvsOut= new float[this.triangles.length * 2];
        for(int n = 0; n < this.triangles.length; n++)
        {
            this.verticesOut[n * 2 ] = this.vertices[this.triangles[n] * 2];
            this.verticesOut[n * 2 + 1] = this.vertices[this.triangles[n] * 2 +  1];
            this.uvsOut[n * 2 ] = this.uvs[this.triangles[n] * 2];
            this.uvsOut[n * 2 + 1] = this.uvs[this.triangles[n] * 2+ 1];
            // Log.i("TIGL", "Triangle vertices " + this.verticesOut[n * 2 ] + "," + this.verticesOut[n * 2 +1 ]);
        }    

        return true; 
    }


}