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

public class TiglManagerPacket 
{
    public static final int POSITIONS_PACKED = 1;
    public static final int ROTATIONS_PACKED = 2;
    public static final int SCALES_PACKED = 3;
    public static final int PIVOTS_PACKED = 4;

    private int type;
    private Object datas;

    public TiglManagerPacket(int type, Object datas)
    {
        this.type = type;
        this.datas = datas;
    }

    public int getType()
    {
        return this.type;
    }
    
    public Object getDatas()
    {
        return this.datas;
    }
}
