/*
VobMatcher.java
 *    
 *    Copyright (c) 2002, Tuomas J. Lukka
 *    Copyright (c) 2005, Benja Fallenstein
 *
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
 *
 */
/*
 * Written by Tuomas J. Lukka
 */
package org.nongnu.libvob;
import javolution.realtime.*;
import javolution.lang.*;
import java.awt.*;

/** An interface for matching coordinate systems  between different vobscenes.
 */

public interface VobMatcher {
    int DONT_INTERP = -1;
    int SHOW_IN_INTERP = -1000;

    /** Add key to a cs, and return the cs.
     * Used in idiom like
     * <pre>cs = vs.matcher.add(vs.coords.affineCoordsys(...), "Foo")
     * </pre>
     * @return cs, for the above idiom.
     */
    int add(int cs, Object key); // called by VobScene.put &c.
    /** 
     * @return cs.
     */
    int add(int into, int cs, Object key);

    /** Return the index of the coordinate system that
     * was added with the given key.
     * @return The index of the coordinate system, or -1 if none.
     */
    int getCS(Object key);

    /** Return the index of the coordinate system that was
     * added into the given parent with the given key.
     * @return The index of the coordinate system, or -1 if none.
     */
    int getCS(int parent, Object key);

    /** Get the matcher parent of the given coordinate system.
     */
    int getParent(int cs);

    /** Whether calling getParent(cs) recursively
     * (at least one time) would eventually
     * return parent. 
     * The coordinate system
     * itself is not its own ancestor.
     */
    boolean isAncestor(int cs, int ancestor);


    Object getPath(int cs);
    int getPathCS(Object path);

    Object getKey(int cs);

    /** Return, for each coordinate system of this matcher, an integer
     * giving the coordinate system of the other matcher that system should move
     * to. DONT_INTERP meand means that the the coordsys should 
     * not be interpolated; SHOW_IN_INTERP means that during interpolation,
     * the coordsys should be rendered with the same coordinates as
     * outside interpolation.
     * @param other The other vobmatcher; the interp list is constructed
     * 		between this and the other matcher.
     * @param towardsOther Whether we are interpolating towards or away from the
     * 		`other` parameter. This has effect when key mappings are used,
     * 		but currently no official API is provided for setting them;
     * @see DefaultVobMap
     */
    int[] interpList(VobMatcher other, boolean towardsOther);

    /** Remove all mappings from this matcher.
     *  This method makes matchers re-usable; rather than creating
     *  a new matcher object, an old one can be re-used by clearing it.
     */
    void clear();


    class TreePath extends RealtimeObject {
	public Object key;
	public Object subpath;

	public Text toText() {
	    return Text.valueOf("["+key+"]->"+subpath);
	}

	public boolean move(ObjectSpace os) {
	    if(super.move(os)) {
		if(key instanceof Realtime)
		    ((Realtime)key).move(os);
		if(subpath instanceof Realtime)
		    ((Realtime)subpath).move(os);
		return true;
	    }
	    return false;
	}

	public boolean equals(Object o) {
	    TreePath tp = (TreePath)o;
	    if(subpath != null)
		return key.equals(tp.key) && subpath.equals(tp.subpath);
	    else
		return key.equals(tp.key) && tp.subpath == null;
	}

	public int hashCode() {
	    return 43289*key.hashCode() + 
		(subpath!=null ? 2388*subpath.hashCode() : 0);
	}
    }

    RealtimeObject.Factory TREE_PATH_FACTORY = new RealtimeObject.Factory() {
	    public Object create() {
		return new TreePath();
	    }
	};
}


