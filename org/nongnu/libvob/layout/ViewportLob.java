/*   
ViewportLob.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein.
 *
 *    This file is part of Fenfire.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.layout;
import org.nongnu.libvob.*;
import org.nongnu.libvob.util.*;
import org.nongnu.navidoc.util.Obs;

public class ViewportLob extends AbstractMonoLob {

    protected Axis axis;
    protected Model positionModel;
    protected float width, height;

    public ViewportLob(Axis axis, Lob content, Model positionModel) {
	super(content);
	this.axis = axis;
	this.positionModel = positionModel;
    }

    protected Replaceable[] getParams() { 
	return new Replaceable[] { content, positionModel };
    }
    protected Object clone(Object[] params) {
	return new ViewportLob(axis, (Lob)params[0], (Model)params[1]);
    }

    public Model getPositionModel() { return positionModel; }

    public void setSize(float requestedWidth, float requestedHeight) {
	if(axis == X) {
	    content.setSize(Float.NaN, requestedHeight);
	    content.setSize(content.getNatSize(X), requestedHeight);
	} else {
	    content.setSize(requestedWidth, Float.NaN);
	    content.setSize(requestedWidth, content.getNatSize(Y));
	}

	width = requestedWidth;
	height = requestedHeight;
    }

    protected float getScroll() {
	float pos = positionModel.getFloat();
	float lobSize = content.getNatSize(axis);
	float viewportSize = (axis==X) ? width : height;
	
	float scroll;
	if(lobSize < viewportSize)
	    scroll = 0;
	else if(pos < viewportSize/2)
	    scroll = 0;
	else if(pos > lobSize-(viewportSize/2))
	    scroll = viewportSize - lobSize;
	else
	    scroll = viewportSize/2 - pos;

	return scroll;
    }

    public boolean mouse(VobMouseEvent e, float x, float y) {
	if(axis == X)
	    return content.mouse(e, x-getScroll(), y);
	else
	    return content.mouse(e, x, y-getScroll());
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float x, float y, float w, float h, float d,
		       boolean visible) {

	if(axis == X)
	    super.render(scene, into, matchingParent, x+getScroll(), y, 
			 content.getNatSize(X), h, d, visible);
	else
	    super.render(scene, into, matchingParent, x, y+getScroll(),
			 w, content.getNatSize(Y), d, visible);
    }
}
