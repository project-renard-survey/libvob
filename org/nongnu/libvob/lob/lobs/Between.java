/*
Between.java
 *    
 *    Copyright (c) 2004-2005, Benja Fallenstein
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
package org.nongnu.libvob.lob.lobs;
import org.nongnu.libvob.lob.*;
import org.nongnu.libvob.*;
import java.awt.Color;
import java.util.*;

/** A lob drawing a lobs in front of and behind another lob.
 */
public class Between extends AbstractDelegateLob {

    protected Lob back, front;

    private Between() {}

    public static Between newInstance(Lob back, Lob middle, Lob front) {
	Between b = (Between)LOB_FACTORY.object();
	
	b.back     = (back == null) ?   NullLob.instance : back;
	b.delegate = (middle == null) ? NullLob.instance : middle;
	b.front    = (front == null) ?  NullLob.instance : front;

	return b;
    }

    public Lob layout(float w, float h) {
	return newInstance(back.layout(w, h), delegate.layout(w, h),
			   front.layout(w, h));
    }

    public Lob layoutOneAxis(float size) {
	Lob m = delegate.layoutOneAxis(size);
	Lob b = back, f = front;

	if(back.getLayoutableAxis() == delegate.getLayoutableAxis())
	    b = back.layoutOneAxis(size);

	if(front.getLayoutableAxis() == delegate.getLayoutableAxis())
	    f = front.layoutOneAxis(size);

	return newInstance(b, m, f);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    back.move(os); front.move(os);
	    return true;
	}
	return false;
    }

    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {
	int _cs = into;
	int cs = scene.coords.translate(_cs, 0, 0, 2*d/4);
	back.render(scene, cs, matchingParent, d/4, visible);
	cs = scene.coords.translate(_cs, 0, 0, d/4);
	delegate.render(scene, cs, matchingParent, d/4, visible);
	cs = _cs;
	front.render(scene, cs, matchingParent, d/4, visible);
    }

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new Between();
	    }
	};
}
