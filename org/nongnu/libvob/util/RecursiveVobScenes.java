/*
RecursiveVobScenes.java
 *    
 *    Copyright (c) 2004, Matti J. Katila
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
 * Written by Matti J. Katila
 */

package org.nongnu.libvob.util;
import org.nongnu.libvob.*;
import org.nongnu.libvob.mouse.*;

/** Help util for handling mouse events with 
 *  recursive vob scenes.
 */
public class RecursiveVobScenes {
    static private void p(String s) { System.out.println("RecVS:: "+s); }


    public static Action findAction(final VobScene root, 
				    int x, int y,
				    Object request) {
	return findActionRec(root, null, new int[0], 
			     root.getCSAt(0, x,y, null),
			     x,y,
			     request);
    }
    private static Action findActionRec(VobScene root, 
				 ChildVobScene cvs, 
				 int [] actCSs, 
				 int cs, int x, int y,
				 Object req) {
	VobScene scene = (cvs != null? cvs: root);
	if(scene.actions.get(cs) != null) {
	    //p("what is found("+cs+")? "+scene.actions.get(cs));
	    Action a = (Action) scene.actions.get(cs);
	    if (a.request(req) == null) {
		if (scene.coords.hasActiveChildVS(cs)) {
		    ChildVobScene child = scene.coords.getChildByCS(cs);
		    //p("child.." + child);
		    int [] NactCSs = addAct(actCSs, cs);
		    int parent = -1;
		    int cs_ = root.coords.getChildCSAt(NactCSs, parent, x,y, null);
		    //p("child cs: "+ cs_);
		    if (cs_ > 0)
			return findActionRec(root, child, NactCSs, cs_, x,y, req); 
		    // nothing found - try normal getCSAt
		    //p("try NORMLAL "+cs);
		}
		if (cvs == null)
		    return findActionRec(root, cvs, actCSs, root.getCSAt(cs, x,y, null), x,y,req); 
		else
		    return findActionRec(root, cvs, actCSs, root.coords.getChildCSAt(actCSs, cs, x,y, null), x,y,req); 
	    }
	    //p("return "+a);
	    return a;
	}
	return null;
    }


    static private int[] addAct(int[] i, int j) {
	int [] ret = new int[i.length+1];
	System.arraycopy(i, 0, ret, 0, i.length);
	ret[i.length] = j;
	return ret;
    }

}
