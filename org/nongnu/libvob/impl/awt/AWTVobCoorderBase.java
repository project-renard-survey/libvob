/*
AWTVobCoorder.java
 *
 *    Copyright (c) 2004, Matti J. Katila
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
 * Written by Matti J. Katila
 */
package org.nongnu.libvob.impl.awt;
import org.nongnu.libvob.*;
import org.nongnu.libvob.impl.*;
import org.nongnu.libvob.gl.GL;
import java.util.*;

/** This is an internal base class for AWTVobCoorder.
 */
public abstract class AWTVobCoorderBase extends VobCoorder {
    public static boolean dbg = false;
    private static void p(String s) { System.out.println("AWTCoorder:: "+s); }


    float width, height;
    final DepthSorter sorter;
    public AWTVobCoorderBase(float w, float h) {
	width = w; height = h;
	sorter = new DepthSorter(this);
    }


    float[] floats = new float[512];
    int nfloats = 0;

    int[] inds = new int[128];
    int ninds = 1; // zero is special (the root)
    ChildVobScene[] children = new ChildVobScene[128];

    public void clear() {
	nfloats = 0;
	ninds = 1;
    }


    protected final void addFloats(int n) { 
	nfloats += n; 
	if(nfloats > floats.length) {
	    float[] nf = new float[floats.length*2];
	    System.arraycopy(floats, 0, nf, 0, floats.length);
	    floats = nf;
	}
    }
    protected final void addInds(int n) { 
	ninds += n; 
	if(ninds > inds.length) {
	    int[] ni = new int[inds.length*2];
	    ChildVobScene[] nc = new ChildVobScene[inds.length*2];
	    System.arraycopy(inds, 0, ni, 0, inds.length);
	    System.arraycopy(children, 0, nc, 0, inds.length);
	    inds = ni; children = nc;
	}
    }


    static float i(float a, float b, float fract)
	{ return (a + fract * (b-a)); }


    float[] cs1rect = new float[5];
    float[] cs2rect = new float[5];
    float[] wh = new float[4];
    float[] scale = new float[2];

    protected void getAbsoluteRect(int cs, float[] into, float[] scale) {
	for(int i=0; i<5; i++) 
	    into[i] = 0;

	getSqSize(cs, wh);
	into[2] = wh[0]; into[3] = wh[1];

	if (dbg) {
	    for (int i=0; i<4; i++)
		p("info: "+into[i]);
	}
	
	Trans t = getTrans(cs);
	t.transformRect(into);
	scale[0] = t.sx(); scale[1] = t.sy();
	t.pop();
    }

    public void setInterpInfo(int cs1, AWTVobCoorderBase other, int cs2,
			      float fract,
			      OrthoRenderInfo info
			) {

	this.getAbsoluteRect(cs1, cs1rect, scale);
	float sx1 = scale[0], sy1 = scale[1];
	
	other.getAbsoluteRect(cs2, cs2rect, scale);
	float sx2 = scale[0], sy2 = scale[1];

	if (dbg) check();
	
	if (dbg) {
	    for (int i=0; i<4; i++)
		p("info 1: "+cs1rect[i]);
	    p("sx: "+sx1+", sy: "+sy1);
	}
	info.setCoords(i(cs1rect[4], cs2rect[4], fract),// depth
		       i(cs1rect[0], cs2rect[0], fract),
		       i(cs1rect[1], cs2rect[1], fract),
		       i(cs1rect[2], cs2rect[2], fract),
		       i(cs1rect[3], cs2rect[3], fract),
		       i(sx1, sx2, fract),
		       i(sy1, sy2, fract));
    }

    public void setInfo(int cs, OrthoRenderInfo info) {
	setInterpInfo(cs, this, cs, 0, info);
    }

    
    Trans noOp = new Trans(){
	    public String toString() { return "no op"; }
	    void transformRect(float[] rect) { 
		Trans t = getParentTrans();
		t.transformRect(rect);
		t.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans t = getParentTrans();
		t.inverseTransformRect(rect);
		t.pop();
	    }
	};

