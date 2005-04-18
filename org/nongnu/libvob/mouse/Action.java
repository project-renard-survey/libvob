/*
Action.java
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

package org.nongnu.libvob.mouse;
import org.nongnu.libvob.*;

/** Action that can be found from scene with mouse and performed.
 */
public class Action extends MouseMultiplexer {

    public static interface RequestHandler {
	Object handleRequest(Object request);
    }


    protected RequestHandler handl;
    public Action(RequestHandler handler) {
	handl = handler;
    }

    public Object request(Object request) {
	if (handl != null)
	    return handl.handleRequest(request);
 	return null;
    }

    public Action parent;
}
