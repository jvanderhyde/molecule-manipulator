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
    JmolPanel jmolPanel2;
    private final int W = 1400;
    private final int H = 800;
	

	public void init()
	{
   
		jmolPanel = new JmolPanel();
                jmolPanel2 = new JmolPanel();
        
        jmolPanel.setPreferredSize(new Dimension(300,300));
        jmolPanel2.setPreferredSize(new Dimension(300, 300));
        setStructure();
		
		/*JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JButton button = new JButton("This is a button...");
		
		panel.add(button, BorderLayout.WEST);
		panel.add(jmolPanel, BorderLayout.EAST);
		this.add(panel);*/
                //sets up main applet window
        this.setPreferredSize(new Dimension(W,H));
        //sets up layout of main window
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //creates each section of the layout
        JPanel top = new JPanel();
        JPanel middle = new JPanel();
        JPanel bottom = new JPanel();
        //sets layout for each section
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        //logo for the program
        JPanel logo = new JPanel();
        logo.setSize(new Dimension(150, 150));
        //label for the text box
        JPanel mol = new JPanel();
        mol.setLayout(new FlowLayout());
        JLabel molLabel = new JLabel("Please input a molecular formula:");
        mol.add(molLabel);
        //text box
        JPanel text = new JPanel();
        text.setLayout(new FlowLayout());
        JTextField input = new JTextField(50);
        text.add(input);
        //draw button
        JPanel button = new JPanel();
        button.setLayout(new FlowLayout());
        JButton draw = new JButton("Draw");
        button.add(draw);
        //add elements to the top pane
        top.add(logo);
        top.add(Box.createRigidArea(new Dimension(50, 150)));
        top.add(mol);
        top.add(Box.createRigidArea(new Dimension(10, 150)));
        top.add(text);
        top.add(Box.createRigidArea(new Dimension(10, 150)));
        top.add(button);
        top.add(Box.createRigidArea(new Dimension(10, 150)));
        
        //creates tabbed display
        JTabbedPane tabs = new JTabbedPane();
        JPanel rot, inv, rotInv, res;
        rot = new JPanel();
        inv = new JPanel();
        rotInv = new JPanel();
        res = new JPanel();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.addTab("Rotation", null, rot, "Rotate the molecule around an axis");
        tabs.addTab("Inversion", null, inv, "Invert the molecule through a plane");
        tabs.addTab("Rot & Inv", null, rotInv, "");
        tabs.addTab("Reset", null, res, "View GPA here");
        
        //adds tabbed display to the middle of the layout
        JPanel molViewer1 = new JPanel();
        JPanel molViewer2 = new JPanel();
        molViewer1.setLayout(new FlowLayout());
        molViewer2.setLayout(new FlowLayout());
        molViewer1.add(jmolPanel);
        molViewer2.add(jmolPanel2);
        middle.add(Box.createRigidArea(new Dimension(5, 300)));
        middle.add(tabs);
        middle.add(Box.createRigidArea(new Dimension(10, 300)));
        middle.add(molViewer1);
        middle.add(Box.createRigidArea(new Dimension(10, 300)));
        middle.add(molViewer2);
        middle.add(Box.createRigidArea(new Dimension(20, 300)));
        //create previous/next buttons
        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        //Label
        JLabel molVar = new JLabel("Molecule Variations");
        //text output area
        JTextField out = new JTextField(45);
        out.setEditable(false);
        //set up bottom layout
        bottom.add(Box.createRigidArea(new Dimension(400, 100)));
        bottom.add(previous);
        bottom.add(molVar);
        bottom.add(next);
        bottom.add(out);
        //adds each section to applet window
        this.add(Box.createRigidArea(new Dimension(W, 50)));
        this.add(top);
        this.add(Box.createRigidArea(new Dimension(W, 20)));
        this.add(middle);
        this.add(Box.createRigidArea(new Dimension(W, 5)));
        this.add(bottom);
        this.add(Box.createRigidArea(new Dimension(W, 2)));
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