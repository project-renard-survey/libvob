/*   
Models.java
 *    
 *    Copyright (c) 2004, Benja Fallenstein
 *
 *    This file is part of Libvob.
 *    
 *    Fenfire is free software; you can redistribute it and/or modify it under
 *    the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *    
 *    Fenfire is distributed in the hope that it will be useful, but WITHOUT
 *    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *    or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *    Public License for more details.
 *    
 *    You should have received a copy of the GNU General
 *    Public License along with Fenfire; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA  02111-1307  USA
 *
 */
package org.nongnu.libvob.layout;
import org.nongnu.navidoc.util.*;
import java.util.*;
import java.lang.reflect.*;

public class Models {
    private static Object[] NO_PARAMS = {};

    public static Model cache(Model m) {
	return new CacheModel(m);
    }


    public static Model forall(CollectionModel collection, Model condition) {
	ListModel list = new ListModel.ListCache(collection);
	list = new ListModel.Transform(list, condition);
	
	return new FunctionModel(list) { public Object f(Object o, Obs obs) {
	    for(Iterator i=((Collection)o).iterator(); i.hasNext();) {
		Model m = (Model)i.next();
		m.addObs(obs);
		if(!m.getBool())
		    return Boolean.FALSE;
	    }

	    return Boolean.TRUE;
	}};
    }


    public static Model min(final Model m1, final Model m2) {
	return new AbstractModel.AbstractFloatModel() {
		{ m1.addObs(this); m2.addObs(this); }
		protected Replaceable[] getParams() {
		    return new Replaceable[] { m1, m2 };
		}
		protected Object clone(Object[] params) {
		    return min((Model)params[0], (Model)params[1]);
		}

		public float getFloat() {
		    float f1 = m1.getFloat(), f2 = m2.getFloat();
		    return (f1<f2) ? f1 : f2;
		}
		public void setFloat(float value) {
		    m1.setFloat(value);
		}
	    };
    }

    public static Model max(final Model m1, final Model m2) {
	return new AbstractModel.AbstractFloatModel() {
		{ m1.addObs(this); m2.addObs(this); }
		protected Replaceable[] getParams() {
		    return new Replaceable[] { m1, m2 };
		}
		protected Object clone(Object[] params) {
		    return max((Model)params[0], (Model)params[1]);
		}

		public float getFloat() {
		    float f1 = m1.getFloat(), f2 = m2.getFloat();
		    return (f1>f2) ? f1 : f2;
		}
		public void setFloat(float value) {
		    m1.setFloat(value);
		}
	    };
		
    }


    public static Model parseFloat(final Model m) {
	return new AbstractModel.AbstractFloatModel() {
		{ m.addObs(this); chg(); }
		protected Replaceable[] getParams() {
		    return new Replaceable[] { m };
		}
		protected Object clone(Object[] params) {
		    return parseFloat((Model)params[0]);
		}

		private float value;

		public void chg() {
		    String s = (String)m.get();
		    if(s == null || s.equals("")) 
			value = 0;
		    else
			value = Float.parseFloat(s);
		}

		public float getFloat() {
		    return value;
		}
		public void setFloat(float value) {
		    m.set(""+value);
		}
	    };
		
    }


    /** A model reading from a different model than it writes to.
     */
    public static Model readWrite(final Model read, final Model write) {
	return new AbstractModel.AbstractObjectModel() {
		{ read.addObs(this); }
		protected Replaceable[] getParams() {
		    return new Replaceable[] { read, write };
		}
		protected Object clone(Object[] params) {
		    return readWrite((Model)params[0], (Model)params[1]);
		}

		public Object get() {
		    return read.get();
		}
		public void set(Object value) {
		    write.set(value);
		}
	    };
    }


    // adaption of an object's methods as a model
    // so from an object foo, you can create a model whose get() method
    // calls foo.getBlah() and whose set method calls foo.setBlah(...).

    public static Model adaptSlot(Object o, String slot) {
	if(o instanceof Model)
	    throw new IllegalArgumentException("plese use adaptMethod(Model, "+
					       "Class, getMethodName)");
	return adaptSlot(new ObjectModel(o), o.getClass(), slot);
    }

    public static Model adaptSlot(Model m, Class c, String slot) {
	slot = 
	    Character.toUpperCase(slot.charAt(0)) + slot.substring(1);
	return adaptMethods(m, c, "get"+slot, "set"+slot);
    }

    public static Model adaptMethod(Object o, String getMethodName) {
	if(o instanceof Model)
	    throw new IllegalArgumentException("plese use adaptMethod(Model, "+
					       "Class, getMethodName)");
	return adaptMethod(new ObjectModel(o), o.getClass(),
			   getMethodName);
    }

