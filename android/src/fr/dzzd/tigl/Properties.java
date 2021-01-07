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


public class Properties
{
    
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
    
}
