/*
DecoratorLob.java
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
import org.nongnu.libvob.layout.IndexedVobMatcher;
import java.awt.Color;
import java.util.*;

/** A lob that draws its child, then searches the scene for a cs with
 *  a given key, then draws another lob in that cs.
 */
public class DecoratorLob extends AbstractLob {

    protected Lob child, decoration;
    protected Object key;
    protected int intKey;

    private DecoratorLob() {}

    public static DecoratorLob newInstance(Lob child, Lob decoration,
					   Object key, int intKey) {
	DecoratorLob l = (DecoratorLob)LOB_FACTORY.object();
	l.child = child; l.decoration = decoration;
	l.key = key; l.intKey = intKey;
	return l;
    }

    public SizeRequest getSizeRequest() {
	return child.getSizeRequest();
    }

    public Lob getImplementation(Class clazz) {
	if(clazz.isInstance(this))
	    return this;
	else
	    return child.getImplementation(clazz);
    }

    public Lob layout(float w, float h) {
	return newInstance(child.layout(w, h), decoration, key, intKey);
    }

    public boolean move(ObjectSpace os) {
	if(super.move(os)) {
	    child.move(os);
	    decoration.move(os);
	    return true;
	}
	return false;
    }

    float[] wh = new float[2];
    public void render(VobScene scene, int into, int matchingParent,
		       float d, boolean visible) {

	child.render(scene, into, matchingParent, d, visible);

	int cs;

	if(scene.matcher instanceof IndexedVobMatcher) {
	    IndexedVobMatcher m = (IndexedVobMatcher)scene.matcher;
	    cs = m.getCS(matchingParent, key, intKey);
	} else {
	    cs = scene.matcher.getCS(matchingParent, key);
	}

	if(cs >= 0) {
	    scene.coords.getSqSize(cs, wh);

	    Lob layout = decoration.layout(wh[0], wh[1]);
	    layout.render(scene, cs, matchingParent, d, visible);
	}
    }

    private static final Factory LOB_FACTORY = new Factory() {
	    public Object create() {
		return new DecoratorLob();
	    }
	};
}
