/*
BoundedFloatLinearAbsoluteAdapter.java
 *    
 *    Copyright (c) 2003, Tuomas J. Lukka
 *    This file is part of Libvob.
 *    
 *    Libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 */
/*
 * Written by Tuomas J. Lukka
 */

package org.nongnu.libvob.input;
import java.util.*;

/** A class which receives absolute events from an axis.
 */
public class BoundedFloatLinearAbsoluteAdapter 
	implements AbsoluteAxisListener {
    private BoundedFloatModel model;

    public BoundedFloatLinearAbsoluteAdapter(BoundedFloatModel model) {
	this.model = model;
    }

    public void changedAbsolute(float newValue) {
	double min = model.getMinimum();
	double max = model.getMaximum();
	model.setValue(min + (max-min) * newValue);
    }
}



