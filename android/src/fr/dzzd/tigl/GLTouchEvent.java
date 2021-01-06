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

import android.view.MotionEvent;

public class GLTouchEvent 
{
    public static final int ACTION_DOWN  = 0;
    public static final int ACTION_MOVE  = 1;
    public static final int ACTION_UP  = 2;
    public static final int ACTION_CANCEL  = 3;
    public int action;
    public int pointer;
    public float x;
    public float y;
    public float sceneX;
    public float sceneY;
    public int entityId;

    public GLTouchEvent()
    {
    }
    
}
