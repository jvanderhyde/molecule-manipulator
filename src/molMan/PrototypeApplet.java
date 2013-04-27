/*
 * TODO:
 * 	- Add loading animation when loading new molecule.
 * 	- Add more control buttons beneath the JMol windows (Select All/None)
 * 	- Add Transformation animations
 * 
 * Rotate around a bond.
 * Get camera rotation out of Jmol
 */

 

package molMan;
//Reference the required Java libraries
import java.applet.Applet; 
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSelectionListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;

//The applet code  
public class PrototypeApplet extends Applet 
{

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
    private Boolean reflected = false;
    
    ////////////BUTTONS\\\\\\\\\\\\
    private JButton selectButton = new JButton("Select");
    private JButton rotateButton = new JButton("Rotate On/Off");
    private JButton invertButton = new JButton("Invert");
    private JButton reset0Button = new JButton("Reset");
    private JButton reset1Button = new JButton("Reset");
    private JButton rotButton = new JButton("Rotate");
    private JButton rotRefButton = new JButton("Rotate & Invert");
    private JButton refButton = new JButton("Reflect");
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
    private JRadioButton refX = new JRadioButton("X");
    private JRadioButton refY = new JRadioButton("Y");
    private JRadioButton refZ = new JRadioButton("Z");
    private JButton previous = new JButton("Previous");
    private JButton next = new JButton("Next");
    private String[] rotRefString = {"Rotation & Reflection",
            "s1: 360̊", "s2: 180̊", "s3: 120̊", "s4: 90̊", "s5: 72̊", "s6: 60̊",
            "s7: 51̊", "s8: 45̊", "s9: 40̊", "s10: 36̊"};
    private JComboBox rotRefBox = new JComboBox(rotRefString);
    private String[] rotString = {"Rotations","c1: 360̊", "c2: 180̊", "c3: 120̊",
        "c4: 90̊", "c5: 72̊", "c6: 60̊", "c7: 51̊", "c8: 45̊", "c9: 40̊", "c10: 36̊"};
    private JComboBox rotBox = new JComboBox(rotString);
    
    private JSlider zAxisSlider;
    private JSlider xAxisSlider;
    private JSlider yAxisSlider;
    private MyChangeListener sliderListener = new MyChangeListener();
    private JTextField zAxisField = new JTextField("0", 2);
    private JTextField xAxisField = new JTextField("0", 2);
    private JTextField yAxisField = new JTextField("0", 2);
    private JLabel zAxisLabel = new JLabel("Z Axis Rotation: ");
    private JLabel xAxisLabel = new JLabel("X Axis Rotation: ");
    private JLabel yAxisLabel = new JLabel("Y Axis Rotation: ");
    //These vectors are both halves of the real axis...
    private float axisRadius = 5.539443f;  
	private Vector3 axisEnd0 = new Vector3(axisRadius, 0, 0);
	private Vector3 axisEnd1 = new Vector3(-axisRadius,0,0);
	private int zAxisRot = 0;
	private int xAxisRot = 0;
	private int yAxisRot = 0;
	private JCheckBox showAxis = new JCheckBox("Show Axis");
	private JCheckBox showPlane = new JCheckBox("Show Plane");
	private boolean axisShown = false;
	private boolean planeShown = false;

    protected Map<EnumCallback, String> callbacks = new Hashtable<EnumCallback, String>();
    @SuppressWarnings("unused")
	private String callbackString = new String("Nothing");
    private MyJmolListener jListen0 = new MyJmolListener();
    private MyJmolListener jListen1 = new MyJmolListener();
    private boolean loadingMol0 = false;//These are both used to be able to tell when both Jmol windows are finished loading.
    @SuppressWarnings("unused")
	private boolean loadingMol1 = false;
    private String refPlane;
    private MySelectionListener selectionListen0 = new MySelectionListener();
    
    
    
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
		
