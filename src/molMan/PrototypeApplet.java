package molMan;
//Reference the required Java libraries
import java.applet.Applet; 
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.swing.*;

import molMan.SimpleJmolExample.JmolPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;

//The applet code
public class PrototypeApplet extends Applet {

	private static final long serialVersionUID = 1L;	
	JmolSimpleViewer viewer0;
	JmolSimpleViewer viewer1;
    String structurePbd;
    JmolPanel jmolPanel0;
    JmolPanel jmolPanel1;
    private final int W = 1400;
    private final int H = 800;
    JScrollPane scrollPane;
    String currentMolecule = "";
    JButton selectButton = new JButton("Select");
    JTextField input = new JTextField(30);
    JLabel currentMolLabel = new JLabel("");
    JmolSimpleViewer view0;
    JmolSimpleViewer view1;

	public void init()
	{	
		//Setup the textarea for the system output to go to.
		JTextArea out = new JTextArea("Output", 7, 26);
		scrollPane = new JScrollPane(out);
		out.setEditable(false);
		   
		try //Redirect System.out to run to our output box.
		{
			PrintStream output = new PrintStream(new RedirectedOut(out), true, "UTF-8");
			System.setOut(output);
			
			System.out.println("Hello World");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
		
		//Calculate the appropriate size for jmolPanel		
		int jmolWidth = this.getWidth()/3;
				
		jmolPanel0 = new JmolPanel();
        jmolPanel1 = new JmolPanel();
        
        jmolPanel0.setPreferredSize(new Dimension(jmolWidth,jmolWidth));
        jmolPanel1.setPreferredSize(new Dimension(jmolWidth, jmolWidth));
        
        setUpGui();
        loadStructure();		     
	}
	
	public void loadStructure() 
	{ 
        view0 = jmolPanel0.getViewer();
        view1 = jmolPanel1.getViewer();
         
        //view0.openFile("5PTI.pdb");
        //view1.openFile("5PTI.pdb");
        
        //viewer.evalString("select *; spacefill off; wireframe off; backbone 0.4;  ");
        //viewer.evalString("color chain;  ");
        
        view0.evalString("load \":caffeine\"; rotate on;");
        view1.evalString("load \":caffeine\"; rotate on;");
        
        this.viewer0 = view0; 
        this.viewer1 = view1;
    }
	
	public void setUpGui()
	{
		///////////////////////////MAIN WINDOW\\\\\\\\\\\\\\\\\\\\\\\\
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
        bottom.setLayout(new BorderLayout());
        
        
        ///////////////////////////TOP SECTION\\\\\\\\\\\\\\\\\\\\\\\\
        //logo for the program
        JPanel logo = new JPanel();
        JLabel logoLabel = new JLabel();
		ImageIcon logoImage = new ImageIcon("logo.jpg", "MolMan");
		logoLabel.setIcon(logoImage);
		logo.add(logoLabel);

		//label for the text box
        JPanel mol = new JPanel();
        mol.setLayout(new FlowLayout());
        JLabel molLabel = new JLabel("Please input a molecular formula:");
        mol.add(molLabel);
        
        //text box
        JPanel text = new JPanel();
        text.setLayout(new FlowLayout());
        text.add(input);
        
        //draw button
        JPanel button = new JPanel();
        button.setLayout(new FlowLayout());
        ButtonListener handler = new ButtonListener();
        selectButton.addActionListener(handler);
        button.add(selectButton);
        input.addActionListener(handler);
     
        //add elements to the NORTH border
        JPanel molName = new JPanel();
        molName.setLayout(new BorderLayout());
        molName.setAlignmentY(CENTER_ALIGNMENT);
        JPanel borderTop = new JPanel();
        borderTop.setLayout(new FlowLayout());
        borderTop.add(mol);
        borderTop.add(text);
        borderTop.add(button);

        //add elements to SOUTH border
        currentMolLabel = new JLabel("Current Molecule: Caffeine");
        currentMolLabel.setFont(new Font("Sans Serif", Font.BOLD, 24));
        JPanel currentPanel = new JPanel();
        currentPanel.setLayout(new FlowLayout());
        currentPanel.setAlignmentY(CENTER_ALIGNMENT);
        currentPanel.add(currentMolLabel);
        
        //Add border to the pane
        molName.add(borderTop, BorderLayout.NORTH);
        molName.add(currentPanel, BorderLayout.CENTER);
        
        //Add all panels to the top
        top.add(logo);       
        top.add(molName);
        
        ///////////////////////////MIDDLE SECTION\\\\\\\\\\\\\\\\\\\\\\\\
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
        JPanel molViewer0 = new JPanel();
        JPanel molViewer1 = new JPanel();
        molViewer0.setLayout(new FlowLayout());
        molViewer1.setLayout(new FlowLayout());
        molViewer0.add(jmolPanel0);
        molViewer1.add(jmolPanel1);
        middle.add(tabs);
        middle.add(molViewer0);
        middle.add(molViewer1);
        
        ///////////////////////////BOTTOM SECTION\\\\\\\\\\\\\\\\\\\\\\\\
        //create previous/next buttons
        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        //Label
        JLabel molVar = new JLabel("  Molecule Variations  ");
        //text output area
        
        JPanel buttonFlow = new JPanel();
        buttonFlow.setLayout(new FlowLayout());
        buttonFlow.add(previous);
        buttonFlow.add(molVar);
        buttonFlow.add(next);
        
        JPanel scrollFlow = new JPanel();
        scrollFlow.setLayout(new FlowLayout());
        scrollFlow.add(scrollPane);
        
        //set up bottom layout
        bottom.add(scrollFlow, BorderLayout.WEST);
        bottom.add(buttonFlow, BorderLayout.CENTER);
        //bottom.add(Box.createRigidArea(new Dimension(400, 100)));
        
        //adds each section to applet window
        this.add(top);
        this.add(middle);
        this.add(bottom);
        
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
        JmolPanel() 
        {
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
	
	private class RedirectedOut extends OutputStream 
	{			
		private PipedOutputStream out = new PipedOutputStream();
		private Reader reader;
		JTextArea txtArea;
		
		public RedirectedOut(JTextArea txtArea) throws IOException
		{
			PipedInputStream in = new PipedInputStream(out);
			reader = new InputStreamReader(in, "UTF-8");
			this.txtArea = txtArea;
		}
		
		@Override
		public void write(int i) throws IOException 
		{
			out.write(i);
		}
		public void write(byte[] bytes, int i, int i1) throws IOException 
		{
		    out.write(bytes, i, i1);
		}
		
		public void flush() throws IOException
		{
			if(reader.ready())
			{
				char[] chars = new char[1024];
				int n = reader.read(chars);
				String txt = new String(chars, 0, n);
				
				txtArea.append(txt);
			}
		}
	}
	
	private class ButtonListener implements ActionListener 
	{

		public void actionPerformed(ActionEvent e) 
		{
			if (e.getSource() == selectButton || e.getSource() == input)
			{
				currentMolecule = input.getText();
				System.out.println("Current Molecule Set to " +currentMolecule);
				view0.evalString("load \":"+currentMolecule+"\";");
		        view1.evalString("load \":"+currentMolecule+"\";");
				currentMolLabel.setText("Current Molecule: "+currentMolecule);				
			}				
		}		
	}
} 