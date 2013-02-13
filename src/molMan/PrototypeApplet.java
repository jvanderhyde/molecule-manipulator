package molMan;
//Reference the required Java libraries
import java.applet.Applet; 
import java.awt.*; 

import javax.swing.*;

import molMan.SimpleJmolExample.JmolPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;

//The applet code
public class PrototypeApplet extends Applet {

	private static final long serialVersionUID = 1L;
	
	JmolSimpleViewer viewer;
    String structurePbd;
    JmolPanel jmolPanel;
	

	public void init()
	{
   
		jmolPanel = new JmolPanel();
        
        jmolPanel.setPreferredSize(new Dimension(400,400));
        setStructure();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JButton button = new JButton("This is a button...");
		
		panel.add(button, BorderLayout.WEST);
		panel.add(jmolPanel, BorderLayout.EAST);
		this.add(panel);
	}
	
	public void setStructure() 
	{
		 
        JmolSimpleViewer viewer = jmolPanel.getViewer();
 
        viewer.openFile("5PTI.pdb");
 
        // send the PDB file to Jmol.
        // there are also other ways to interact with Jmol, but they require more
        // code. See the link to SPICE above...
        //viewer.openStringInline(pdb);
        viewer.evalString("select *; spacefill off; wireframe off; backbone 0.4;  ");
        viewer.evalString("color chain;  ");
        //viewer.evalString("rotate on;");
        
        this.viewer = viewer; 
    }
	
	
	
	//This code is basically copied from SimpleJmolExample....
	static class JmolPanel extends JPanel 
	{
        /**
         * 
         */
        private static final long serialVersionUID = -3661941083797644242L;
        JmolSimpleViewer viewer;
        JmolAdapter adapter;
        JmolPanel() {
            adapter = new SmarterJmolAdapter();
            viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
 
        }
 
        public JmolSimpleViewer getViewer() 
        {
            return viewer;
        }
 
        public void executeCmd(String rasmolScript){
            viewer.evalString(rasmolScript);
        }
 
 
        final Dimension currentSize = new Dimension();
        final Rectangle rectClip = new Rectangle();
 
        public void paint(Graphics g) 
        {
            getSize(currentSize);
            g.getClipBounds(rectClip);
            
            ///viewer.renderScreenImage(g, currentSize, rectClip);
            viewer.renderScreenImage(g, rectClip.width, rectClip.height);
        }
    }
} 