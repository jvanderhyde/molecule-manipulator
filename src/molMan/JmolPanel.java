//Panel for holding a single Jmol applet
//Created by James Vanderhyde, 3 June 2013
//  Refactored out of Molly.java where it was an inner class

package molMan;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Hashtable;
import javax.swing.JPanel;
import javax.vecmath.AxisAngle4f;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolViewer;

/**
 * JmolPanel creates a link between the Jmol viewer and our Swing interface by putting the
 * Jmol view in a JPanel so it can be dropped into any other JPanel.
 * @author This code is a modified version of code found at:
 * 	http://biojava.org/wiki/BioJava:CookBook:PDB:Jmol
 * 	Modified by Josh Kuestersteffen
 */
class JmolPanel extends JPanel
{
    private static final long serialVersionUID = -3661941083797644242L;
    JmolViewer viewer;
    JmolAdapter adapter;

    JmolPanel() {
        adapter = new SmarterJmolAdapter();
        //TODO See if we can figure out if this will help our problem of loading molecules...
        //From: http://old.nabble.com/AccessControlException-when-opening-embedded-Jmol-instance-when-accessing-PDB-via-URL.-td29492618.html
        //String fullname = htmlName + "__" + Math.random() + "__";
        //JmolViewer viewer = JmolViewer.allocateviewer(yourApplet, null, fullName, documentBase, codeBase, "-applet", null)
        //String fullname = htmlName + "__" + Math.random() + "__";
        viewer = JmolViewer.allocateViewer((java.awt.Component)this, adapter, null, null, null, "-applet", null, null);
        viewer.evalString("set antialiasDisplay ON");
    }

    public JmolViewer getViewer() {
        return viewer;
    }

    public void executeCmd(String rasmolScript) {
        viewer.evalString(rasmolScript);
    }

    /**
     * Sets the current orientation (rotation) of this Jmol viewer
     * to the orientation of the given viewer.
     * @param source The JmolPanel whose orientation is to be copied
     */
    public void copyOrientation(JmolPanel source) {
        String moveToCommand = source.getMoveToScript();
        this.executeCmd(moveToCommand);
    }

    private Object getOrientationInfoObject(String key) {
        Hashtable h;
        Object data = viewer.getProperty("object", "orientationInfo", null);
        if (data instanceof Hashtable) {
            h = (Hashtable) data;
        } else {
            throw new IllegalStateException("Orientation info is not a Hashtable, it is a " + data.getClass().toString());
        }
        if (h.containsKey(key)) {
            Object o = h.get(key);
            return o;
        } else {
            String error = "Unknown key: " + key + " Possible keys:";
            for (Object o : h.keySet()) {
                error += " " + o.toString() + " ";
            }
            throw new IllegalStateException(error);
        }
    }

    private String getMoveToScript() {
        Object o = getOrientationInfoObject("moveTo");
        if (o instanceof String) {
            String s = (String) o;
            s = s.replaceFirst("1", "0");
            return s;
        } else {
            throw new IllegalStateException("moveTo is a " + o.getClass());
        }
    }

    public float[] getAxisAngle() {
        Object o = getOrientationInfoObject("axisAngle");
        if (o instanceof AxisAngle4f) {
            AxisAngle4f aa = (AxisAngle4f) o;
            float[] aaa = new float[4];
            aa.get(aaa);
            return aaa;
        } else {
            throw new IllegalStateException("axisAngle is a " + o.getClass());
        }
    }
    final Dimension currentSize = new Dimension();
    final Rectangle rectClip = new Rectangle();

    @Override
    public void paint(Graphics g) {
        getSize(currentSize);
        g.getClipBounds(rectClip);
        viewer.renderScreenImage(g, rectClip.width, rectClip.height);
    }
    
}
