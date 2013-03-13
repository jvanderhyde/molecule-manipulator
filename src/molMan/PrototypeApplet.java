/*
 * TODO:
 * 	- Add loading animation when loading new molecule.
 * 	- Add more control buttons beneath the JMol windows (Select All/None)
 * 	- Parse input from textbox (make sure that it is a real molecule...). 
 * 	- Fill all the tab functions. 
 * 	- Add Transformation animations
 */

 
package molMan;
//Reference the required Java libraries
import java.applet.Applet; 
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.*;
import molMan.SimpleJmolExample.JmolPanel;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolSyncInterface;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;
import org.jmol.i18n.GT;
import org.jmol.util.Logger;
import org.jmol.util.Parser;
import org.jmol.util.TextFormat;
import org.jmol.viewer.Viewer;

//The applet code  
public class PrototypeApplet extends Applet {

	private static final long serialVersionUID = 1L;	
	private JmolPanel jmolPanel0;
	private JmolPanel jmolPanel1;
    private final int W = 1400;
    private final int H = 800;
    private JScrollPane scrollPane;
    private String currentMolecule = "";
    private JTextField input = new JTextField(30);
    private JLabel currentMolLabel = new JLabel("");
    private JmolViewer view0;
    private JmolViewer view1;
    private JTextArea out;
    private int rotAxisValue = -1; //0=x, 1=y, 2=z, 3=-x, 4=-y, 5=-z, -1=not selected
    private int rotationAmount;
     
    ////////////STATE VARIABLES\\\\\\\\\\\\
    private Boolean rotateOn = false;
    private Boolean inverted = false;
    
    ////////////BUTTONS\\\\\\\\\\\\
    private JButton selectButton = new JButton("Select");
    private JButton rotateButton = new JButton("Rotate On/Off");
    private JButton invertButton = new JButton("Invert");
    private JButton reset0Button = new JButton("Reset");
    private JButton reset1Button = new JButton("Reset");
    private JButton rotButton = new JButton("Rotate");
    private JButton rotInvButton = new JButton("Rotate & Invert");
    private JRadioButton rotAxisX = new JRadioButton("X");
    private JRadioButton rotAxisY = new JRadioButton("Y");
    private JRadioButton rotAxisZ = new JRadioButton("Z");
    private JRadioButton rotAxisNegX = new JRadioButton("-X");
    private JRadioButton rotAxisNegY = new JRadioButton("-Y");
    private JRadioButton rotAxisNegZ = new JRadioButton("-Z");
    private JRadioButton rotAxisX1 = new JRadioButton("X");
    private JRadioButton rotAxisY1 = new JRadioButton("Y");
    private JRadioButton rotAxisZ1 = new JRadioButton("Z");
    private JRadioButton rotAxisNegX1 = new JRadioButton("-X");
    private JRadioButton rotAxisNegY1 = new JRadioButton("-Y");
    private JRadioButton rotAxisNegZ1 = new JRadioButton("-Z");
    private JButton previous = new JButton("Previous");
    private JButton next = new JButton("Next");
    

    protected Map<EnumCallback, String> callbacks = new Hashtable<EnumCallback, String>();
    private String callbackString = new String("Nothing");
    private MyJmolListener jListen0 = new MyJmolListener();
    private MyJmolListener jListen1 = new MyJmolListener();
    private boolean loadingMol0 = false;
    private boolean loadingMol1 = false;
    
