/* DO NOT EDIT THIS FILE. THIS FILE WAS GENERATED FROM org/nongnu/libvob/lob/Lists.rj,
 * EDIT THAT FILE INSTEAD!
 * All changes to this file will be lost.
 *//* -*-java-*-
Lists.rj
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
import javolution.util.*;
import java.awt.Color;
import java.util.*;

public class Lists {

    public static List list() {
	List l = FastList.newInstance();
	return l;
    }

    public static List list(Object o1) {
	List l = FastList.newInstance();
	l.add(o1);
	return l;
    }

    public static List list(Object o1, Object o2) {
	List l = FastList.newInstance();
	l.add(o1); l.add(o2);
	return l;
    }

    public static List list(Object o1, Object o2, Object o3) {
	List l = FastList.newInstance();
	l.add(o1); l.add(o2); l.add(o3);
	return l;
    }

    public static List list(Object o1, Object o2, Object o3, Object o4) {
	List l = FastList.newInstance();
	l.add(o1); l.add(o2); l.add(o3); l.add(o4);
	return l;
    }



    public static List concatElements(List lists) {
	// stupid linear implementation
	return new_RealtimeList_1(lists);
    }



    public static List concat(List l1, List l2) {
	return concatElements(list(l1, l2));
    }

    public static List concat(List l1, List l2, List l3) {
	return concatElements(list(l1, l2, l3));
    }

    public static List concat(List l1, List l2, List l3, List l4) {
	return concatElements(list(l1, l2, l3, l4));
    }




    public static List transform(List list, Transform transform) {
	// stupid linear implementation
	return new_RealtimeList_2(list,transform);
    }

        private static class _RealtimeList_1 extends RealtimeList {

            private _RealtimeList_1() {}

            List lists;

            
	    public int size() {
		int result = 0;
		for(int i=0; i<lists.size(); i++)
		    result += ((List)lists.get(i)).size();
		return result;
	    }
	    public Object get(int idx0) {
		int idx = idx0;
		for(int i=0; i<lists.size(); i++) {
		    List l = (List)lists.get(i);
		    if(idx < l.size()) return l.get(idx);
		    idx -= l.size();
		}
		throw new IndexOutOfBoundsException(""+idx0);
	    }
	

            public boolean move(ObjectSpace os) {
                if(super.move(os)) {
                    if(lists instanceof Realtime) ((Realtime)lists).move(os); 
                    return true;
                }
                return false;
            }
        }

        private static final RealtimeObject.Factory _RealtimeList_1_FACTORY =
            new RealtimeObject.Factory() {
                protected Object create() { return new _RealtimeList_1(); }
            };

        private static _RealtimeList_1 new_RealtimeList_1(List lists) {
            _RealtimeList_1 the_new_RealtimeList_1 = (_RealtimeList_1)_RealtimeList_1_FACTORY.object();
            the_new_RealtimeList_1.lists = lists;
            return the_new_RealtimeList_1;
        }
    
        private static class _RealtimeList_2 extends RealtimeList {

            private _RealtimeList_2() {}

            List list; Transform transform;

            
	    public int size() { return list.size(); }
	    public Object get(int i) { 
		return transform.transform(list.get(i)); 
	    }
	

            public boolean move(ObjectSpace os) {
                if(super.move(os)) {
                    if(list instanceof Realtime) ((Realtime)list).move(os); if(transform instanceof Realtime) ((Realtime)transform).move(os); 
                    return true;
                }
                return false;
            }
        }

        private static final RealtimeObject.Factory _RealtimeList_2_FACTORY =
            new RealtimeObject.Factory() {
                protected Object create() { return new _RealtimeList_2(); }
            };

        private static _RealtimeList_2 new_RealtimeList_2(List list, Transform transform) {
            _RealtimeList_2 the_new_RealtimeList_2 = (_RealtimeList_2)_RealtimeList_2_FACTORY.object();
            the_new_RealtimeList_2.list = list;
the_new_RealtimeList_2.transform = transform;
            return the_new_RealtimeList_2;
        }
    }
