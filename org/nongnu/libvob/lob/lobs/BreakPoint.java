/*
BreakPoint.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;

public class BreakPoint extends AbstractDelegateLob implements Breakable {

    protected Axis axis;
    protected float quality;
    protected Lob pre, in, post;

    protected Breakable subBreakable;
    
    private BreakPoint() {}

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    if(pre != null) pre.move(os); 
	    if(in != null) in.move(os);
	    if(post != null) post.move(os);
	    return true;
	}
	return false;
    }

    public static BreakPoint newInstance(Axis axis, Lob content, float quality,
					 Lob pre, Lob in, Lob post) {
	BreakPoint b = (BreakPoint)FACTORY.object();
	b.delegate = content;
	b.axis = axis;
	b.quality = quality;
	b.pre = pre;
	b.in = in;
	b.post = post;
	b.subBreakable = (Breakable)content.getImplementation(Breakable.class);
	return b;
    }

    /*
    public Lob layout(float w, float h) {
	return newInstance(axis, delegate.layout(w, h), quality,
			   pre.layout(w, h), in.layout(w, h), 
			   post.layout(w, h));
    }
    */

    public float getBreakQuality(Axis axis) {
	if(axis==this.axis)
	    return quality;
	else if(subBreakable != null)
	    return subBreakable.getBreakQuality(axis);
	else
	    return -INF;
    }

    public Lob getPreBreakLob(Axis axis) {
	if(axis==this.axis)
	    return pre;
	else if(subBreakable != null)
	    return subBreakable.getPreBreakLob(axis);
	else
	    return null;
    }

    public Lob getInBreakLob(Axis axis) {
	if(axis==this.axis)
	    return in;
	else if(subBreakable != null)
	    return subBreakable.getInBreakLob(axis);
	else
	    return null;
    }

    public Lob getPostBreakLob(Axis axis) {
	if(axis==this.axis)
	    return post;
	else if(subBreakable != null)
	    return subBreakable.getPostBreakLob(axis);
	else
	    return null;
    }

    private static Factory FACTORY = new Factory() {
	    protected Object create() {
		return new BreakPoint();
	    }
	};
}