		/*
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
        view1.addSelectionListener(selectionListen0);
        //perspCheck = new PerspectiveChecker(view0, view1, jListen1);
                      
        loadingMol0 = true;
        loadingMol1 = true;
        
        //Load up the initial molecules...
        view0.evalString("load \":caffeine\";");        
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
        currentMolLabel = new JLabel("Current Molecule: ");
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
        JPanel rot, inv, rotRef, ref;
        
        //ROTATE TAB\\
        rot = new JPanel();
        rot.setLayout(new BoxLayout(rot, BoxLayout.Y_AXIS));
        JPanel rotationButFlow = new JPanel(new FlowLayout()); 
        rotButton.addActionListener(handler);
        rotationButFlow.add(rotButton);
        JLabel rotationTitle = new JLabel("ROTATION");
        rotationTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        @SuppressWarnings({ "unchecked", "rawtypes" })
        JPanel rotBoxPan = new JPanel(new FlowLayout());
        rotBox.setSelectedIndex(0);
        rotBoxPan.add(rotBox);
        
        rot.add(rotBoxPan);
        rot.add(showAxis);
        showAxis.addActionListener(handler);
        
        zAxisSlider = new JSlider(JSlider.HORIZONTAL, -90, 90,0);
        zAxisSlider.addChangeListener(sliderListener);  
        xAxisSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        xAxisSlider.addChangeListener(sliderListener);
        
        yAxisSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        yAxisSlider.addChangeListener(sliderListener);
      
        
        
        JPanel zAxisRotFlow = new JPanel(new FlowLayout());
        zAxisRotFlow.add(zAxisLabel);
        zAxisRotFlow.add(zAxisField);
        zAxisField.addActionListener(handler);
        rot.add(zAxisRotFlow);
        rot.add(zAxisSlider);
        
        JPanel xAxisRotFlow = new JPanel(new FlowLayout());
        xAxisRotFlow.add(xAxisLabel);
        xAxisRotFlow.add(xAxisField);
        xAxisField.addActionListener(handler);
        rot.add(xAxisRotFlow);
        rot.add(xAxisSlider);
        
        JPanel yAxisRotFlow = new JPanel(new FlowLayout());
        yAxisRotFlow.add(yAxisLabel);
        yAxisRotFlow.add(yAxisField);
        yAxisField.addActionListener(handler);
        rot.add(yAxisRotFlow);
        rot.add(yAxisSlider);
        
        rot.add(rotationButFlow);
           
        //INVERT Tab\\
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
        
        //ROTATE and REFLECT Tab\\
        rotRef = new JPanel();
        rotRef.setLayout(new BoxLayout(rotRef, BoxLayout.Y_AXIS));
        JPanel rotRefButFlow = new JPanel(new FlowLayout()); 
        rotRefButton.addActionListener(handler);
        rotRefButFlow.add(rotRefButton);
        JLabel rotRefTitle = new JLabel("ROTATION & INVERSION");
        rotRefTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
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
        
        @SuppressWarnings({ "rawtypes", "unchecked" })	
        JPanel rotRefBoxPan = new JPanel(new FlowLayout());
        rotRefBox.setSelectedIndex(0);
        rotRefBoxPan.add(rotRefBox);
        
        
        rotRef.add(rotRefBoxPan);
        rotRef.add(rotRefTitle);
        rotRef.add(rotAxisX1);
        rotRef.add(rotAxisY1);
        rotRef.add(rotAxisZ1);
        rotRef.add(rotAxisNegX1);
        rotRef.add(rotAxisNegY1);
        rotRef.add(rotAxisNegZ1);
        rotRef.add(rotRefButFlow);
        
        //REFLECT Tab\\
        ref = new JPanel();
        ref.setLayout(new BoxLayout(ref, BoxLayout.Y_AXIS));
        JPanel reflectionButFlow = new JPanel(new FlowLayout()); 
        refButton.addActionListener(handler);
        reflectionButFlow.add(refButton);
        JLabel reflectionTitle = new JLabel("REFLECTION");
        reflectionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        showPlane.addActionListener(handler);
        
        ref.add(reflectionTitle);
        ref.add(showPlane);
        ref.add(reflectionButFlow);
        /*
        ButtonGroup axis2 = new ButtonGroup();
        axis2.add(refX);
        axis2.add(refY);
        axis2.add(refZ);
        refX.addActionListener(handler);
        refY.addActionListener(handler);
        refZ.addActionListener(handler);  
        ref.add(refX);
        ref.add(refY);
        ref.add(refZ);
        */
        
        
        
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.addTab("Rotation", null, rot, "Rotate the molecule around an axis");
        tabs.addTab("Inversion", null, inv, "Invert the molecule through a plane");
        tabs.addTab("Reflection", null, ref, "Reflect the molecule through a plane");
        tabs.addTab("Rot & Ref", null, rotRef, "");
        
        
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
        scrollFlow.setLayout(new BoxLayout(scrollFlow, BoxLayout.Y_AXIS));
        //scrollFlow.add(Box.createRigidArea(new Dimension(scrollFlow.getWidth(),200)));
        scrollFlow.setBounds(scrollFlow.getX(), scrollFlow.getY(), this.W/3-scrollFlow.getX(), this.H-scrollFlow.getY());
        scrollFlow.add(scrollPane);
        
        
        //set up bottom layout
        bottom.add(scrollFlow, BorderLayout.WEST);
        bottom.add(southCenterBorder, BorderLayout.CENTER);
        
