// (c): Matti J. Katila


package org.nongnu.libvob.impl.lwjgl;

import java.awt.Color;

import org.nongnu.libvob.AbstractUpdateManager;
import org.nongnu.libvob.Binder;
import org.nongnu.libvob.GraphicsAPI;
import org.nongnu.libvob.Screen;
import org.nongnu.libvob.Shower;
import org.nongnu.libvob.VobMouseEvent;
import org.nongnu.libvob.VobScene;
import org.nongnu.libvob.GraphicsAPI.Window;
import org.nongnu.libvob.impl.WindowAnimationImpl;
import org.nongnu.libvob.vobs.RectVob;
import org.nongnu.libvob.vobs.SolidBackdropVob;
import org.nongnu.libvob.vobs.TestSpotVob;

public class LWJGL_SimpleVobTester implements Runnable {

    public static void main(String[] args) {
	System.setProperty("vob.api", "lwjgl");
	System.setProperty("java.library.path","../depends/");
	System.setProperties(System.getProperties());
	GraphicsAPI.getInstance().startUpdateManager(new LWJGL_SimpleVobTester());
    }
    
    public static void main(Runnable c) {
	System.setProperty("vob.api", "lwjgl");
	System.setProperty("java.library.path","../depends/");
	System.setProperties(System.getProperties());
	GraphicsAPI.getInstance().startUpdateManager(c);
    }
    
    

    protected VobScene gen(VobScene vs) {
	return vs;
    }
    
    
    public void run() {
	
	final Window window = GraphicsAPI.getInstance().createWindow();
	window.setLocation(0,0,200,200);
    

	
	WindowAnimationImpl windowAnim = new WindowAnimationImpl(window, new Binder(){
	    public void keystroke(String s) {
	    }
	    public void mouse(VobMouseEvent e) {
	    }
	    public void setScreen(Screen s) {
	    }
	    public void timeout(Object id) {
	    }
	    public void repaint() {
	    }
	    public void windowClosed() {
	    }}, new Shower(){
		public VobScene generate(VobScene vs) {
		    vs = window.createVobScene();
		    return gen(vs);
/*
		    vs.put(new SolidBackdropVob(new Color(.9f, .8f, .5f)));
		    int cs = 0;
		    vs.put(new TestSpotVob(0,0,0, Color.BLUE), vs.translateCS(0, "bar", 15,15));
		    vs.put(new RectVob(Color.BLACK, 15), cs=vs.boxCS(0, "foo", 10,20,20,50,50));
		    vs.put(new RectVob(Color.MAGENTA, 5), cs=vs.boxCS(cs, "foo", 10,50,50,25,25));
		    vs.put(new RectVob(Color.RED, 1), cs=vs.scaleCS(cs, "foo", 2,1.5f));
		    return vs;
*/
		}
		public void setScreen(Screen s) {
		}}, false);


	AbstractUpdateManager.addWindow((Screen)windowAnim);
	AbstractUpdateManager.chg();

    }

}
