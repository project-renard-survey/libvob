/*
AbstractMonoVob.java
 *    
 *    Copyright (c) 2003, Benja Fallenstein
 *
 *    This file is part of libvob.
 *    
 *    libvob is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    libvob is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with libvob; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *    
 *
 */
/*
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lava;

/** A vob that contains one other vob.
 *  For example, a transformation on the contained vob.
 */
public abstract class AbstractMonoVob implements Vob {

    protected final Vob content;

    protected AbstractMonoVob(Vob content) {
	this.content = content;
    }

    public int getCount() {
	return content.getCount() + 1;
    }

    public Vob getVob(int index) {
	if(index == 0)
	    return this;
	else
	    return content.getVob(index - 1);
    }

    public float getSize(Vob.Axis axis) {
	return content.getSize(axis);
    }

    public RenderTraversal transform(RenderTraversal t, int index) {
	if(index > 0)
	    content.transform(t, index - 1);
	return t;
    }
}
