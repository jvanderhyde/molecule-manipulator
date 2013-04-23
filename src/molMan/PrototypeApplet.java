//TODO fix axis and double plane bug...

package molMan;
//Reference the required Java libraries
import java.applet.Applet; 
import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;

//The applet code  
public class PrototypeApplet extends Applet 
{
    ////////////GUI Components\\\\\\\\\\\\
	private static final long serialVersionUID = 1L;	
	private JmolPanel jmolPanel0;
	private JmolPanel jmolPanel1;
	private JmolViewer view0;
	private JmolViewer view1;
    private JTextField input = new JTextField(30);
    private JTextField zAxisRotField = new JTextField("0", 2);
    private JTextField xAxisRotField = new JTextField("0", 2);
    private JTextField yAxisRotField = new JTextField("-45", 2);
    private JLabel currentMolLabel = new JLabel("");
    private JLabel zAxisRotLabel = new JLabel("Z Rotation of Axis/Plane: ");
    private JLabel xAxisRotLabel = new JLabel("X Rotation of Axis/Plane: ");
    private JLabel yAxisRotLabel = new JLabel("Y Rotation of Axis/Plane: ");
	private JCheckBox showAxis = new JCheckBox("Show Axis");
	private JCheckBox showPlane = new JCheckBox("Show Plane");
    private JSlider zAxisRotSlider;
    private JSlider xAxisRotSlider;
    private JSlider yAxisRotSlider;
    
    ////////////BUTTONS\\\\\\\\\\\\
    private JButton selectButton = new JButton("Select");
    private JButton rotateButton = new JButton("Rotate On/Off");
    private JButton invertButton = new JButton("Invert");
    private JButton reset0Button = new JButton("Reset");
    private JButton reset1Button = new JButton("Reset");
    private JButton rotButton = new JButton("Rotate");
    private JButton rotRefButton = new JButton("Rotate & Invert");
    private JButton refButton = new JButton("Reflect");
    private JButton previous = new JButton("Previous");
    private JButton next = new JButton("Next");  
    
    ////////////STATE VARIABLES\\\\\\\\\\\\
    private boolean rotateOn = false;
    private boolean inverted = false;
    private boolean reflected = false;
	private boolean axisShown = false;
	private boolean planeShown = false;
	private boolean loadingMol0 = false;//These are both used to be able to tell when both Jmol windows are finished loading.
    @SuppressWarnings("unused")
	private boolean loadingMol1 = false;
    private String currentMolecule = "";
    @SuppressWarnings("unused")
	private String callbackString = new String("Nothing");
    private int rotationAmount;    
    private final int W = 1400;
    private final int H = 800;
    private int zAxisRot = 0;
	private int xAxisRot = 0;
	private int yAxisRot = -45;
    private float axisRadius = 5.539443f;
    //These vectors are both halves of the real axis...      
	private Vector3 axisEnd0 = new Vector3(axisRadius, 0, 0);
	private Vector3 axisEnd1 = new Vector3(-axisRadius,0,0);
    protected Map<EnumCallback, String> callbacks = new Hashtable<EnumCallback, String>();

    ////////////LISTENERS\\\\\\\\\\\\    
    private MyChangeListener sliderListener = new MyChangeListener();
    private MyJmolListener jListen0 = new MyJmolListener();
    private MyJmolListener jListen1 = new MyJmolListener();
    
