/*
Components.java
 *    
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
 * Written by Benja Fallenstein
 */
package org.nongnu.libvob.lob;
import org.nongnu.libvob.fn.*;
import org.nongnu.libvob.lob.lobs.*;
import javolution.lang.*;
import javolution.realtime.*;
import java.awt.Color;
import java.util.*;

public class Components {

    public static Model getModel(Map params, String name, Object _default) {
	return (Model)getParam(params, name, 
			       StateModel.newInstance(name, _default));
    }

    public static Model getModel(Map params, String name, int _default) {
	return (Model)getParam(params, name, 
			       StateModel.newInstance(name, _default));
    }

    public static Object getParam(Map params, String name, Object _default) {
	if(params.get(name) != null)
	    return params.get(name);
	else
	    return _default;
    }



    public static Color 
	lightColor = new java.awt.Color(1, 1, .9f),
	darkColor = new java.awt.Color(.7f, .5f, .5f);




    public static Lob frame(Lob lob) {
	return Lobs.frame(lob, null, Color.black, 1, 5, false);

    }

    public static Lob listBox(List elements, Map params) {
	TransformLobList.Transform tr = 
	    (TransformLobList.Transform)getParam(params, "transform", 
						 toStringTransform);

	Object defaultSelection = elements.size() > 0 ? elements.get(0) : null;
	Model selected = getModel(params, "selected", defaultSelection);

	Axis axis = (Axis)getParam(params, "axis", Axis.Y);

	LobList lobs = TransformLobList.newInstance(elements, tr);
	lobs = ConcatLobList.newInstance(lobs, SingletonLobList.newInstance(Lobs.glue(axis, 0, 0, SizeRequest.INF)));

	Lob lob = Lobs.box(axis, lobs);
	
        Lob cursor_lob = Lobs.filledRect(darkColor);
        cursor_lob = Lobs.key(cursor_lob, "cursor");
        lob = Lobs.decorate(lob, cursor_lob, selected.get(), -1);

	lob = frame(lob);
	return lob;
    }

    public static Lob textBox(Model text, Map params) {
	params.put("multiline", Boolean.FALSE);
	return textComponent(text, params);
    }

    public static Lob textArea(Model text, Map params) {
	params.put("multiline", Boolean.TRUE);
	return textComponent(text, params);
    }

    public static Lob textComponent(Model text, Map params) {

	Model cursor = (Model)getModel(params, "cursor", -1);
	LobFont font = (LobFont)getParam(params, "font", Lobs.font());

	Boolean multilineB = (Boolean)getParam(params, "multiline", null);
	boolean multiline = multilineB.booleanValue();

	Text txt = Text.valueOf((String)text.get());
	LobList list = Lobs.text(font, txt);
	list = KeyLobList.newInstance(list, "text");
	Lob lob = multiline ? Lobs.linebreaker(list) : Lobs.hbox(list);

        Lob cursor_lob = Lobs.line(java.awt.Color.black, 0, 0, 0, 1);
        cursor_lob = Lobs.key(cursor_lob, "textcursor");
        lob = Lobs.decorate(lob, cursor_lob, "text", cursor.getInt());
	
        lob = TextKeyController.newInstance(lob, text, cursor);

	return frame(lob);
    }


    private static class ToStringTransform extends RealtimeObject
	implements TransformLobList.Transform {

	public Object transform(Object o) {
	    if(o instanceof Realtime)
		return Lobs.key(Lobs.hbox(Lobs.text(((Realtime)o).toText())),o);
	    else
		return Lobs.key(Lobs.hbox(Lobs.text(o.toString())), o);
	}
    }

    private static final ToStringTransform toStringTransform = new ToStringTransform();
}