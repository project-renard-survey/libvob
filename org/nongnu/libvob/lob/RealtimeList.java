/* DO NOT EDIT THIS FILE. THIS FILE WAS GENERATED FROM org/nongnu/libvob/lob/RealtimeList.rj,
 * EDIT THAT FILE INSTEAD!
 * All changes to this file will be lost.
 *//* -*-java-*-
Components.rj
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
import javolution.lang.*;
import javolution.realtime.*;
import java.util.*;

/** An abstract implementation of List, like AbstractList, but
 *  inheriting from RealtimeObject.
 *  NOTE: Iterators will be allocated on the Javolution stack
 *  (i.e., using the current pool context). This is virtually always
 *  what you want. When it isn't, you have to move the iterator object
 *  to a different object space (it extends RealtimeObject).
 *  <p>
 *  Lists returned by subList() will be allocated on the stack, too.
 *  This may more often not be what you want, so don't forget to
 *  move the list to a different object space if necessary.
 *  <p>
 *  The methods in this class do not enter a PoolContext themselves,
 *  even those that create iterators and sublists internally.
 */
public abstract class RealtimeList extends RealtimeCollection implements List {

    protected transient int modCount;


    // methods subclasses must implement

    public abstract Object get(int index);
    public abstract int size();


    // methods subclasses can override when they want to be modifiable

    public void add(int index, Object element) {
	throw new UnsupportedOperationException();
    }

    public Object set(int index, Object element) {
	throw new UnsupportedOperationException();
    }

    public Object remove(int index) {
	throw new UnsupportedOperationException();
    }


    // method subclasses can override for efficiency

    protected void removeRange(int from, int to) {
	if(to > size())
	    throw new IndexOutOfBoundsException("toIndex="+to+",size="+size());

	ListIterator iter = listIterator(from);
	for(int i=from; i<to; i++) {
	    iter.next();
	    iter.remove();
	}
    }


    // implementation of ordinary List methods

    public boolean add(Object element) {
	add(size(), element);
	return true;
    }

    public int indexOf(Object element) {
	for(int i=0; i<size(); i++)
	    if(equals(get(i), element)) return i;

	return -1;
    }

    public int lastIndexOf(Object element) {
	for(int i=size()-1; i>=0; i--)
	    if(equals(get(i), element)) return i;

	return -1;
    }

    public void clear() {
	removeRange(0, size());
    }

    public boolean addAll(int index, Collection items) {
	boolean result = false;
	for(Iterator i=items.iterator(); i.hasNext();) {
	    add(index, i.next());
	    index++;
	    result = true;
	}
	return result;
    }


    public Iterator iterator() {
	return listIterator();
    }

    public ListIterator listIterator() {
	return listIterator(0);
    }

    public ListIterator listIterator(int index) {
	RealtimeList list = this;
	int lastIndex = -1;
	int lastModCount = modCount;

	return new_ListIterator_1(list,index,lastIndex,lastModCount);
    }


    public List subList(int from, int to) {
	if(to < from) throw new IllegalArgumentException(from+" "+to);
	if(to > size()) throw new IndexOutOfBoundsException(from+" "+to);
	
	RealtimeList list = this;

	return new_RealtimeList_2(list,from,to);
    }

    
    public boolean equals(Object o) {
	if(o == this) return true;
	if(!(o instanceof List)) return false;

	ListIterator i1 = listIterator(), i2 = ((List)o).listIterator();
	while(i1.hasNext() && i2.hasNext()) {
	    if(!equals(i1.next(), i2.next())) return false;
	}

	if(i1.hasNext() || i2.hasNext()) return false; // length not equal
	return true;
    }

    public int hashCode() {
	int result = 1;

	for(ListIterator i = listIterator(); i.hasNext();) {
	    Object o = i.next();
	    result *= 31;
	    result += (o == null ? 0 : o.hashCode());
	}

	return result;
    }

        private static class _ListIterator_1 extends RealtimeObject implements ListIterator {

            private _ListIterator_1() {}

            RealtimeList list; int index; 
					    int lastIndex; int lastModCount;

            
	    public boolean hasNext() { return index < list.size(); }
	    public boolean hasPrevious() { return index > 0; }