	public void init()
	{	
		//Setup the textarea for the system output to go to.
                
		out = new JTextArea(7,30);
		scrollPane = new JScrollPane(out);
		//Make the ScrollPane autoScroll when something is added.
		//Found at : http://www.coderanch.com/t/329964/GUI/java/JScrollpane-Force-autoscroll-bottom
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				//I think that the 1000 here means that it will be able to adjust the scroll
				//  if the output is not more then 1000 lines input at a time...
				out.select(out.getHeight()+1000,0);                                
			}
		});
		
		out.setEditable(false);
		
		/*   I have killed the output for now because I was halving problems with prints from
		 * the applet not showing up...  Not sure if perhaps Jmol is blocking them...
		try //Redirect System.out to run to our output box.
		{
			PrintStream output = new PrintStream(new RedirectedOut(out), true, "UTF-8");
			System.setOut(output);			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		*/     
		
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
        view0.setJmolStatusListener(jListen0);
        view1 = jmolPanel1.getViewer();
        view1.setJmolStatusListener(jListen1);
                      
        loadingMol0 = true;
        view0.evalString("load \":caffeine\";");
        loadingMol1 = true;
        view1.evalString("load \":caffeine\";");
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
		ImageIcon logoImage = new ImageIcon("logo.png", "MolMan");
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
        rot.setLayout(new BoxLayout(rot, BoxLayout.Y_AXIS));
        JPanel rotationButFlow = new JPanel(new FlowLayout()); 
        rotButton.addActionListener(handler);
        rotationButFlow.add(rotButton);
        JLabel rotationTitle = new JLabel("ROTATION");
        rotationTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        ButtonGroup axis = new ButtonGroup();
        axis.add(rotAxisX);
        axis.add(rotAxisY);
        axis.add(rotAxisZ);
        axis.add(rotAxisNegX);
        axis.add(rotAxisNegY);
        axis.add(rotAxisNegZ);
        rotAxisX.addActionListener(handler);
        rotAxisY.addActionListener(handler);
        rotAxisZ.addActionListener(handler);
        rotAxisNegX.addActionListener(handler);
        rotAxisNegY.addActionListener(handler);
        rotAxisNegZ.addActionListener(handler);        
        rot.add(rotationTitle);
        rot.add(rotAxisX);
        rot.add(rotAxisY);
        rot.add(rotAxisZ);
        rot.add(rotAxisNegX);
        rot.add(rotAxisNegY);
        rot.add(rotAxisNegZ);
        rot.add(rotationButFlow);
        
        inv = new JPanel();
        inv.setLayout(new BoxLayout(inv, BoxLayout.Y_AXIS));
        JPanel invButFlow = new JPanel(new FlowLayout()); 
        invertButton.addActionListener(handler);
        invButFlow.add(invertButton);
        JLabel inversionTitle = new JLabel("INVERSION");
        inversionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        JTextArea inversionText = new JTextArea("\nInversion moves all of the atoms of the molecule from" +
        		" their original position (x,y,z), through the center of the molecule, to the" +
        		" opposite position (-x,-y,-z).");
        inversionText.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        inversionText.setLineWrap(true);
        inversionText.setWrapStyleWord(true);
        inversionText.setOpaque(false);
        //inversionText.setPreferredSize(new Dimension(250,100));
        JPanel invTextFlow = new JPanel(new FlowLayout());
        invTextFlow.add(inversionText);
        inv.add(inversionTitle);
        //inv.add(invTextFlow);
        inv.add(invButFlow);
        //inv.add(Box.createRigidArea(new Dimension(1, 500)));
        
        
        rotInv = new JPanel();
        rotInv.setLayout(new BoxLayout(rotInv, BoxLayout.Y_AXIS));
        JPanel rotInvButFlow = new JPanel(new FlowLayout()); 
        rotInvButton.addActionListener(handler);
        rotInvButFlow.add(rotInvButton);
        JLabel rotInvTitle = new JLabel("ROTATION & INVERSION");
        rotInvTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        ButtonGroup axis1 = new ButtonGroup();
        axis1.add(rotAxisX1);
        axis1.add(rotAxisY1);
        axis1.add(rotAxisZ1);
        axis1.add(rotAxisNegX1);
        axis1.add(rotAxisNegY1);
        axis1.add(rotAxisNegZ1);
        rotAxisX1.addActionListener(handler);
        rotAxisY1.addActionListener(handler);
        rotAxisZ1.addActionListener(handler);
        rotAxisNegX1.addActionListener(handler);
        rotAxisNegY1.addActionListener(handler);
        rotAxisNegZ1.addActionListener(handler);        
        rotInv.add(rotInvTitle);
        rotInv.add(rotAxisX1);
        rotInv.add(rotAxisY1);
        rotInv.add(rotAxisZ1);
        rotInv.add(rotAxisNegX1);
        rotInv.add(rotAxisNegY1);
        rotInv.add(rotAxisNegZ1);
        rotInv.add(rotInvButFlow);
        
        res = new JPanel();
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.addTab("Rotation", null, rot, "Rotate the molecule around an axis");
        tabs.addTab("Inversion", null, inv, "Invert the molecule through a plane");
        tabs.addTab("Rot & Inv", null, rotInv, "");
        tabs.addTab("Reflection", null, res, "Reflect the molecule through a plane");
        
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
        previous.addActionListener(handler);
        next.addActionListener(handler);
        //Label
        JLabel molVar = new JLabel("  Molecule Variations  ");
        //text output area
        
        JPanel southCenterBorder = new JPanel();
        southCenterBorder.setLayout(new BorderLayout());
        JPanel buttonFlow = new JPanel();
        buttonFlow.setLayout(new FlowLayout());
        buttonFlow.add(previous);
        buttonFlow.add(molVar);
        buttonFlow.add(next);
        
        JPanel reset0ButFlow = new JPanel(new FlowLayout());
        reset0Button.addActionListener(handler);
        reset0ButFlow.add(reset0Button);
        
        JPanel rotButFlow = new JPanel(new FlowLayout());
        rotateButton.addActionListener(handler);        
        rotButFlow.add(rotateButton);
        
        JPanel reset1ButFlow = new JPanel(new FlowLayout());
        reset1Button.addActionListener(handler);
        reset1ButFlow.add(reset1Button);
        
        JPanel controlButsFlow = new JPanel(new FlowLayout());
        controlButsFlow.add(reset0ButFlow);
        controlButsFlow.add(rotButFlow);
        controlButsFlow.add(reset1ButFlow);
        
        southCenterBorder.add(controlButsFlow, BorderLayout.NORTH);
        southCenterBorder.add(buttonFlow, BorderLayout.SOUTH);
        
        JPanel scrollFlow = new JPanel();
        scrollFlow.setLayout(new FlowLayout());
        //scrollFlow.setLayout(new BoxLayout(scrollFlow, BoxLayout.Y_AXIS));
        //scrollFlow.add(Box.createRigidArea(new Dimension(scrollFlow.getWidth(),200)));
        //scrollFlow.setBounds(scrollFlow.getX(), scrollFlow.getY(), this.W/3-scrollFlow.getX(), this.H-scrollFlow.getY());
        scrollFlow.add(scrollPane);
        
        
        //set up bottom layout
        bottom.add(scrollFlow, BorderLayout.WEST);
        bottom.add(southCenterBorder, BorderLayout.CENTER);
        
        //adds each section to applet window
        this.add(top);
        this.add(middle);
        this.add(bottom);
        
	}
	
	
	//This code found at: http://biojava.org/wiki/BioJava:CookBook:PDB:Jmol
	static class JmolPanel extends JPanel 
	{
        
        private static final long serialVersionUID = -3661941083797644242L;
        //JmolSimpleViewer viewer;
        JmolViewer viewer;
        JmolAdapter adapter;
        JmolPanel() 
        {
            adapter = new SmarterJmolAdapter();
            viewer = JmolViewer.allocateViewer(this, adapter);
        }
 
        public JmolViewer getViewer() 
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
				//Check the evalString method in JMol 
				currentMolecule = input.getText();
				System.out.println("Current Molecule Set to " +currentMolecule);
				view0.evalString("try{load \":"+currentMolecule+"\"}catch(e){prompt \"Molecule "+currentMolecule+" not found.\"}");
		        view1.evalString("try{load \":"+currentMolecule+"\"}catch(e){}");
		        
				//Still need to figure out how to get data from jmol to see if this should be changed or not...
		        //  I think that the function of the 'prompt' command is significant because it is actually having jmol interact with java...
		        while(!view1.getBooleanProperty("__appletReady"))
		        {
		        	System.out.println("Executing...");
		        }//Wait for molecule to load...
		        String urlName = view1.getModelSetPathName();
		        urlName = urlName.substring(55, urlName.length()-19); 
		        //System.out.println(urlName);
		        currentMolecule = urlName;
		        currentMolLabel.setText("Current Molecule: "+currentMolecule);
		        //http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/caffeine/SDF?record_type=3d
			}	
			else if(e.getSource()== rotateButton)
			{
				if(rotateOn)
				{
					view0.evalString("rotate off;");
			        view1.evalString("rotate off;");
			        rotateOn = false;
				}
				else
				{
					view0.evalString("rotate on;");
			        view1.evalString("rotate on;");
			        rotateOn = true;
				}		        
			}
			else if(e.getSource() == invertButton)
			{
				view1.evalString("select all; invertSelected POINT {0,0,0};");
				inverted = !inverted;
			}
			else if(e.getSource() == rotInvButton)
			{
				rotationAmount = 180;
				
				switch (rotAxisValue) 
				{
					case -1:
						JOptionPane.showMessageDialog(null, "Please select an axis to rotate around.");
						break;
					case 0:
						view1.evalString("move "+rotationAmount+" 0 0 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					case 1:
						view1.evalString("move 0 "+rotationAmount+" 0 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					case 2:
						view1.evalString("move 0 0 "+rotationAmount+" 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					case 3:
						view1.evalString("move -"+rotationAmount+" 0 0 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					case 4:
						view1.evalString("move 0 -"+rotationAmount+" 0 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					case 5:
						view1.evalString("move 0 0 -"+rotationAmount+" 0 0 0 0 0 5;");
	                                            view1.evalString("select all; invertSelected POINT {0,0,0};");
	                                            inverted = !inverted;
						break;
					default:
						break;
				}			
			}
            else if(e.getSource() == rotButton)
            {
            	rotationAmount = 180;
				
				switch (rotAxisValue) 
				{
					case -1:
						JOptionPane.showMessageDialog(null, "Please select an axis to rotate around.");
						break;
					case 0:
						view1.evalString("move "+rotationAmount+" 0 0 0 0 0 0 0 5;");
						break;
					case 1:
						view1.evalString("move 0 "+rotationAmount+" 0 0 0 0 0 0 5;");
						break;
					case 2:
						view1.evalString("move 0 0 "+rotationAmount+" 0 0 0 0 0 5;");
						break;
					case 3:
						view1.evalString("move -"+rotationAmount+" 0 0 0 0 0 0 0 5;");
						break;
					case 4:
						view1.evalString("move 0 -"+rotationAmount+" 0 0 0 0 0 0 5;");
						break;
					case 5:
						view1.evalString("move 0 0 -"+rotationAmount+" 0 0 0 0 0 5;");
						break;
					default:
						break;
				}
            }
			else if(e.getSource() == reset0Button) view0.evalString("reset;");
			else if(e.getSource() == reset1Button) 
			{
				view1.evalString("reset;");
				if(inverted) 
				{
					view1.evalString("select all; invertSelected POINT {0,0,0};");
					inverted = !inverted;
				}
				
				//remove any axis that is being drawn.
				view1.evalString("draw axis1 DELETE");
				
			}
			else if((e.getSource() == rotAxisX)||(e.getSource() == rotAxisX1))
            {
                rotAxisValue = 0;
                
                //First delete any drawn axis, then draw the right one...
                view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"x axis\" {4,0,0} {-4,0,0}");
            }
			else if((e.getSource() == rotAxisY)||(e.getSource() == rotAxisY1))
	        {
	            rotAxisValue = 1;
	            
	            //First delete any drawn axis, then draw the right one...
	            view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"y axis\" {0,4,0} {0,-4,0}");
	        }
			else if((e.getSource() == rotAxisZ)||(e.getSource() == rotAxisZ1))
            {
                rotAxisValue = 2;
                
                //First delete any drawn axis, then draw the right one...
	            view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"z axis\" {0,0,4} {0,0,-4}");
            }
			else if((e.getSource() == rotAxisNegX)||(e.getSource() == rotAxisNegX1))
            {
                rotAxisValue = 3;

                //First delete any drawn axis, then draw the right one...
                view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"-x axis\" {-4,0,0} {4,0,0}");
            }
			else if((e.getSource() == rotAxisNegY)||(e.getSource() == rotAxisNegY1))
            {
                rotAxisValue = 4;
                
	            //First delete any drawn axis, then draw the right one...
	            view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"-y axis\" {0,-4,0} {0,4,0}");
            }
			else if((e.getSource() == rotAxisNegZ)||(e.getSource() == rotAxisNegZ1))
            {
                rotAxisValue = 5;

                //First delete any drawn axis, then draw the right one...
	            view1.evalString("draw axis1 DELETE");
                view1.evalString("draw axis1 \"-z axis\" {0,0,-4} {0,0,4}");
            }
			else if(e.getSource() == previous) //Right now I am just using this as a testing grounds for new features.
			{
				///////////Possible Useful Commands\\\\\\\\\\\\\\\
				//view1.getModelSetPathName();  \\Gets the URL for the current molecule at pubchem...
				//view1.isScriptExecuting()  //returns true if no script is executing...
				//view1.getBooleanProperty("__appletReady");
				//view1..getBooleanProperty("ShowAxes");???
				
				
				//draw axis1 DELETE
				//JOptionPane.showMessageDialog(null, callbackString);				
				view1.evalString("echo \"Hello World\"");
				
				//view1.setRotation(javax.vecmath.Matrix3f matrixRotation) //Could we possibly use matricies to move the atoms?
				//view1.getAtomPoint3f(int);
				//view1.getBooleanProperty(String)
				//view1.getData(String atomExpression, String type)
				//view1.setBooleanProperty(java.lang.String propertyName, boolean value)
				//view1.evalString("print \"Stuff\"");
				//view1.evalString("try{load \"garbage\"}catch(e){prompt \"Molecule not found.\"}");
				//view1.evalString("show orientation moveto;");
				//view1.evalString("axes on; axes 4;");
				//draw POLYGON 4 {0 0 0} {1 1 1} {1 2 1} {0 5 0} 2 [0 1 2 6] [0 3 2 6] mesh nofill
			}
		}		
	}
	private class MyJmolListener implements JmolStatusListener
	{

		@Override
		public void notifyCallback(EnumCallback type, Object[] data) 
		{
			String callback = callbacks.get(type);
			String strInfo = (data == null || data[1] == null ? null : data[1]
			          .toString());
			
			switch(type)
			{
				case CLICK:
			        // x, y, action, int[] {action}
			        // the fourth parameter allows an application to change the action
					callbackString = "x=" + data[1] + " y=" + data[2] + " action=" + data[3] + " clickCount=" + data[4];
					//JOptionPane.showMessageDialog(null, callbackString);
					break;
				case ECHO:
					JOptionPane.showMessageDialog(null, strInfo);
					break;
				case APPLETREADY:
					//JOptionPane.showMessageDialog(null, "Applet is Ready");
					break;
				case LOADSTRUCT: //Called when the applet is finished loading the new mol.
					JOptionPane.showMessageDialog(null, "LOADSTRUCT");
					break;
			}
		}

		@Override
		public boolean notifyEnabled(EnumCallback type) 
		{
			switch (type) 
			{
			  case ANIMFRAME:
			  case ECHO:
				  return true;
			  case ERROR:
			  case EVAL:
			  case LOADSTRUCT:
				  return true;
			  case MEASURE:
			  case MESSAGE:
			  case PICK:
			  case SYNC:
			  case SCRIPT:
			    return true;
			  case APPLETREADY:
				  return true;// Jmol 12.1.48
			  case ATOMMOVED:  // Jmol 12.1.48
			  case CLICK:
				  return true;
			  case HOVER:
			  case MINIMIZATION:
			  case RESIZE:
			    break;
			}
		    return (callbacks.get(type) != null);
		}

		@Override
		public void setCallbackFunction(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String createImage(String arg0, String arg1, Object arg2,
				int arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String eval(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public float[][] functionXY(String arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public float[][][] functionXYZ(String arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, Object> getProperty(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, Object> getRegistryInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void resizeInnerPanel(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void showUrl(String arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
} 