    Trans root = new Trans(){
	    public String toString() { return "root"; }
	    void transformRect(float[] rect) { 
	    }
	    void inverseTransformRect(float[] rect) { 
	    }
	    float sx() { return width; }
	    float sy() { return height; }
	    float w() { return 1; }
	    float h() { return 1; }
	};

    
    Trans [] trans =  
    new Trans[] {
	noOp, // 0 rational1D22
	noOp, // 1 power1D
	noOp, // 2 power1D2
	noOp, // 3 distort
	noOp, // 4 cull
	new Trans() {   // 5 concat
	    public String toString() { return "concat"; }
	    void transformRect(float[] rect) { 
		Trans g = getTrans(inds[cs()+2]);
		g.transformRect(rect);
		g.pop();

		Trans f = getTrans(inds[cs()+1]);
		f.transformRect(rect);
		f.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans f = getTrans(inds[cs()+1]);
		f.inverseTransformRect(rect);
		f.pop();

		Trans g = getTrans(inds[cs()+2]);
		g.inverseTransformRect(rect);
		g.pop();
	    }
	},
	new Trans() {   // 6 concatInverse
	    public String toString() { return "concat inverse"; }
	    void transformRect(float[] rect) { 
		Trans g = getTrans(inds[cs()+2]);
		g.inverseTransformRect(rect);
		g.pop();

		
		Trans f = getTrans(inds[cs()+1]);
		f.transformRect(rect);
		f.pop();

	    }
	    void inverseTransformRect(float[] rect) { 

		Trans f = getTrans(inds[cs()+1]);
		f.inverseTransformRect(rect);
		f.pop();

		Trans g = getTrans(inds[cs()+2]);
		g.transformRect(rect);
		g.pop();

	    }
	},
	new Trans() {   // 7 translate
	    public String toString() { return "translate"; }
	    void transformRect(float[] rect) { 
		int f = inds[cs()+2];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+i];
		rect[4] += floats[f+2]; // depth
		Trans t = getParentTrans();
		t.transformRect(rect);
		t.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans p = getParentTrans();
		p.inverseTransformRect(rect);
		p.pop();
 		int f = inds[cs()+2];
		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+i];
		rect[4] -= floats[f+2]; // depth
	    }
	},
	new Trans() {   // 8 scale
	    public String toString() { return "scale"; }
	    void transformRect(float[] rect) { 
		int f = inds[cs()+2];
		for (int i=0; i<2; i++) {
		    rect[i] *= floats[f+i];
		    rect[i+2] *= floats[f+i];
		}
		rect[4] *= floats[f+2]; // depth
		Trans p = getParentTrans();
		p.transformRect(rect);
		p.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans p = getParentTrans();
		p.inverseTransformRect(rect);
		p.pop();
		int f = inds[cs()+2];
		for (int i=0; i<2; i++) {
		    rect[i] /= floats[f+i];
		    rect[i+2] /= floats[f+i];
		}
		rect[4] /= floats[f+2]; // depth
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+0];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+1];
	    }
	}, 
	noOp, // 9 rotate
	noOp, // 10 nadirUnitSq
	noOp, // 11 unit
	new Trans() {   // 12 box
	    public String toString() { return "box"; }
	    void transformRect(float[] rect) { 
		Trans t = getParentTrans();
		t.transformRect(rect);
		t.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans t = getParentTrans();
		t.inverseTransformRect(rect);
		t.pop();
	    }
	    float w() {
		int f = inds[cs()+2];
		return floats[f+0];
	    }
	    float h() {
		int f = inds[cs()+2];
		return floats[f+1];
	    }
	},
	noOp, // 13 rotateXYZ
	noOp, // 14 rotateQuaternion	    
	noOp, // 15 affine
	new Trans() {   // 16 ortho
	    public String toString() { return "ortho"; }
	    void transformRect(float[] rect) { 
		int f = inds[cs()+2];
		rect[4] += floats[f+0]; // depth

		rect[0] *= floats[f+3];
		rect[1] *= floats[f+4];
		rect[2] *= floats[f+3];
		rect[3] *= floats[f+4];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+1+i];
		Trans p = getParentTrans();
		p.transformRect(rect);
		p.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans p = getParentTrans();
		p.inverseTransformRect(rect);
		p.pop();

		int f = inds[cs()+2];
		rect[4] -= floats[f+0]; // depth

		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+1+i];

		rect[0] /= floats[f+3];
		rect[1] /= floats[f+4];
		rect[2] /= floats[f+3];
		rect[3] /= floats[f+4];
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+3];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+4];
	    }
	}, 
	noOp, // 17 buoyOnCircle1
	noOp, // 18 buoyOnCircle2
	new Trans() {   // 19 orthoBox
	    public String toString() { return "orthoBox"; }
	    void transformRect(float[] rect) { 
		int f = inds[cs()+2];

		rect[4] += floats[f+0]; // depth

		rect[0] *= floats[f+3];
		rect[1] *= floats[f+4];
		rect[2] *= floats[f+3];
		rect[3] *= floats[f+4];
		for (int i=0; i<2; i++)
		    rect[i] += floats[f+1+i];

		Trans p = getParentTrans();
		p.transformRect(rect);
		p.pop();
	    }
	    void inverseTransformRect(float[] rect) { 
		Trans p = getParentTrans();
		p.inverseTransformRect(rect);
		p.pop();
		int f = inds[cs()+2];

		rect[4] -= floats[f+0]; // depth

		for (int i=0; i<2; i++)
		    rect[i] -= floats[f+1+i];

		rect[0] /= floats[f+3];
		rect[1] /= floats[f+4];
		rect[2] /= floats[f+3];
		rect[3] /= floats[f+4];
	    }
	    float sx() {
		int f = inds[cs()+2];
		return floats[f+3];
	    }
	    float sy() {
		int f = inds[cs()+2];
		return floats[f+4];
	    }
	    float w() {
		int f = inds[cs()+2];
		return floats[f+5];
	    }
	    float h() {
		int f = inds[cs()+2];
		return floats[f+6];
	    }
	}, 
	new Trans() {   // 20 unitSq
	    void transformRect(float[] rect) { 
		throw new Error("unitSq unimplemented");
	    }
	    void inverseTransformRect(float[] rect) { 
		throw new Error("unitSq unimplemented");
	    }
	},
    };
    
    Trans getTrans(int cs) {
	if (dbg) p("cs "+cs+ ", "+inds[cs]+(parentCoordsys ==null));
	if (cs < numberOfParameterCS || (inds[cs] < 0 && inds[cs] > -3)) {
	    
	    // it may also be export!
	    // no it may not because cs < numberOfParameterCS
	    if (parentCoordsys == null) {
		root.push(cs);
		return root;
	    }

	    // there is parent coorder...

	    if((cs >= 0) && (cs < numberOfParameterCS)) {
		// go to parent vob coorder

		int n = cs;
		int len = parentCoordsys.inds[parentCS+2];
		if (n >= len) throw new Error("cs too big");
		int c = parentCoordsys.inds[parentCS+3+n];
		return parentCoordsys.getTrans(c);
	    }
	    // check child coorder, i.e., parent: -1 = put 
	    // and -2 = export
	    if (inds[cs] == -1) return noOp;

	    if (inds[cs] == -2) {
		ChildVobScene cvs = children[inds[cs+1]];
		AWTVobCoorderBase coords = (AWTVobCoorderBase)cvs.coords;
		coords.parentCoordsys = this;
		coords.parentCS = inds[cs+1];
		return coords.getTrans(inds[cs+2]);
	    }
	    throw new Error("Help! Wrong coordsys: "+cs);
	} else {
	    if (dbg) p(", ind: "+inds[cs]+", "+isActive(cs));
	    Trans t = trans[inds[cs] & (~GL.CSFLAGS)];
	    t.push(cs);
	    return t;
	}
    }
    

    public void check() {
	for (int i=0; i<trans.length; i++) 
	    if (trans[i].csInd != 0)
		throw new Error(trans[i] +" is guilty!");
	if(parentCoordsys != null)
	    parentCoordsys.check();
    }

    public abstract class Trans {
	int cs[] = new int[20];
	int csInd = 0;
	void push(int cs) { 
	    if (dbg) {
		System.out.print(csInd+": ");
		for (int i=0; i<csInd; i++)
		    System.out.print(" ");
		System.out.println("push "+cs+", "+this+ ", "+(parentCoordsys ==null));
	    }
	    this.cs[++csInd] = cs; 
	}
	int cs() { return cs[csInd]; }
	void pop() { 
	    if (dbg)
		System.out.println("pop "+ cs() +", "+this+ ", "+(parentCoordsys ==null));
	    csInd--; 
	}

	int getParent() { return inds[cs()+1]; }
	Trans getParentTrans() { return getTrans(inds[cs()+1]); }
	abstract void transformRect(float[] rect);
	abstract void inverseTransformRect(float[] rect);
	float sx() { 
	    Trans p = getParentTrans();
	    float x = p.sx();
	    p.pop();
	    return x; 
	}
	float sy() { 
	    Trans p = getParentTrans();
	    float y = p.sy();
	    p.pop();
	    return y; 
	}
	float w() { return 1; }
	float h() { return 1; }
    }


    public void getSqSize(int cs, float[] into) {
	Trans t = getTrans(cs);
	into[0] = t.w();
	into[1] = t.h();
	t.pop();
    }


    public float[] transformPoints3(int withCS, float[] pt, float[]into) {
	if(into == null)
	    into = new float[pt.length];
	float[] rect = new float[] { 0, 0, 1, 1, 0};
	Trans t = getTrans(withCS);
	t.transformRect(rect);
	t.pop();

	float ox = rect[0];
	float oy = rect[1];
	float sx = rect[2];
	float sy = rect[3];
	for(int i=0; i<pt.length; i+=3) {
	    into[i + 0] = ox + sx * pt[i + 0];
	    into[i + 1] = oy + sy * pt[i + 1];
	    into[i + 2] = rect[4] + pt[i + 2];
	}
	return into;
    }

    public float[] inverseTransformPoints3(int withCS, float[] pt, 
					   float[]into) {
	if(into == null)
	    into = new float[pt.length];
	System.arraycopy(pt, 0, into, 0, pt.length);

	Trans t = getTrans(withCS);
	t.inverseTransformRect(into);
	t.pop();

	return into;
    }





    int parentCS = 0;
    AWTVobCoorderBase parentCoordsys = null;


    public void activate(int cs) {
	inds[cs] |= GL.CSFLAG_ACTIVE;
	//checkActiveRegion(cs);
    }

    private boolean isActive(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE) != 0;
    }
    public int getParent(int cs) {
	if(cs == 0) return -1;
	return inds[cs+1];
    }

    public void dump() {
	// XXX check DepthSorter...

	p("Coorder dump!");
	for(int i=0; i<ninds; i++) {
	    p("   "+i+", parent: "+getParent(i));
	}
    }

    public boolean needInterp(VobCoorder interpTo0, int[] interpList) {
        AWTVobCoorderBase interpTo = (AWTVobCoorderBase)interpTo0;
	for(int my=0; my<interpList.length; my++) {

	    int other = interpList[my];

	    if(other > 0)
	        if(needInterp(my, interpTo, other)) return true;
	}
	return false;
    }

    public boolean needInterp(int cs1, AWTVobCoorderBase coords2, int cs2) {
	AWTVobCoorderBase coords1 = this;

	coords1.getAbsoluteRect(cs1, cs1rect, scale);
	coords2.getAbsoluteRect(cs2, cs2rect, scale);

	float 
	    x1 = cs1rect[0], y1 = cs1rect[1], w1 = cs1rect[2], h1 = cs1rect[3],
	    x2 = cs2rect[0], y2 = cs2rect[1], w2 = cs2rect[2], h2 = cs2rect[3];

	if(Math.abs(x1 - x2) + Math.abs(y1 - y2) + 
	   Math.abs(w1 - w2) + Math.abs(h1 - h2) > 5) // heuristic
	    
	    return true;
	else
	    return false;
    }








    abstract public int concatInverse(int f, int g);
    // Then some simple implementations
    public int invert(int f) {
	return concatInverse(0, f);
    }

    public boolean isChildVS(int cs) { 
	return children[cs] != null;
    }

    public int _putChildVobScene(ChildVobScene child, int[] cs) {
	int j=ninds;

	children[j] = child;

	inds[j+0] = -1; // Code for child vobscene
	inds[j+1] = j;
	inds[j+2] = cs.length;
	for(int i=0; i<cs.length; i++)
	    inds[j+3+i] = cs[i];

	ninds += 3+cs.length;

	return j;
    }


    // Stupid implementation...
    public int getCSAt(int parent, float x, float y, float[] internalcoords) {

	sorter.sort();
	int[] sorted = sorter.sorted;
	int nsorted = sorter.nsorted;

	float d[] = new float[5];

	for(int i=nsorted-1; i>=0; i--) {

	    if(isActive(sorted[i]) && 
	       (getParent(sorted[i]) == parent || parent == -1)) {
		Trans t = getTrans(sorted[i]);
		d[0] = d[1] = 0;
		d[2] = t.w();
		d[3] = t.h();
		t.transformRect(d);
		t.pop();

		if (x >= d[0] && y >= d[1] &&
		    x < d[0]+d[2] && y < d[1]+d[3]) {
		    if(internalcoords != null) {
			internalcoords[0] = (x-d[0])/(d[2]);
			internalcoords[1] = (y-d[1])/(d[3]);
		    }
		    return sorted[i];
		}
	    }
	}
	return -1;
    }



    public int exportChildCoordsys(int childVobSceneId, int nth) {
	int j=ninds;
	inds[j+0] = -2; // Code for child vobscene cs export
	inds[j+1] = childVobSceneId; // index in list
	inds[j+2] = nth;
	ninds += 3;
	return j;
    }
    
    /** Internal API: to be called right after creation,
     * to leave room for other coordsyses.
     */
    void setNumberOfParameterCS(int numberOfParameterCS) {
	ninds = this.numberOfParameterCS = numberOfParameterCS;
    }
    int numberOfParameterCS = 1;










    Map actChildren = new HashMap();
    public void activateChildByCS(int cs, int childCS) {
	inds[cs] |= GL.CSFLAG_ACTIVE_REGION;
	actChildren.put(cs+"", ""+childCS);
    }
    public boolean hasActiveChildVS(int cs) {
	return (inds[cs] & GL.CSFLAG_ACTIVE_REGION) != 0;
    }
    public ChildVobScene getChildByCS(int cs) {
	return children[Integer.parseInt((String) actChildren.get(""+cs))];
    }


    public int getChildCSAt(int[] activateCSs, 
			    int parent, 
			    float x, float y, 
			    float[] targetcoords) {
	int [] css = new int[activateCSs.length];
	for (int i=0; i<css.length; i++)
	    css[i] = Integer.parseInt((String)actChildren.get(""+activateCSs[i]));

	ChildVobScene cvs = children[css[0]];
	((AWTVobCoorderBase)cvs.coords).parentCoordsys = this;
	((AWTVobCoorderBase)cvs.coords).parentCS = css[0];
	for (int i=1; i<css.length; i++) {
	    ChildVobScene oldCvs = cvs;
	    cvs = ((AWTVobCoorderBase)cvs.coords).children[css[i]];
	    ((AWTVobCoorderBase)cvs.coords).parentCoordsys = 
		((AWTVobCoorderBase)oldCvs.coords);
	    ((AWTVobCoorderBase)cvs.coords).parentCS = css[i];
	}

	return cvs.getCSAt(parent, x,y, targetcoords);
    }


}