	public void init()
	{			
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
        String[] rotString = {"Rotations","c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "c10"};
        @SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox rotBox = new JComboBox(rotString);
        rotBox.setSelectedIndex(0);
        JPanel rotBoxPan = new JPanel(new FlowLayout());
        rotBoxPan.add(rotBox);
        
        rot.add(rotBoxPan);
        
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
        
        String[] rotRefString = {"Rotation & Reflection",
            "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10"};
        @SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox rotRefBox = new JComboBox(rotRefString);
        rotBox.setSelectedIndex(0);
        JPanel rotRefBoxPan = new JPanel(new FlowLayout());
        rotRefBoxPan.add(rotRefBox);
        
        rotRef.add(rotRefBoxPan);
        rotRef.add(rotRefTitle);
        rotRef.add(rotRefButFlow);
        
        //REFLECT Tab\\
        ref = new JPanel();
        ref.setLayout(new BoxLayout(ref, BoxLayout.Y_AXIS));
        JPanel reflectionButFlow = new JPanel(new FlowLayout()); 
        refButton.addActionListener(handler);
        reflectionButFlow.add(refButton);
        JLabel reflectionTitle = new JLabel("REFLECTION");
        reflectionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        
        ref.add(reflectionTitle);
        ref.add(reflectionButFlow);
         
        
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
        JPanel southCenterBorder = new JPanel();
        southCenterBorder.setLayout(new BorderLayout());
        
        
        //////////MOLECULE VARIATION GROUP\\\\\\\\\\\ (Testing purposes only...)
        //create previous/next buttons
        previous.addActionListener(handler);
        next.addActionListener(handler);
        //Label
        JLabel molVar = new JLabel("  Molecule Variations  ");
        JPanel buttonFlow = new JPanel();
        buttonFlow.setLayout(new FlowLayout());
        buttonFlow.add(previous);
        buttonFlow.add(molVar);
        buttonFlow.add(next);
        
        
        
        //////////AXIS OPTIONS GROUP\\\\\\\\\\\ 
        JPanel axisOptionsGroup = new JPanel(new BorderLayout());
        
        JPanel axisRotSlidersBoxPanel = new JPanel();
        axisRotSlidersBoxPanel.setLayout(new BoxLayout(axisRotSlidersBoxPanel, BoxLayout.Y_AXIS));
        
        
        //TODO Right now we are only using the y-slider.  I do not know if it will be possible to incorperate the others
        zAxisRotSlider = new JSlider(JSlider.HORIZONTAL, -90, 90,0);
        zAxisRotSlider.addChangeListener(sliderListener);
        
        xAxisRotSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        xAxisRotSlider.addChangeListener(sliderListener);
        
        yAxisRotSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        yAxisRotSlider.addChangeListener(sliderListener);
        yAxisRotSlider.setValue(-45);
        
        JPanel zAxisRotFlow = new JPanel(new FlowLayout());
        zAxisRotFlow.add(zAxisRotLabel);
        zAxisRotFlow.add(zAxisRotField);
        zAxisRotField.addActionListener(handler);
        axisRotSlidersBoxPanel.add(zAxisRotSlider);
        axisRotSlidersBoxPanel.add(zAxisRotFlow);        
        
        JPanel xAxisRotFlow = new JPanel(new FlowLayout());
        xAxisRotFlow.add(xAxisRotLabel);
        xAxisRotFlow.add(xAxisRotField);
        xAxisRotField.addActionListener(handler);
        axisRotSlidersBoxPanel.add(xAxisRotSlider);
        axisRotSlidersBoxPanel.add(xAxisRotFlow);
        
        JPanel yAxisRotFlow = new JPanel(new FlowLayout());
        yAxisRotFlow.add(yAxisRotLabel);
        yAxisRotFlow.add(yAxisRotField);
        yAxisRotField.addActionListener(handler);     
        axisRotSlidersBoxPanel.add(yAxisRotSlider);
        axisRotSlidersBoxPanel.add(yAxisRotFlow);
        
        JPanel axisRotChecksBoxPanel = new JPanel();
        axisRotChecksBoxPanel.setLayout(new BoxLayout(axisRotChecksBoxPanel, BoxLayout.Y_AXIS));
        showAxis.addActionListener(handler);
        showPlane.addActionListener(handler);
        JLabel axisOptionsLabel = new JLabel("<html><body>Axis/Plane<br>Options:</body></html>");
        axisOptionsLabel.setFont(new Font("Sans Serif", Font.BOLD, 20));
        axisRotChecksBoxPanel.add(axisOptionsLabel);
        axisRotChecksBoxPanel.add(new JLabel(" "));
        axisRotChecksBoxPanel.add(showAxis);
        axisRotChecksBoxPanel.add(showPlane);
     
        axisOptionsGroup.add(axisRotChecksBoxPanel, BorderLayout.WEST);
        axisOptionsGroup.add(axisRotSlidersBoxPanel, BorderLayout.CENTER);
        axisOptionsGroup.setBorder(BorderFactory.createLineBorder(Color.black));
        
        
        
        
        
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
        
        //set up bottom layout
        bottom.add(southCenterBorder, BorderLayout.CENTER);
        bottom.add(axisOptionsGroup, BorderLayout.WEST);
        
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
		
		axisEnd0 = new Vector3(orig.x, 0, orig.y);
		axisEnd1 = new Vector3(-orig.x,0,-orig.y);
	}
	
	//This code found at: http://biojava.org/wiki/BioJava:CookBook:PDB:Jmol
	static class JmolPanel extends JPanel 
	{        
        private static final long serialVersionUID = -3661941083797644242L;
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
            
            viewer.renderScreenImage(g, rectClip.width, rectClip.height);
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
				//TODO change this so that it accepts from Nathan's drop-down
            	rotationAmount = 180;
						           	
            	view1.evalString(
            		"select all;" +
            		"rotateSelected $axis1 "+ rotationAmount+" 30;");//30 is the angles/sec of the rotation
      
               	//First we need to get the rotation angles so that we can align the molecule
            	//  with the y axis so that we just have to modify y-coordinates
            	//	when we reflect.  
            	//		Assuming your vector is (x,y,z):
            	//			1. Rotate by alpha around the y-axis.
            	//			2. Rotate by beta around the z-axis.
            	double x = axisEnd0.x;
            	double y = axisEnd0.y;
            	double z = axisEnd0.z;
            	double alpha = Math.toDegrees(Math.atan2(z, x));
            	double r = Math.sqrt(x*x + y*y + z*z);
            	double beta = Math.toDegrees(Math.acos(y/r));
            	double negAlpha = -alpha;
            	double negBeta = -beta;
            	
            	//Get the total number of atoms so we know what to loop through.
				int numAtoms = view1.getAtomCount();
				//System.out.println("Atom Count = "+numAtoms);
									
				//This just sends one big segment of commands to jmol and lets it evaluate it...
				view1.evalString(
			
					//First align all the atoms to the y axis for measurements.
					"select all;" +
					"rotateSelected {0,0,0} {0,1,0} "+alpha+";" +
					"rotateSelected {0,0,0} {0,0,1} "+beta+";"+
					
					//Take measurements and store them to arrays...
					"origY = ["+numAtoms+"];"+ //Saves original location.
					"yChange = ["+numAtoms+"];"+  //Saves the incremental change for the particular molecule.
					"for(i=1; i<"+numAtoms+"+1; i++)" + //Loop through all of the molecules.
					"{"+
						"origY[@i] = {atomno = i}.y;"+
						"yChange[@i] = {atomno = i}.y / 100;"+
					"}"+
											
					//Reset the atoms alignment so we can begin the for loop...
					"rotateSelected {0,0,0} {0,0,1} "+negBeta+";"+
					"rotateSelected {0,0,0} {0,1,0} "+negAlpha+";"+
															
					
					
					//Basically animate the inversion.
					"for(j=0; j<200; j++)"+ //j is the number of frames
					"{" +
						//Align everything to the y axis so that we can translate along the y.
						"select all;" +
						"rotateSelected {0,0,0} {0,1,0} "+alpha+";" +
						"rotateSelected {0,0,0} {0,0,1} "+beta+";"+
						
						//For each atom we are going to move it by an increment.  
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
						
						//Reorient the atom before the delay so that the molecule looks like it is in the right place.
						"select all;" +
						"rotateSelected {0,0,0} {0,0,1} "+negBeta+";"+
						"rotateSelected {0,0,0} {0,1,0} "+negAlpha+";"+
						
						"delay 0.025;"+  //Controls the fps (Essentially the time between frames)
					"}"
					//"echo \"\";"	
				);
                reflected = !reflected;
			}
			
			//Called if the ROTATE Button is hit
            else if(e.getSource() == rotButton)
            {
            	//TODO change this so that it accepts from Nathan's drop-down
            	rotationAmount = 180;
						           	
            	view1.evalString(
            		"select all;" +
            		"rotateSelected $axis1 "+ rotationAmount+" 30;");//30 is the angles/sec of the rotation
      
            }
			
			//Called if the REFLECT Button is hit.
            else if(e.getSource() == refButton)
            {
            	//First we need to get the rotation angles so that we can align the molecule
            	//  with the y axis so that we just have to modify y-coordinates
            	//	when we reflect.  
            	//		Assuming your vector is (x,y,z):
            	//			1. Rotate by alpha around the y-axis.
            	//			2. Rotate by beta around the z-axis.
            	double x = axisEnd0.x;
            	double y = axisEnd0.y;
            	double z = axisEnd0.z;
            	double alpha = Math.toDegrees(Math.atan2(z, x));
            	double r = Math.sqrt(x*x + y*y + z*z);
            	double beta = Math.toDegrees(Math.acos(y/r));
            	double negAlpha = -alpha;
            	double negBeta = -beta;
            	
            	//Get the total number of atoms so we know what to loop through.
				int numAtoms = view1.getAtomCount();
				//System.out.println("Atom Count = "+numAtoms);
									
				//This just sends one big segment of commands to jmol and lets it evaluate it...
				view1.evalString(
			
					//First align all the atoms to the y axis for measurements.
					"select all;" +
					"rotateSelected {0,0,0} {0,1,0} "+alpha+";" +
					"rotateSelected {0,0,0} {0,0,1} "+beta+";"+
					
					//Take measurements and store them to arrays...
					"origY = ["+numAtoms+"];"+ //Saves original location.
					"yChange = ["+numAtoms+"];"+  //Saves the incremental change for the particular molecule.
					"for(i=1; i<"+numAtoms+"+1; i++)" + //Loop through all of the molecules.
					"{"+
						"origY[@i] = {atomno = i}.y;"+
						"yChange[@i] = {atomno = i}.y / 100;"+
					"}"+
											
					//Reset the atoms alignment so we can begin the for loop...
					"rotateSelected {0,0,0} {0,0,1} "+negBeta+";"+
					"rotateSelected {0,0,0} {0,1,0} "+negAlpha+";"+
															
					
					
					//Basically animate the inversion.
					"for(j=0; j<200; j++)"+ //j is the number of frames
					"{" +
						//Align everything to the y axis so that we can translate along the y.
						"select all;" +
						"rotateSelected {0,0,0} {0,1,0} "+alpha+";" +
						"rotateSelected {0,0,0} {0,0,1} "+beta+";"+
						
						//For each atom we are going to move it by an increment.  
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
						
						//Reorient the atom before the delay so that the molecule looks like it is in the right place.
						"select all;" +
						"rotateSelected {0,0,0} {0,0,1} "+negBeta+";"+
						"rotateSelected {0,0,0} {0,1,0} "+negAlpha+";"+
						
						"delay 0.025;"+  //Controls the fps (Essentially the time between frames)
					"}"
					//"echo \"\";"	
				);
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
                                        //view1.evalString("select all; invertSelected PLANE \""+refPlane+"\";");
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
                    //view1.evalString("select all; invertSelected PLANE \""+refPlane+"\";");
                    reflected = !reflected;
                }
                
				
				//remove any axis that is being drawn.
				view1.evalString("draw axis1 DELETE");
				if(axisShown)
				{
					showAxis.setSelected(false);
					axisShown = false;
				}
			}
            else if(e.getSource() == zAxisRotField)
            {
            	zAxisRotSlider.setValue(Integer.parseInt(zAxisRotField.getText()));
            }
            else if(e.getSource() == xAxisRotField)
            {
            	xAxisRotSlider.setValue(Integer.parseInt(xAxisRotField.getText()));
            }
            else if(e.getSource() == yAxisRotField)
            {
            	yAxisRotSlider.setValue(Integer.parseInt(yAxisRotField.getText()));
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
            	
            	int scale = (int)Math.ceil(axisRadius * 180.5);
            	
            	if(planeShown == true) view1.evalString(
			    		"draw circle {0,0,0} {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"} "+
	    				"SCALE "+scale+" translucent;");
            	else view1.evalString("draw circle DELETE");
            }
            else if(e.getSource() == next)
            {
            	//view1.evalString("show STATE;");
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
						//We cannot just set these values, because then we cannot control the length of the
						//  axis depending on the size of the molecule.  We will load the axis as usual and
						//  then rotate it where we want it.  
						
						Vector3 orig = new Vector3(axisRadius, 0, 0);
						
						Matrix3x3 preliminaryYRot = Matrix3x3.rotationMatrix(yAxisRot);
						
						orig = preliminaryYRot.transform(orig);
						
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
							int scale = (int)Math.ceil(axisRadius * 180.5);
							view1.evalString(
						    		"draw circle {0,0,0} {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"} "+
						    				"SCALE "+scale+" color translucent;");
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
	
	/*
	 * This class listens to the sliders (for the planes and axes)  and will adjust things 
	 *   as you move the slider bars.
	 */
	private class MyChangeListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e) 
		{
		    //the XAXISSLIDER is being adjusted.
		    if(e.getSource() == zAxisRotSlider)
		    {
		    	//showAxis.setSelected(true);
		    	//axisShown=true;
		    	
		    	int zValue = zAxisRotSlider.getValue();
		    	zAxisRotField.setText(""+zValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	zValue = zValue - zAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	zAxisRot = zAxisRotSlider.getValue();
		    	
		    	//Create a rotation matrix for the current rotation
		    	Matrix3x3 rotMatrix0 = Matrix3x3.rotationMatrix(zValue);
		    	Matrix3x3 rotMatrix1 = Matrix3x3.rotationMatrix(zValue);
		    	//TODO Do we need both of these matricies?
		    	
		    	//Apply the rotation to the axis end.
		    	axisEnd0 = rotMatrix0.transform(axisEnd0);
		    	axisEnd1 = rotMatrix1.transform(axisEnd1);
		    	
		    	//Redraw the new Axis.
		    	if(axisShown)view1.evalString(
			    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
				    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    }
		    else if(e.getSource() == xAxisRotSlider)
		    {
		    	//showAxis.setSelected(true);
		    	//axisShown=true;
		    	
		    	int xValue = xAxisRotSlider.getValue();
		    	xAxisRotField.setText(""+xValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	xValue = xValue - xAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	xAxisRot = xAxisRotSlider.getValue();
		    	
		    			    	
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
		    	if(axisShown)view1.evalString(
		    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	
		    }
		    else if(e.getSource() == yAxisRotSlider)
		    {
		    	int yValue = yAxisRotSlider.getValue();
		    	yAxisRotField.setText(""+yValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	yValue = yValue - yAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	yAxisRot = yAxisRotSlider.getValue();
		    	
		    			    	
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
		    	if(axisShown)view1.evalString(
		    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
			    		" {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"};");
		    	if(planeShown)
				{
					int scale = (int)Math.ceil(axisRadius * 180.5);
					view1.evalString(
				    		"draw circle {0,0,0} {"+axisEnd1.x+","+axisEnd1.y+","+axisEnd1.z+"} "+
				    				"SCALE "+scale+" color translucent;");
				}
		    }
		 }
	}
} 