        //adds each section to applet window
        this.add(top);
        this.add(middle);
        this.add(bottom);
        
	}
	
	//Jmol calls this when it is done loading the molecule so that the molecule name can be updated.
	public void changeCurrentMolLabel()
	{
		String urlName0 = view0.getModelSetPathName();
		String urlName1 = view1.getModelSetPathName();
		
		while(!urlName0.equals(urlName1)) //Keep reloading the names until you have the same one
		{
			urlName0 = view0.getModelSetPathName();
			urlName1 = view1.getModelSetPathName();
		}
		
        //http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/caffeine/SDF?record_type=3d
        urlName0 = urlName0.substring(55, urlName0.length()-19); 

        currentMolecule = urlName0;
        currentMolLabel.setText("Current Molecule: "+currentMolecule);
        
        resetAxisSize();
	}
	
	
	//This will check the size of the current molecule and adjust the vectors to match.
	public void resetAxisSize()
	{
		axisRadius = view1.getRotationRadius();
		
		Vector3 orig = new Vector3(axisRadius, 0, 0);
		
		Matrix3x3 preliminaryRot = Matrix3x3.rotationMatrix(-45);
		
		orig = preliminaryRot.transform(orig);
		
		double x = orig.x;
		double y = 0;
		double z = orig.y;

		
		axisEnd0 = new Vector3(orig.x, 0, orig.y);
		axisEnd1 = new Vector3(-orig.x,0,-orig.y);
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
			//Called if the SELECT button is hit and you try to load a molecule.
			if (e.getSource() == selectButton || e.getSource() == input)
			{
				currentMolecule = input.getText();
				
				//Check the evalString method in JMol 
				loadingMol0=true;
				loadingMol1=false;
				view0.evalString(
					"try{" + //If the molecule fails to load an error dialogue box pops up...
							"load \":"+currentMolecule+"\"" +
					"}catch(e){prompt \"Molecule "+currentMolecule+" not found.\"}");
		        view1.evalString("try{load \":"+currentMolecule+"\"}catch(e){}");      
			}
			
			//Called if the ROTATE On/Off button is hit to spin both molecules.
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
			
			//Called if the INVERT button is hit.
			else if(e.getSource() == invertButton)
			{
				//Get the total number of atoms so we know what to loop through.
				int numAtoms = view1.getAtomCount();
				System.out.println("Atom Count = "+numAtoms);
									
				//This just sends one big segment of commands to jmol and lets it evaluate it...
				view1.evalString(
					//"set echo top left;"+
					//"echo \"Inverting...\";"+
					
					//We need arrays to store the original xyz locations and the change in x,y, and z
					//Arrays for X
					"origX = ["+numAtoms+"];"+
					"xChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origX[@i] = {atomno = i}.x;"+
						"xChange[@i] = {atomno = i}.x / 100;"+
					"}"+
					
					//Arrays for Y
					"origY = ["+numAtoms+"];"+
					"yChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origY[@i] = {atomno = i}.y;"+
						"yChange[@i] = {atomno = i}.y / 100;"+
					"}"+
					
					//Arrays for Z
					"origZ = ["+numAtoms+"];"+
					"zChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origZ[@i] = {atomno = i}.z;"+
						"zChange[@i] = {atomno = i}.z / 100;"+
					"}"+
					
					
					//Basically animate the inversion.
					"for(j=0; j<200; j++)"+ //j is the number of frames
					"{"+
						"for(i=1; i<"+numAtoms+"+1; i++)" +
						"{" +
							//Select the next atom
							"select none;" +
							"select (*)[i];" +
																			
							//Get the position change that will have to be made each iteration							
							"newXpos = -xChange[@i];"+
							"newYpos = -yChange[@i];"+
							"newZpos = -zChange[@i];"+
						
							"translateSelected {@newXpos, @newYpos, @newZpos};"+
						"}"+
						"delay 0.025;"+  //Controls the fps (Essentially the time between frames)
					"}"
					//"echo \"\";"	
				);
								
				inverted = !inverted;
			}
			
			//Called if the ROTATE and REFLECT button is hit.
			else if(e.getSource() == rotRefButton)
			{
                                rotationAmount = 0;
                                switch(rotRefBox.getSelectedIndex())
                                {
                                    case 0:
                                            JOptionPane.showMessageDialog(null, "Please select a rotation.");
                                            break;
                                    case 1:
                                            rotationAmount = 360;
                                            break;
                                    case 2:
                                            rotationAmount = 180;
                                            break;
                                    case 3:
                                            rotationAmount = 120;
                                            break;
                                    case 4:
                                            rotationAmount = 90;
                                            break;
                                    case 5:
                                            rotationAmount = 72;
                                            break;
                                    case 6:
                                            rotationAmount = 60;
                                            break;
                                    case 7:
                                            rotationAmount = 51;
                                            break;
                                    case 8:
                                            rotationAmount = 45;
                                            break;
                                    case 9:
                                            rotationAmount = 40;
                                            break;
                                    case 10:
                                            rotationAmount = 36;
                                            break;
                                }

				
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
			
			//Called if the ROTATE Button is hit
            else if(e.getSource() == rotButton)
            {
            	//TODO change this so that it accepts from Nathan's drop-down
            	rotationAmount = 0;
                        switch(rotBox.getSelectedIndex())
                        {
                            case 0:
                                    JOptionPane.showMessageDialog(null, "Please select a rotation.");
                                    break;
                            case 1:
                                    rotationAmount = 360;
                                    break;
                            case 2:
                                    rotationAmount = 180;
                                    break;
                            case 3:
                                    rotationAmount = 120;
                                    break;
                            case 4:
                                    rotationAmount = 90;
                                    break;
                            case 5:
                                    rotationAmount = 72;
                                    break;
                            case 6:
                                    rotationAmount = 60;
                                    break;
                            case 7:
                                    rotationAmount = 51;
                                    break;
                            case 8:
                                    rotationAmount = 45;
                                    break;
                            case 9:
                                    rotationAmount = 40;
                                    break;
                            case 10:
                                    rotationAmount = 36;
                                    break;
                        }
                        
						           	
            	view1.evalString(
            		"select all;" +
            		"rotateSelected $axis1 "+ rotationAmount+" 30;");//30 is the angles/sec of the rotation
      
            }
			
			//Called if the REFLECT Button is hit.
            else if(e.getSource() == refButton)
            {
            	
            	//First lets get the angle that we will have to rotate to in order to move all the atoms 
            	// Axis0{x,y,x} needs to rotate to {0, sqrt(x^2 + y^2 + z^2), 0).
            	Vector2 v0 =  new Vector2(axisEnd0.x, axisEnd0.y);
            	Vector2 v1 = new Vector2(0, Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y
            			+ axisEnd0.z*axisEnd0.z));
            	
            	//Normalize both vectors.
            	v0 = v0.normalized();
            	v1 = v1.normalized();
            	
            	double xyAngle = Math.toDegrees(Math.acos(v0.dot(v1)));
            	
            	//Repeat for the yz plane
            	v0 =  new Vector2(axisEnd0.y, axisEnd0.z);
            	v1 = new Vector2(Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y
            			+ axisEnd0.z*axisEnd0.z),0);
            	
            	//Normalize both vectors.
            	v0 = v0.normalized();
            	v1 = v1.normalized();
            	
            	double yzAngle = Math.toDegrees(Math.acos(v0.dot(v1)));
            	//Now that we have the angles, we know that we need to rotate around the
            	//  z axis by xyAngle degrees and the x axis by the yzAngle
            	
            	//Get the total number of atoms so we know what to loop through.
				int numAtoms = view1.getAtomCount();
				System.out.println("Atom Count = "+numAtoms);
									
				//This just sends one big segment of commands to jmol and lets it evaluate it...
				view1.evalString(
					//"set echo top left;"+
					//"echo \"Inverting...\";"+
					
					//We need arrays to store the original xyz locations and the change in x,y, and z
					//Arrays for X
					"origX = ["+numAtoms+"];"+
					"xChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origX[@i] = {atomno = i}.x;"+
						"xChange[@i] = {atomno = i}.x / 100;"+
					"}"+
					
					//Arrays for Y
					"origY = ["+numAtoms+"];"+
					"yChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origY[@i] = {atomno = i}.y;"+
						"yChange[@i] = {atomno = i}.y / 100;"+
					"}"+
					
					//Arrays for Z
					"origZ = ["+numAtoms+"];"+
					"zChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origZ[@i] = {atomno = i}.z;"+
						"zChange[@i] = {atomno = i}.z / 100;"+
					"}"+
					
					
					//Basically animate the inversion.
					"for(j=0; j<200; j++)"+ //j is the number of frames
					"{"+
						"select all;" +
						"rotateSelected " +
						"for(i=1; i<"+numAtoms+"+1; i++)" +
						"{" +
							//Select the next atom
							"select none;" +
							"select (*)[i];" +
																			
							//Get the position change that will have to be made each iteration							
							"newXpos = -xChange[@i];"+
							"newYpos = -yChange[@i];"+
							"newZpos = -zChange[@i];"+
						
							"translateSelected {@newXpos, @newYpos, @newZpos};"+
						"}"+
						"delay 0.025;"+  //Controls the fps (Essentially the time between frames)
					"}"
					//"echo \"\";"	
				);
            	
            	
            	/*
                view1.evalString("select all; invertSelected PLANE \""+refPlane+"\";");
                */
                reflected = !reflected;
            }
			
			//Called if the RESET button is hit for the left Jmol screen
			else if(e.getSource() == reset0Button)
            {
                view0.evalString("reset;");
            }
			
			//Called if the RESET button is hit for the right Jmol screen.
			else if(e.getSource() == reset1Button) 
			{
                view1.evalString("reset;");  //Resets the view.
                
                //Check to see if there have been any transformations...  if so make undo them
				if(inverted && reflected) 
				{
					view1.evalString("select all; invertSelected POINT {0,0,0};");
                                        view1.evalString("select all; invertSelected PLANE \""+refPlane+"\";");
					inverted = !inverted;
                    reflected = !reflected;
                }
                else if(inverted && !reflected)
                {
                    view1.evalString("select all; invertSelected POINT {0,0,0};");
                    inverted = !inverted;
                }
                else if (reflected && !inverted)
                {
                    view1.evalString("select all; invertSelected PLANE \""+refPlane+"\";");
                    reflected = !reflected;
                }
                
				
				//Reset the sliders to 0
				xAxisSlider.setValue(0);
				yAxisSlider.setValue(0);
				zAxisSlider.setValue(0);
				
				//remove any axis that is being drawn.
				view1.evalString("draw axis1 DELETE");
				if(axisShown)
				{
					showAxis.setSelected(false);
					axisShown = false;
				}
				
				
			}
			
			//Called if 
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
            else if(e.getSource() == refX)
            {
                refPlane = "yz";
            }
            else if(e.getSource() == refY)
            {
                refPlane = "xz";
            }
            else if(e.getSource() == refZ)
            {
                refPlane = "xy";
            }
            else if(e.getSource() == zAxisField)
            {
            	zAxisSlider.setValue(Integer.parseInt(zAxisField.getText()));
            }
            else if(e.getSource() == xAxisField)
            {
            	xAxisSlider.setValue(Integer.parseInt(xAxisField.getText()));
            }
            else if(e.getSource() == yAxisField)
            {
            	yAxisSlider.setValue(Integer.parseInt(yAxisField.getText()));
            }
            else if(e.getSource() == showAxis)
            {
            	axisShown = !axisShown;
            	if(axisShown == true) view1.evalString("draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
            	else view1.evalString("draw axis1 DELETE");
            }
            else if(e.getSource() == showPlane)
            {
            	planeShown = !planeShown;
            	if(planeShown == true) view1.evalString(
			    		"draw circle {0,0,0} {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"} "+
	    				"SCALE 1000;");
            	else view1.evalString("draw circle DELETE");
            }
            else if(e.getSource() == next)
            {
            	//view1.evalString("show STATE;");
            }
            else if(e.getSource() == previous) //Right now I am just using this as a testing grounds for new features.
			{
            	
            	//Get the total number of atoms so we know what to loop through.
				int numAtoms = view1.getAtomCount();
				System.out.println("Atom Count = "+numAtoms);
									
				//This just sends one big segment of commands to jmol and lets it evaluate it...
				view1.evalString(
					//"set echo top left;"+
					//"echo \"Inverting...\";"+
					
					//We need arrays to store the original xyz locations and the change in x,y, and z
					//Arrays for X
					"origX = ["+numAtoms+"];"+
					"xChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origX[@i] = {atomno = i}.x;"+
						"xChange[@i] = {atomno = i}.x / 100;"+
					"}"+
					
					//Arrays for Y
					"origY = ["+numAtoms+"];"+
					"yChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origY[@i] = {atomno = i}.y;"+
						"yChange[@i] = {atomno = i}.y / 100;"+
					"}"+
					
					//Arrays for Z
					"origZ = ["+numAtoms+"];"+
					"zChange = ["+numAtoms+"];"+
					"for(i=1; i<"+numAtoms+"+1; i++)" +
					"{"+
						"origZ[@i] = {atomno = i}.z;"+
						"zChange[@i] = {atomno = i}.z / 100;"+
					"}"+
					
					
					//Basically animate the inversion.
					"for(j=0; j<200; j++)"+ //j is the number of frames
					"{"+
						"for(i=1; i<"+numAtoms+"+1; i++)" +
						"{" +
							//Select the next atom
							"select none;" +
							"select (*)[i];" +
																			
							//Get the position change that will have to be made each iteration							
							"newXpos = 0;"+
							"newYpos = -yChange[@i];"+
							"newZpos = 0;"+
						
							"translateSelected {@newXpos, @newYpos, @newZpos};"+
						"}"+
						"delay 0.025;"+  //Controls the fps (Essentially the time between frames)
					"}"
					//"echo \"\";"	
				);
            	
            	
            	
            	
            	
            	
            	/*
            	Vector3 A = Vector3.cross(axisEnd0, new Vector3(0,1,0));
            	double sinAngle = Math.sqrt(A.x*A.x +A.y*A.y + A.z*A.z)/
            			(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y + axisEnd0.z*axisEnd0.z);
            	
            	double angle = Math.toDegrees(Math.asin(sinAngle));
            	
            	view1.evalString(
            			"rotate {"+A.x+","+A.y+","+A.z+"} {0,0,0} "+angle+" 30");
            	*/
            	
            	
            	
            	
            	
            	
            	
            	/*
            	double xyLength = Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y);
            	double zAngle = Math.toDegrees(Math.acos(axisEnd0.y / xyLength));
            	
            	double vecLength = Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y + 
            			axisEnd0.z*axisEnd0.z);
            	double xAngle = Math.toDegrees(Math.acos(xyLength / vecLength));
            	
            	
            	//I think we are getting some probs because some of the rotations need to be negative
            	if(axisEnd0.x < 0 && axisEnd0.z < 0)
            	{
            		zAngle = -zAngle;
            		xAngle = -xAngle;
            	}
            	else if(axisEnd0.x > 0) xAngle = -xAngle;
            	else if(axisEnd0.z > 0) zAngle = -zAngle;
            	

            	System.out.println("x = "+axisEnd0.x+" y = "+ axisEnd0.y+" z = "+axisEnd0.z);
            	System.out.println("xyLength = "+xyLength+" vecLength = "+vecLength);
            	System.out.println("zAngle = "+zAngle+" xAngle = "+xAngle);
            	
            	view1.evalString(
            			"select all;" +
            			"move "+ xAngle+" 0 0 0 0 0 0 0 3;" +
            			"move 0 0 "+ zAngle+" 0 0 0 0 0 3;");
            	
            	
            	//First lets get the angle that we will have to rotate to in order to move all the atoms 
            	// Axis0{x,y,x} needs to rotate to {0, sqrt(x^2 + y^2 + z^2), 0).
            	Vector2 v0 =  new Vector2(axisEnd0.x, axisEnd0.y);
            	Vector2 v1 = new Vector2(0, Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y
            			+ axisEnd0.z*axisEnd0.z));
            	
            	//Normalize both vectors.
            	v0 = v0.normalized();
            	v1 = v1.normalized();
            	
            	double xyAngle = Math.toDegrees(Math.atan2(v1.y, v1.x) - Math.atan2(v0.y, v0.x));
            	
            	//Repeat for the zy plane
            	v0 =  new Vector2(axisEnd0.z, axisEnd0.y);
            	v1 = new Vector2(0, Math.sqrt(axisEnd0.x*axisEnd0.x + axisEnd0.y*axisEnd0.y
            			+ axisEnd0.z*axisEnd0.z));
            	
            	//Normalize both vectors.
            	v0 = v0.normalized();
            	v1 = v1.normalized();
            	
            	double zyAngle = Math.toDegrees(Math.atan2(v1.y, v1.x) - Math.atan2(v0.y, v0.x));
            	//Now that we have the angles, we know that we need to rotate around the
            	//  z axis by xyAngle degrees and the x axis by the yzAngle
            	
            	System.out.println("xyAngle = "+ xyAngle+", yzAngle = "+zyAngle);
            	
            	view1.evalString(
            			"select all;" +
            			"rotateSelected z "+ xyAngle+";"+
            			"delay 1;" +
            			"rotateSelected x "+ -zyAngle+";");
            	*/

				///////////Possible Useful Commands\\\\\\\\\\\\\\\
				//view1.getModelSetPathName();  \\Gets the URL for the current molecule at pubchem...
				//view1.isScriptExecuting()  //returns true if no script is executing...
				//view1.getBooleanProperty("__appletReady");
				//view1..getBooleanProperty("ShowAxes");???
				
				
				//draw axis1 DELETE
				//JOptionPane.showMessageDialog(null, callbackString);				
				//view1.evalString("echo \"Hello World\"");
				
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
	
	public class MyJmolListener implements JmolStatusListener
	{
		public String echoText = "";
		
		@Override
		public void notifyCallback(EnumCallback type, Object[] data) 
		{
			@SuppressWarnings("unused")
			String callback = callbacks.get(type);
			String strInfo = (data == null || data[1] == null ? null : data[1]
			          .toString());
			
			switch(type)
			{
				case ATOMMOVED:
					//System.out.println("Atom Moved...");
					break;
				case MESSAGE:
					//System.out.println(strInfo);
					
					break;
				case CLICK:
			        // x, y, action, int[] {action}
			        // the fourth parameter allows an application to change the action
					callbackString = "x=" + data[1] + " y=" + data[2] + " action=" + data[3] + " clickCount=" + data[4];
					
					
					//This is where we handle the linking of perspectives for the jmol windows
					//Every time there is a mouse event with view1 this will run.
					view1.evalString("show STATE;");  //Get the current perspective
					
					if(echoText != "")  //Make sure the String has been updated
					{
						//First we need to get the exact command we need out of the state String.
						String stateCommand = echoText.substring(
								echoText.indexOf("function _setPerspectiveState()"), 
								echoText.indexOf("function _setSelectionState()")-1);
						stateCommand = stateCommand.substring(
								stateCommand.indexOf("moveto 0.0"), 
								stateCommand.indexOf("slab")); 

						//then we just feed that command into view0
						view0.evalString(stateCommand);
						
						//We also need to adjust the location of axis1
						//Its rotation should be opposite to that of the molecule.  
						//This method of vector rotation from http://www.blitzbasic.com/Community/posts.php?topic=57616
						
						//We need to pull the axis coords out of the String
						//To do this, we will use a scanner, but first we have to get the
						//  } out of the way because for some reason it screws things up.
						Scanner sc = new Scanner(stateCommand).useDelimiter("}");
				    	String temp = "";
				    	while(sc.hasNext()){ temp+= sc.next();}
				    	   
				    	//Now we can scan the text for the values we need
				    	sc = new Scanner(temp);
				    	int counter = 0;
				    	double[] vals = new double[5];
				    	while(sc.hasNext() && counter<5)
				    	{
				    		if(sc.hasNextDouble())
				    		{
				    			vals[counter] = sc.nextDouble();
				    			counter++;
				    		}
				    		else sc.next();				    		
				    	}
					   	
				    	//Store the axis values and the angle of rotation.
				    	// u,v,w is the axis of rotation and a is the rotation ammount in degrees
						double u = vals[1];
						double v = vals[2];
						double w = vals[3];
						double a = vals[4];
						
						//Sets the original location of the drawn axis.
						//TODO fix this so the axis length is variable.
						//We cannot just set these values, because then we cannot control the length of the
						//  axis depending on the size of the molecule.  We will load the axis as usual and
						//  then rotate it where we want it.  
						
						Vector3 orig = new Vector3(axisRadius, 0, 0);
						
						Matrix3x3 preliminaryRot = Matrix3x3.rotationMatrix(-45);
						
						orig = preliminaryRot.transform(orig);
						
						double x = orig.x;
						double y = 0;
						double z = orig.y;
						
						//Create the matrix to multiply our axis vector by.
						RotationMatrix rotMat = new RotationMatrix(0, 0, 0, u, v, w, Math.toRadians(-a));
						
						//do the matrix multiplication/
						double[] rotVector = rotMat.timesXYZ(x, y, z);
																	
						axisEnd0.x = rotVector[0];
						axisEnd0.y = rotVector[1];
						axisEnd0.z = rotVector[2];	
						axisEnd1.x = -rotVector[0];
						axisEnd1.y = -rotVector[1];
						axisEnd1.z = -rotVector[2];	
						
						if(axisShown)
						{
							view1.evalString(
						    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
							    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
						}
						if(planeShown)
						{
							view1.evalString(
						    		"draw circle {0,0,0} {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"} "+
						    				"SCALE 1000;");
						}
						
					}
					
					break; 
				case ECHO:
					
					echoText = strInfo;

					//JOptionPane.showMessageDialog(null, strInfo);
					break;
				case APPLETREADY:
					//JOptionPane.showMessageDialog(null, "Applet is Ready");
					break;
				case LOADSTRUCT: //Called when the applet is finished loading the new mol.

					//When this case has been hit twice, then it will set both of the
					//  loading booleans to false.  This is when you know they are both
					//  done loading...
			
					//if it has been called once, then set the second one false
					if(!loadingMol0)
					{
						loadingMol1 = false;
						changeCurrentMolLabel();
					}
					else loadingMol0 = false;
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
				  return true;
			  case PICK:
			  case SYNC:
			  case SCRIPT:
			    return true;
			  case APPLETREADY:
				  return true;// Jmol 12.1.48
			  case ATOMMOVED:  // Jmol 12.1.48
				  return true;
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
		public void setCallbackFunction(String arg0, String arg1) {	}

		@Override
		public String createImage(String arg0, String arg1, Object arg2, int arg3) 
		{
			return null;
		}

		@Override
		public String eval(String arg0) 
		{
			return null;
		}

		@Override
		public float[][] functionXY(String arg0, int arg1, int arg2) 
		{
			return null;
		}

		@Override
		public float[][][] functionXYZ(String arg0, int arg1, int arg2, int arg3) 
		{
			return null;
		}

		@Override
		public Map<String, Object> getProperty(String arg0) 
		{
			return null;
		}

		@Override
		public Map<String, Object> getRegistryInfo() 
		{
			return null;
		}

		@Override
		public void resizeInnerPanel(String arg0) {}

		@Override
		public void showUrl(String arg0) {}	
	}
	
		
	
	private class MySelectionListener implements JmolSelectionListener
	{
		@Override
		public void selectionChanged(BitSet arg0) 
		{
			//System.out.println("Selection Changed");
			
		}
		
	}
	
	/*
	 * This class listens to the sliders (for the planes and axes)  and will adjust things 
	 *   as you move the slider bars.
	 */
	private class MyChangeListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e) 
		{
		    //the XAXISSLIDER is being adjusted.
		    if(e.getSource() == zAxisSlider)
		    {
		    	showAxis.setSelected(true);
		    	axisShown=true;
		    	
		    	int zValue = zAxisSlider.getValue();
		    	zAxisField.setText(""+zValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	zValue = zValue - zAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	zAxisRot = zAxisSlider.getValue();
		    	
		    	//view1.evalString(
		    	//	"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
		    	//		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	
		    	//Create a rotation matrix for the current rotation
		    	Matrix3x3 rotMatrix0 = Matrix3x3.rotationMatrix(zValue);
		    	Matrix3x3 rotMatrix1 = Matrix3x3.rotationMatrix(zValue);
		    	
		    	//Apply the rotation to the axis end.
		    	axisEnd0 = rotMatrix0.transform(axisEnd0);
		    	axisEnd1 = rotMatrix1.transform(axisEnd1);
		    	
		    	//Redraw the new Axis.
		    	view1.evalString(
		    		//"draw axis1 DELETE;" + //Erase old axis and then draw new one.
		    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	
		    }
		    else if(e.getSource() == xAxisSlider)
		    {
		    	showAxis.setSelected(true);
		    	axisShown=true;
		    	
		    	int xValue = xAxisSlider.getValue();
		    	xAxisField.setText(""+xValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	xValue = xValue - xAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	xAxisRot = xAxisSlider.getValue();
		    	
		    			    	
		    	//Create a rotation matrix for the current rotation
		    	Matrix3x3 rotMatrix0 = Matrix3x3.rotationMatrix(xValue);
		    	Matrix3x3 rotMatrix1 = Matrix3x3.rotationMatrix(xValue);
		    	
		    	//To rotate around a different axis, we need to have two new vectors that will match
		    	//  the rotation axis...
		    	Vector3 tempAxis0 = new Vector3(axisEnd0.z, axisEnd0.y, axisEnd0.x);
		    	Vector3 tempAxis1 = new Vector3(axisEnd1.z, axisEnd1.y, axisEnd1.x);
		    	
		    	//Apply the rotation to the axis end.
		    	tempAxis0 = rotMatrix0.transform(tempAxis0);
		    	tempAxis1 = rotMatrix1.transform(tempAxis1);
		    	
		    	//Save the new axis back to the right place.
		    	axisEnd0 = new Vector3(tempAxis0.z, tempAxis0.y, tempAxis0.x);
		    	axisEnd1 = new Vector3(tempAxis1.z, tempAxis1.y, tempAxis1.x);
		    	
		    	//Redraw the new Axis.
		    	view1.evalString(
		    		//"draw axis1 DELETE;" + //Erase old axis and then draw new one.
		    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	
		    }
		    else if(e.getSource() == yAxisSlider)
		    {
		    	showAxis.setSelected(true);
		    	axisShown=true;
		    	
		    	int yValue = yAxisSlider.getValue();
		    	yAxisField.setText(""+yValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	yValue = yValue - yAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	yAxisRot = yAxisSlider.getValue();
		    	
		    			    	
		    	//Create a rotation matrix for the current rotation
		    	Matrix3x3 rotMatrix0 = Matrix3x3.rotationMatrix(yValue);
		    	Matrix3x3 rotMatrix1 = Matrix3x3.rotationMatrix(yValue);
		    	
		    	//To rotate around a different axis, we need to have two new vectors that will match
		    	//  the rotation axis...
		    	Vector3 tempAxis0 = new Vector3(axisEnd0.x, axisEnd0.z, axisEnd0.y);
		    	Vector3 tempAxis1 = new Vector3(axisEnd1.x, axisEnd1.z, axisEnd1.y);
		    	
		    	//Apply the rotation to the axis end.
		    	tempAxis0 = rotMatrix0.transform(tempAxis0);
		    	tempAxis1 = rotMatrix1.transform(tempAxis1);
		    	
		    	//Save the new axis back to the right place.
		    	axisEnd0 = new Vector3(tempAxis0.x, tempAxis0.z, tempAxis0.y);
		    	axisEnd1 = new Vector3(tempAxis1.x, tempAxis1.z, tempAxis1.y);
		    	
		    	//Redraw the new Axis.
		    	view1.evalString(
		    		//"draw axis1 DELETE;" + //Erase old axis and then draw new one.
		    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	
		    }

		    /*
		    if (!source.getValueIsAdjusting()) 
		    {
		        
		    } 
		    else 
		    {
		    	
		    }
		    */
		 }
	}
} 