	    public Object next() { 
		if(list.modCount > lastModCount) 
		    throw new ConcurrentModificationException();
		Object o = list.get(index); lastIndex = index; index++; return o;
	    }
	    public Object previous() { 
		if(list.modCount > lastModCount) 
		    throw new ConcurrentModificationException();
		Object o = list.get(index-1); index--; lastIndex = index; return o;
	    }

	    public int nextIndex() { return index; }
	    public int previousIndex() { return index-1; }

	    public void add(Object o) { 
		if(list.modCount > lastModCount) 
		    throw new ConcurrentModificationException();
		list.add(index, o); 
		index++; 
		lastIndex = -1;
		list.modCount++; lastModCount = list.modCount;
	    }
	    public void set(Object o) { 
		if(list.modCount > lastModCount) 
		    throw new ConcurrentModificationException();
		if(lastIndex < 0) throw new IllegalStateException();
		list.set(lastIndex, o); 
	    }
	    public void remove() { 
		if(list.modCount > lastModCount) 
		    throw new ConcurrentModificationException();
		if(lastIndex < 0) throw new IllegalStateException();
		list.remove(lastIndex); 
		if(lastIndex < index) index--;
		lastIndex = -1;
		list.modCount++; lastModCount = list.modCount;
	    }
	

            public boolean move(ObjectSpace os) {
                if(super.move(os)) {
                    if(list instanceof Realtime) ((Realtime)list).move(os); 
                    return true;
                }
                return false;
            }
        }

        private static final RealtimeObject.Factory _ListIterator_1_FACTORY =
            new RealtimeObject.Factory() {
                protected Object create() { return new _ListIterator_1(); }
            };

        private static _ListIterator_1 new_ListIterator_1(RealtimeList list, int index, 
					    int lastIndex, int lastModCount) {
            _ListIterator_1 the_new_ListIterator_1 = (_ListIterator_1)_ListIterator_1_FACTORY.object();
            the_new_ListIterator_1.list = list;
the_new_ListIterator_1.index = index;
the_new_ListIterator_1.lastIndex = lastIndex;
the_new_ListIterator_1.lastModCount = lastModCount;
            return the_new_ListIterator_1;
        }
    
        private static class _RealtimeList_2 extends RealtimeList {

            private _RealtimeList_2() {}

            RealtimeList list; 
					  int from; int to;

            
	    public Object get(int i) {
		if(i > to-from) throw new IndexOutOfBoundsException(""+i);
		return list.get(from+i);
	    }
	    public int size() {
		return to-from;
	    }
	    public void add(int i, Object o) {
		if(i > to-from) throw new IndexOutOfBoundsException(""+i);
		to++;
		list.add(i, o);
	    }
	    public Object set(int i, Object o) {
		if(i > to-from) throw new IndexOutOfBoundsException(""+i);
		return list.set(from+i, o);
	    }
	    public Object remove(int i) {
		if(i > to-from) throw new IndexOutOfBoundsException(""+i);
		to--;
		return list.remove(from+i);
	    }
	    public boolean addAll(int i, Collection items) {
		if(i > to-from) throw new IndexOutOfBoundsException(""+i);
		boolean result = list.addAll(from+i, items);
		to += items.size();
		return result;
	    }
	    protected void removeRange(int rfrom, int rto) {
		if(rto < rfrom) 
		    throw new IllegalArgumentException(rfrom+" "+rto);
		if(rfrom < 0 || rto > to-from)
		    throw new IndexOutOfBoundsException(rfrom+" "+rto);

		list.removeRange(from+rfrom, from+rto);
		to -= (rto - rfrom);
	    }
	

            public boolean move(ObjectSpace os) {
                if(super.move(os)) {
                    if(list instanceof Realtime) ((Realtime)list).move(os); 
                    return true;
                }
                return false;
            }
        }

        private static final RealtimeObject.Factory _RealtimeList_2_FACTORY =
            new RealtimeObject.Factory() {
                protected Object create() { return new _RealtimeList_2(); }
            };

        private static _RealtimeList_2 new_RealtimeList_2(RealtimeList list, 
					  int from, int to) {
            _RealtimeList_2 the_new_RealtimeList_2 = (_RealtimeList_2)_RealtimeList_2_FACTORY.object();
            the_new_RealtimeList_2.list = list;
the_new_RealtimeList_2.from = from;
the_new_RealtimeList_2.to = to;
            return the_new_RealtimeList_2;
        }
    }