    public static Model adaptMethods(Object o, String getMethodName, 
			      String setMethodName) {
	if(o instanceof Model)
	    throw new IllegalArgumentException("plese use adaptMethod(Model, "+
					       "Class, getMethodName)");
	return adaptMethods(new ObjectModel(o), o.getClass(),
			    getMethodName, setMethodName);
    }

    public static Model adaptMethod(Model m, Class c, String getMethodName) {

	Method getMethod = findMethod(c, getMethodName, 0);

	return cache(new MethodAdapterModel(m, getMethod, null/*XXX*/));
    }

    public static Model adaptMethods(Model m, Class c, String getMethodName,
			      String setMethodName) {

	Method getMethod = findMethod(c, getMethodName, 0);
	Method setMethod = findMethod(c, setMethodName, 1);

	return cache(new MethodAdapterModel(m, getMethod, setMethod));
    }

    public static Model adaptField(Model m, Class c, String fieldName) {

	Field field;
	try {
	    field = c.getField(fieldName);
	} catch(NoSuchFieldException e) {
	    throw new Error(e);
	}
	return cache(new FieldAdapterModel(m, field));
    }

    public static class MethodAdapterModel 
	extends AbstractModel.AbstractObjectModel {

	protected Model model;
	protected Method getMethod, setMethod;

	protected Object[] setParams = new Object[1];

	public MethodAdapterModel(Model model, Method getMethod, 
				  Method setMethod) {
	    this.model = model;
	    this.getMethod = getMethod;
	    this.setMethod = setMethod;

	    model.addObs(this);
	}

	public Replaceable[] getParams() {
	    return new Replaceable[] { model };
	}
	public Object clone(Object[] params) {
	    return new MethodAdapterModel((Model)params[0], 
					  getMethod, setMethod);
	}

	public Object get() {
	    Object o = model.get();
	    if(o instanceof Observable) ((Observable)o).addObs(this);
	    return invoke(o, getMethod, NO_PARAMS);
	}

	public void set(Object value) {
	    Object o = model.get();
	    setParams[0] = value;
	    invoke(o, setMethod, setParams);
	    obses.trigger();
	}
    }

    public static class FieldAdapterModel 
	extends AbstractModel.AbstractObjectModel {

	protected Model model;
	protected Field field;

	public FieldAdapterModel(Model model, Field field) {
	    this.model = model;
	    this.field = field;

	    model.addObs(this);
	}

	public Replaceable[] getParams() {
	    return new Replaceable[] { model };
	}
	public Object clone(Object[] params) {
	    return new FieldAdapterModel((Model)params[0], field);
	}

	public Object get() {
	    Object o = model.get();
	    if(o instanceof Observable) ((Observable)o).addObs(this);
	    try {
		return field.get(o);
	    } catch(IllegalAccessException e) {
		throw new Error(e);
	    }
	}

	public void set(Object value) {
	    Object o = model.get();
	    try {
		field.set(o, value);
	    } catch(IllegalAccessException e) {
		throw new Error(e);
	    }
	    obses.trigger();
	}
    }

    public static class CacheModel 
	extends AbstractModel.AbstractObjectModel {
	
	protected Model model;

	protected Object cachedValue;
	protected boolean cacheIsCurrent;

	public CacheModel(Model model) {
	    this.model = model;
	    model.addObs(this);
	}

	public Replaceable[] getParams() {
	    return new Replaceable[] { model };
	}
	public Object clone(Object[] params) {
	    return new CacheModel((Model)params[0]);
	}

	public void chg() {
	    cacheIsCurrent = false;
	    super.chg();
	}

	public Object get() {
	    if(!cacheIsCurrent) {
		cachedValue = model.get();
		cacheIsCurrent = true;
	    }
	    return cachedValue;
	}

	public void set(Object value) {
	    model.set(value);
	}
    }

    private static Method findMethod(Class c, String name, int nparams) {
	Method[] methods = c.getMethods();
	for(int i=0; i<methods.length; i++) {
	    if(methods[i].getName().equals(name) &&
	       methods[i].getParameterTypes().length == nparams)
		return methods[i];
	}
	throw new NoSuchElementException();
    }

    private static Object invoke(Object o, Method m, Object[] params) {
	try {
	    return m.invoke(o, params);
	} catch(IllegalAccessException e) {
	    throw new Error(e);
	} catch(InvocationTargetException e) {
	    throw new Error(e);
	}
    }
}
