//TODO Possibly add a "Lock Axis" check box to allow the axis to rotate or not. 

package molMan;
//Reference the required Java libraries
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;

//The applet code  
public class Molly extends Applet 
{
   
    ////////////BUTTONS\\\\\\\\\\\\
    private JButton selectButton = new JButton("Select");
    private JButton spinButton = new JButton("Spin On/Off");
    private JButton invertButton = new JButton("Invert");
    private JButton reset0Button = new JButton("Reset");
    private JButton reset1Button = new JButton("Reset");
    private JButton rotButton = new JButton("Rotate");
    private JButton rotRefButton = new JButton("Rotate & Reflect");
    private JButton refButton = new JButton("Reflect");
    private JButton previous = new JButton("Previous");
    private JButton next = new JButton("Next");  
    private JButton xAlignAxis = new JButton("Align X");
    private JButton yAlignAxis = new JButton("Align Y");
    private JButton zAlignAxis = new JButton("Align Z");
    
    ////////////STATE VARIABLES\\\\\\\\\\\\
    private boolean rotateOn = false;
    private boolean inverted = false;
    private boolean reflected = false;
    private boolean rotated = false;
    private boolean rotatedAndReflected = false;
	private boolean axisShown = false;
	private boolean planeShown = false;
	private boolean perspectiveLink = true;
	private boolean loadingMol0 = false;//These are both used to be able to tell when both Jmol windows are finished loading
    @SuppressWarnings("unused")
	private boolean loadingMol1 = false;
    private boolean loadFromPubchem = false;
    private boolean axisRotLock = true;
    private String currentMolecule = "Caffeine";
    @SuppressWarnings("unused")
	private String callbackString = new String("Nothing");
    private String[] rotRefString = {"Rotation & Reflection",
            "s1: 360°", "s2: 180°", "s3: 120°", "s4: 90°", "s5: 72°", "s6: 60°",
            "s7: 51°", "s8: 45°", "s9: 40°", "s10: 36°"};
    private String[] rotString = {"Rotations","c1: 360°", "c2: 180°", "c3: 120°",
        "c4: 90°", "c5: 72°", "c6: 60°", "c7: 51°", "c8: 45°", "c9: 40°", "c10: 36°"};
    private int rotationAmount;    
    private final int W = 1400;
    private final int H = 800;
    private int zAxisRot = 0;
	private int xAxisRot = 0;
	private int yAxisRot = -45;
	int jmolWidth = 0;
    private float axisRadius = 5.539443f;
    //These vectors are both halves of the real axis...      
	private Vector3 axisEnd0 = new Vector3(axisRadius, 0, 0);
	//private Vector3 axisEnd1 = new Vector3(-axisRadius,0,0);
    protected Map<EnumCallback, String> callbacks = new Hashtable<EnumCallback, String>();
    double uPerspective = 0;
	double vPerspective = 0;
	double wPerspective = 0;
	double aPerspective = 0;
	
	 ////////////GUI Components\\\\\\\\\\\\
	private static final long serialVersionUID = 1L;	
	private JmolPanel jmolPanel0;
	private JmolPanel jmolPanel1;
	private JmolViewer view0;
	private JmolViewer view1;
    private JTextField input = new JTextField(30);
    private JTextField axisRotField = new JTextField("-45", 2);    
    private JLabel currentMolLabel = new JLabel("");
    private JLabel axisRotLabel = new JLabel("Rotation of Axis/Plane: ");
	private JCheckBox showAxis = new JCheckBox("Show Axis");
	private JCheckBox showPlane = new JCheckBox("Show Plane");
	private JCheckBox perspectiveLinkBox = new JCheckBox("Link Rot");
	private JCheckBox axisRotLockBox = new JCheckBox("Lock Rotation");
    private JSlider axisRotSlider;
    private JComboBox rotRefBox = new JComboBox(rotRefString);
    private JComboBox rotBox = new JComboBox(rotString);
    private JRadioButton pubChemRadioBut = new JRadioButton("PubChem");
    private JRadioButton nciNihRadioBut = new JRadioButton("NCI/NIH");
	
    ////////////LISTENERS\\\\\\\\\\\\    
    private MyChangeListener sliderListener = new MyChangeListener();
    private MyJmolListener jListen0 = new MyJmolListener();
    private MyJmolListener jListen1 = new MyJmolListener();
           
	public void init()
	{			
		//Calculate the appropriate size for jmolPanel		
		jmolWidth = this.getWidth()/3;
				
		jmolPanel0 = new JmolPanel();
        jmolPanel1 = new JmolPanel();

        jmolPanel0.setPreferredSize(new Dimension(jmolWidth,jmolWidth));
        jmolPanel1.setPreferredSize(new Dimension(jmolWidth, jmolWidth));

        setUpGui();
        loadStructure();
        axisRotSlider.setValue(-45);
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
        view0.evalString("load \"$Caffeine\";");        
        view1.evalString("load \"$Caffeine\";");
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
        ButtonListener handler = new ButtonListener();
        
        ///////////////////////////TOP SECTION\\\\\\\\\\\\\\\\\\\\\\\\
        //logo for the program
        JPanel logo = new JPanel();
        	JLabel logoLabel = new JLabel();
        	ImageIcon logoImage = new ImageIcon("logo.png", "MolMan");
        	logoLabel.setIcon(logoImage);
		logo.add(logoLabel);
        
        //add elements to the NORTH border
        JPanel molName = new JPanel();
        	molName.setLayout(new BorderLayout());
        	molName.setAlignmentY(CENTER_ALIGNMENT);
        
        	
        	JPanel loadMolGroup = new JPanel();
        		loadMolGroup.setLayout(new BorderLayout());
        	
	        	JPanel sourceSelectFlow = new JPanel(new FlowLayout());
	        		nciNihRadioBut.addActionListener(handler);
	        		pubChemRadioBut.addActionListener(handler);
	        		
	        		ButtonGroup sourcesRadioButs = new ButtonGroup();
	        		sourcesRadioButs.add(nciNihRadioBut);
	        		sourcesRadioButs.add(pubChemRadioBut);
	        		
	        		nciNihRadioBut.setSelected(true);
	        	
	        	sourceSelectFlow.add(nciNihRadioBut);
	        	sourceSelectFlow.add(pubChemRadioBut);
	        	
	        	        	
	        	JPanel borderTop = new JPanel();
	        	borderTop.setLayout(new FlowLayout());
	        	
		        	//label for the text box
		            JPanel mol = new JPanel();
		            	mol.setLayout(new FlowLayout());
		            	JLabel molLabel = new JLabel("<html><body>Enter the Name or Identifier <br>(SMILES, InChl, CAS) of a compound:</body></html>");
		            mol.add(molLabel);        	
		        	
		        	//text box
		            JPanel text = new JPanel();
		            	text.setLayout(new FlowLayout());
		            	input.addActionListener(handler);
		            text.add(input);
		        	
		        	//draw button
		            JPanel button = new JPanel();
		            	button.setLayout(new FlowLayout());
		            	selectButton.addActionListener(handler);
		            button.add(selectButton);
		        		        
		        borderTop.add(mol);
		        borderTop.add(text);
		        borderTop.add(button);
	
	        loadMolGroup.add(sourceSelectFlow, BorderLayout.NORTH);
	        loadMolGroup.add(borderTop, BorderLayout.CENTER);
	        loadMolGroup.setBorder(BorderFactory.createTitledBorder(
	        		BorderFactory.createLineBorder(Color.black), "Load Compound",
	        		2, 0, new Font("Sans Serif", Font.BOLD, 20)));
	        loadMolGroup.setPreferredSize(new Dimension(jmolWidth*2,jmolWidth/3));
		        
		        
	        JPanel currentPanel = new JPanel();
	        	currentPanel.setLayout(new FlowLayout());
	        	currentPanel.setAlignmentY(CENTER_ALIGNMENT);
	        	currentMolLabel = new JLabel("Current Molecule: ");
	            currentMolLabel.setFont(new Font("Sans Serif", Font.BOLD, 24));
	        currentPanel.add(currentMolLabel);
	        
        //Add border to the pane
	    molName.add(loadMolGroup, BorderLayout.CENTER);
        molName.add(currentPanel, BorderLayout.SOUTH);
        
        
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
        	
	        JLabel rotationTitle = new JLabel("ROTATION");
	        	rotationTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
	        
	        JLabel rotSpacer = new JLabel(" ");
	        	
	        JPanel rotTextFlow = new JPanel(new FlowLayout());
	    		JLabel rotTextLabel = new JLabel("<html><body>" +
	    				"Rotations occur around the axis of<br>" +
	    				"rotation.  Valid rotation degrees are<br>" +
	    				"360°/n for any integer 1=<n>=10.<br>" +
	    				" <br>" +
	    				"To perform a rotation, specify your<br>" +
	    				"desired axis with the options below.<br>" +
	    				"Then select the degrees of rotation from<br>" +
	    				"the drop-down list and click the Rotate<br>" +
	    				"Button." +
	    				"</body></html>");
	    		rotTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
    		rotTextFlow.add(rotTextLabel);
	        
	        JPanel rotBoxPan = new JPanel(new FlowLayout());
	        	rotBox.setSelectedIndex(0);
	        rotBoxPan.add(rotBox);
	        
	        JPanel rotationButFlow = new JPanel(new FlowLayout()); 
    			rotButton.addActionListener(handler);
    		rotationButFlow.add(rotButton);
	        
    	rot.add(rotationTitle);
    	rot.add(rotSpacer);
    	rot.add(rotTextFlow);
        rot.add(rotBoxPan);
        rot.add(rotationButFlow);
           
        //INVERT Tab\\
        inv = new JPanel();
	        inv.setLayout(new BoxLayout(inv, BoxLayout.Y_AXIS));
	        inv.setAlignmentX(CENTER_ALIGNMENT);
	        
	        JLabel inversionTitle = new JLabel("INVERSION");
        		inversionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
        		
        	JLabel invSpacer = new JLabel(" ");

        	JPanel invTextFlow = new JPanel(new FlowLayout());
        		JLabel inversionTextLabel = new JLabel("<html><body>" +
        				"Inversion moves all of the molecule's<br>" +
        				"atoms from their original position (x,y,z),<br>" +
        				"through the center point of the molecule,<br>" +
        				"to the opposite position (-x,-y,-z)." +
        				"</body></html>");
        		inversionTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
	        invTextFlow.add(inversionTextLabel);
	        
	        JPanel invButFlow = new JPanel(new FlowLayout()); 
        		invertButton.addActionListener(handler);
        	invButFlow.add(invertButton);
        	invButFlow.setAlignmentY(TOP_ALIGNMENT);
        
        inv.add(inversionTitle);
        inv.add(invSpacer);
        inv.add(invTextFlow);
        inv.add(invButFlow);
        
        //REFLECT Tab\\
        ref = new JPanel();
        	ref.setLayout(new BoxLayout(ref, BoxLayout.Y_AXIS));
        	
        	JLabel reflectionTitle = new JLabel("REFLECTION");
        		reflectionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
    
        	JLabel refSpacer = new JLabel(" ");
	        	
	        JPanel refTextFlow = new JPanel(new FlowLayout());
	    		JLabel refTextLabel = new JLabel("<html><body>" +
	    				"Reflections occur through a plane.<br>" +
	    				"Basically each atom is a certain distance,<br>" +
	    				"D, from the plane.  During the reflection,<br>" +
	    				"each atom is moved so that it is D away<br>" +
	    				"from the plane on the opposite side.<br>" +
	    				" <br>" +
	    				"To perform a reflection, specify your<br>" +
	    				"desired plane with the options below.<br>" +
	    				"Then click the Reflect Button." +
	    				"</body></html>");
	    		refTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
    		refTextFlow.add(refTextLabel);
    	        	
        		       		
	        JPanel reflectionButFlow = new JPanel(new FlowLayout()); 
		        refButton.addActionListener(handler);
		    reflectionButFlow.add(refButton);
	    
	    ref.add(reflectionTitle);
	    ref.add(refSpacer);
	    ref.add(refTextFlow);
        ref.add(reflectionButFlow);
         
        //ROTATE and REFLECT Tab\\
        rotRef = new JPanel();
	        rotRef.setLayout(new BoxLayout(rotRef, BoxLayout.Y_AXIS));
	        
	        JLabel rotRefTitle = new JLabel("ROTATION & REFLECTION");
	        	rotRefTitle.setFont(new Font("Sans Serif", Font.BOLD, 20));
	        	
	        
	        JLabel rotRefSpacer = new JLabel(" ");
	        	
	        JPanel rotRefTextFlow = new JPanel(new FlowLayout());
	    		JLabel rotRefTextLabel = new JLabel("<html><body>" +
	    				"Rotation and Reflection is basically.<br>" +
	    				"exactly what it sounds like.  A rotation<br>" +
	    				"is performed around an axis and then the<br>" +
	    				"atoms are reflected through the plane that<br>" +
	    				"perpendicular to the axis of rotation<br>" +
	    				" <br>" +
	    				"To perform a Rotation and Reflection, specify<br>" +
	    				"your desired axis/plane with the options<br>" +
	    				"below.  Select your degree of rotation from<br>" +
	    				"the drop-down list. Then click the Rotate<br>" +
	    				"and Reflect Button." +
	    				"</body></html>");
	    		rotRefTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
    		rotRefTextFlow.add(rotRefTextLabel);
    	        		       
	        JPanel rotRefBoxPan = new JPanel(new FlowLayout());
				rotRefBox = new JComboBox(rotRefString);
				rotRefBox.setSelectedIndex(0);
	        rotRefBoxPan.add(rotRefBox);
	        
	        JPanel rotRefButFlow = new JPanel(new FlowLayout()); 
        		rotRefButton.addActionListener(handler);
        	rotRefButFlow.add(rotRefButton);
        
        rotRef.add(rotRefTitle);
        rotRef.add(rotRefSpacer);
        rotRef.add(rotRefTextFlow);
        rotRef.add(rotRefBoxPan);
        rotRef.add(rotRefButFlow);
        
        
        
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.addTab("Rotation", null, rot, "Rotate the molecule around an axis");
        tabs.addTab("Inversion", null, inv, "Invert the molecule through a plane");
        tabs.addTab("Reflection", null, ref, "Reflect the molecule through a plane");
        tabs.addTab("Rot & Ref", null, rotRef, "");
        
        tabs.setPreferredSize(new Dimension(jmolWidth, jmolWidth-50));
       // tabs.setBorder(BorderFactory.createTitledBorder(
       // 		BorderFactory.createLineBorder(Color.black), "Symmetry Operations",
       // 		2, 0, new Font("Sans Serif", Font.BOLD, 16)));
        
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
        
        
        //////////AXIS OPTIONS GROUP\\\\\\\\\\\ 
        JPanel axisOptionsGroup = new JPanel(new BorderLayout());
        
        JPanel axisRotSlidersBoxPanel = new JPanel();
        	axisRotSlidersBoxPanel.setLayout(new BoxLayout(axisRotSlidersBoxPanel, BoxLayout.Y_AXIS));
        
        	axisRotSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);
        	axisRotSlider.addChangeListener(sliderListener);
        
      
	        JPanel yAxisRotFlow = new JPanel(new FlowLayout());
	        	axisRotField.addActionListener(handler);
	        yAxisRotFlow.add(axisRotLabel);
	        yAxisRotFlow.add(axisRotField);
	        
	        JPanel alignmentButsFlow = new JPanel(new FlowLayout());
	        	yAlignAxis.addActionListener(handler);
	        	xAlignAxis.addActionListener(handler);
	        	zAlignAxis.addActionListener(handler);
	        alignmentButsFlow.add(xAlignAxis);
	        alignmentButsFlow.add(yAlignAxis);
	        alignmentButsFlow.add(zAlignAxis);
	        	
        axisRotSlidersBoxPanel.add(alignmentButsFlow);
        axisRotSlidersBoxPanel.add(yAxisRotFlow);
        axisRotSlidersBoxPanel.add(axisRotSlider);
        
       
        
        JPanel axisRotChecksBoxPanel = new JPanel();
        	axisRotChecksBoxPanel.setLayout(new BoxLayout(axisRotChecksBoxPanel, BoxLayout.Y_AXIS));
        	showAxis.addActionListener(handler);
        	showPlane.addActionListener(handler);
        	axisRotLockBox.addActionListener(handler);
        	axisRotLockBox.setSelected(true);
        axisRotChecksBoxPanel.add(axisRotLockBox);
        axisRotChecksBoxPanel.add(showAxis);
        axisRotChecksBoxPanel.add(showPlane);
     
        axisOptionsGroup.add(axisRotChecksBoxPanel, BorderLayout.WEST);
        axisOptionsGroup.add(axisRotSlidersBoxPanel, BorderLayout.CENTER);
        axisOptionsGroup.setBorder(BorderFactory.createTitledBorder(
        		BorderFactory.createLineBorder(Color.black), "Axis/Plane Options",
        		2, 0, new Font("Sans Serif", Font.BOLD, 20)));
        axisOptionsGroup.setPreferredSize(new Dimension(jmolWidth,jmolWidth/3));
        
        
        //////////VIEW OPTIONS GROUP\\\\\\\\\\\ 
        JPanel southCenterBorder = new JPanel();
        southCenterBorder.setLayout(new BorderLayout());
        
        	JPanel controlButsFlow = new JPanel(new BorderLayout());
        	
		        JPanel reset0ButFlow = new JPanel(new FlowLayout());
		        	reset0Button.addActionListener(handler);
		        reset0ButFlow.add(reset0Button);
		        
		        JPanel rotButFlow = new JPanel(new FlowLayout());
		        	spinButton.addActionListener(handler);     
		        	perspectiveLinkBox.addActionListener(handler);
		        	perspectiveLinkBox.setSelected(true);
		        			        	
		        rotButFlow.add(spinButton);
		        rotButFlow.add(perspectiveLinkBox);
		        
		        JPanel reset1ButFlow = new JPanel(new FlowLayout());
		        	reset1Button.addActionListener(handler);
		        reset1ButFlow.add(reset1Button);
		        
	        controlButsFlow.add(reset0ButFlow, BorderLayout.WEST);
	        controlButsFlow.add(rotButFlow, BorderLayout.CENTER);
	        controlButsFlow.add(reset1ButFlow, BorderLayout.EAST);
	        
	        
	        
	        //////////MOLECULE VARIATION GROUP\\\\\\\\\\\ (Testing purposes only...)
	        //create previous/next buttons
	        JPanel buttonFlow = new JPanel(new FlowLayout());
		        previous.addActionListener(handler);
		        next.addActionListener(handler);
		        //Label
		        JLabel molVar = new JLabel("  Molecule Variations  ");
	       
		        JLabel googleCodeLable = new JLabel();
	        		ImageIcon googleCodeLogo = new ImageIcon("googleCodeIcon.png", "Google Code");
	        	googleCodeLable.setIcon(googleCodeLogo); 
	        	
	        	JLabel eclipseLable = new JLabel();
        			ImageIcon eclipseLogo = new ImageIcon("eclipseIcon.png", "Eclipse");
        		eclipseLable.setIcon(eclipseLogo);
        		
        		JLabel jmolLable = new JLabel();
    				ImageIcon jmolLogo = new ImageIcon("jmolIcon.png", "Jmol");
    			jmolLable.setIcon(jmolLogo);
    			
    			JLabel pubChemLable = new JLabel();
					ImageIcon pubChemLogo = new ImageIcon("pubChemIcon.png", "pubChem");
				pubChemLable.setIcon(pubChemLogo);
		    
				JLabel nihLable = new JLabel();
					ImageIcon nihLogo = new ImageIcon("nihIcon.png", "nih");
				nihLable.setIcon(nihLogo);
				
				JLabel javaLable = new JLabel();
					ImageIcon javaLogo = new ImageIcon("javaIcon.png", "java");
				javaLable.setIcon(javaLogo);
				
			buttonFlow.add(javaLable);
	        buttonFlow.add(jmolLable);
	        buttonFlow.add(eclipseLable);
	        buttonFlow.add(googleCodeLable);
	        buttonFlow.add(pubChemLable);
	        buttonFlow.add(nihLable);
	        //buttonFlow.add(previous);
	        //buttonFlow.add(molVar);
	        //buttonFlow.add(next);
	        
        southCenterBorder.add(controlButsFlow, BorderLayout.CENTER);
        southCenterBorder.add(buttonFlow, BorderLayout.SOUTH);
        
        
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
		
        if(loadFromPubchem)
        {
        	//http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/caffeine/SDF?record_type=3d
            urlName0 = urlName0.substring(55, urlName0.length()-19); 
            
            urlName0 = urlName0.replaceAll("%20", " ");//Puts spaces back in if the name is more then one word

            currentMolecule = urlName0;
            currentMolLabel.setText("Current Molecule: "+currentMolecule);
        }
        else //Load from NCI/NIH
        {
        	//http://cactus.nci.nih.gov/chemical/structure/PF5/file?format=sdf&get3d=True
        	urlName0 = urlName0.substring(45, urlName0.length()-27); 

        	 urlName0 = urlName0.replaceAll("%20", " ");
        	
            currentMolecule = urlName0;
            currentMolLabel.setText("Current Molecule: "+currentMolecule);
        }
        
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
		//axisEnd1 = new Vector3(-orig.x,0,-orig.y);
	}
	public void drawAxis()
	{
		view1.evalString(
	    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
		    		" {"+-axisEnd0.x+","+-axisEnd0.y+","+-axisEnd0.z+"};");
	}
	
	public void drawCircle()
	{
		int scale = (int)Math.ceil(axisRadius * 180.5);
    	
    	view1.evalString(
	    		"draw circle1 CIRCLE {0,0,0} {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"} "+
				"SCALE "+scale+" translucent [102,155,255];");
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
				
				if(loadFromPubchem)
				{
					view0.evalString(
							"try{" + //If the molecule fails to load an error dialogue box pops up...
									"load \":"+currentMolecule+"\"" +
							"}catch(e){prompt \"Molecule "+currentMolecule+" not found.\"}");
				    view1.evalString("try{load \":"+currentMolecule+"\"}catch(e){}");
				}
				else
				{
					view0.evalString(
							"try{" + //If the molecule fails to load an error dialogue box pops up...
									"load \"$"+currentMolecule+"\"" +
							"}catch(e){prompt \"Molecule "+currentMolecule+" not found.\"}");
				    view1.evalString("try{load \"$"+currentMolecule+"\"}catch(e){}");
				}
				
				      
			}
			
			//Called if the ROTATE On/Off button is hit to spin both molecules.
			else if(e.getSource()== spinButton)
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
					"rotateSelected $axis1 "+ rotationAmount+" 30;"+
					
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
                rotatedAndReflected = true;
			}
			
			//Called if the ROTATE Button is hit
            else if(e.getSource() == rotButton)
            {
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
            	rotated = true;
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
                reflected = true;
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

                if(reflected)
                {
                    view1.evalString("load \":"+currentMolecule+"\";");
                    reflected = !reflected;
                }
                else if (rotated)
                {
                	view1.evalString("load \":"+currentMolecule+"\";");
                	rotated = !rotated;
                }
                else if(rotatedAndReflected)
                {
                	view1.evalString("load \":"+currentMolecule+"\";");
                	rotatedAndReflected = !rotatedAndReflected;
                }
                else if(inverted) 
				{
					view1.evalString("select all; invertSelected POINT {0,0,0};");
					inverted = !inverted;
                }
			}
            else if(e.getSource() == axisRotField)
            {
            	axisRotSlider.setValue(Integer.parseInt(axisRotField.getText()));
            }
            else if(e.getSource() == perspectiveLinkBox)
            {
            	perspectiveLink = !perspectiveLink;
            	
            	//If this is turning the perspective link on, then we will go ahead and adjust view0.
            	if(perspectiveLink)
            	{
            		//This is where we handle the linking of perspectives for the jmol windows
					//Every time there is a mouse event with view1 this will run.
					view1.evalString("show STATE;");  //Get the current perspective
					
					if(jListen1.echoText != "")  //Make sure the String has been updated
					{
						//First we need to get the exact command we need out of the state String.
						String stateCommand = jListen1.echoText.substring(
								jListen1.echoText.indexOf("function _setPerspectiveState()"), 
								jListen1.echoText.indexOf("function _setSelectionState()")-1);
						stateCommand = stateCommand.substring(
								stateCommand.indexOf("moveto 0.0"), 
								stateCommand.indexOf("slab")); 

						//then we just feed that command into view0
						view0.evalString(stateCommand);
					
					}
            	}
            	
            }
			
            else if(e.getSource() == showAxis)
            {
            	axisShown = !axisShown;
            	if(axisShown == true) drawAxis();
            	else view1.evalString("draw axis1 DELETE");
            }
            else if(e.getSource() == showPlane)
            {
            	planeShown = !planeShown;
            	
            	int scale = (int)Math.ceil(axisRadius * 180.5);
            	
            	if(planeShown == true) drawCircle();
            	else view1.evalString("draw circle DELETE");
            }
            else if(e.getSource() == xAlignAxis)
            {
            	axisRotSlider.setValue(0);
            	
            	axisEnd0 = new Vector3(axisRadius, 0, 0);
            	
            	view1.evalString(
            			"reset;");
            	
            	if(perspectiveLink) view0.evalString("reset;");
            	
            	if(axisShown)drawAxis();
            	if(planeShown)drawCircle();
            	            	
            	
            }
            else if(e.getSource() == yAlignAxis)
            {
            	axisRotSlider.setValue(0);
            	
            	axisEnd0 = new Vector3(0, axisRadius, 0);
            	
            	view1.evalString(
            			"reset;" +
            			"rotate z 90;");
            	            	
            	if(perspectiveLink) 
            	{
            		view0.evalString(
                			"reset;" +
                			"rotate z 90;");
            	}
            	
            	if(axisShown)drawAxis();
            	if(planeShown)drawCircle();
            }
            else if(e.getSource() == zAlignAxis)
            {
            	axisRotSlider.setValue(0);
            	
            	axisEnd0 = new Vector3(0, 0, axisRadius);
            	
            	view1.evalString(
            			"reset;" +
            			"rotate y 90;");
            	
            	if(perspectiveLink) 
            	{
            		view0.evalString(
                			"reset;" +
                			"rotate y 90;");
            	}
            	
            	if(axisShown)drawAxis();
            	if(planeShown)drawCircle();
            	            	
            }
            else if(e.getSource() == pubChemRadioBut)
            {
            	loadFromPubchem = true;
            }
            else if(e.getSource() == nciNihRadioBut)
            {
            	loadFromPubchem = false;
            }
            else if(e.getSource() == axisRotLockBox)
            {
            	axisRotLock = !axisRotLock;
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
						
						if(perspectiveLink)
						{
							view0.evalString(stateCommand);
						}
						
						
						if(axisRotLock)
						{
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
							uPerspective = vals[1];
							vPerspective = vals[2];
							wPerspective = vals[3];
							aPerspective = vals[4];
							
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
							RotationMatrix rotMat = new RotationMatrix(0, 0, 0, uPerspective, vPerspective, wPerspective, Math.toRadians(-aPerspective));
							
							//do the matrix multiplication/
							double[] rotVector = rotMat.timesXYZ(x, y, z);
																		
							axisEnd0.x = rotVector[0];
							axisEnd0.y = rotVector[1];
							axisEnd0.z = rotVector[2];	
							//axisEnd1.x = -rotVector[0];
							//axisEnd1.y = -rotVector[1];
							//axisEnd1.z = -rotVector[2];	
							
							if(axisShown)drawAxis();
			            	if(planeShown)drawCircle();						
							
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
		    if(e.getSource() == axisRotSlider)
		    {
		    	//This is where we handle the linking of perspectives for the jmol windows
				//Every time there is a mouse event with view1 this will run.
				view1.evalString("show STATE;");  //Get the current perspective
				
				int yValue = axisRotSlider.getValue();
		    	axisRotField.setText(""+yValue);
		    	
		    	//This gets us the adjusted value (the ammount of the new rotation...)
		    	yValue = yValue - yAxisRot;
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	yAxisRot = axisRotSlider.getValue();
				
				if(jListen1.echoText != "")  //Make sure the String has been updated
				{
					//First we need to get the exact command we need out of the state String.
					String stateCommand = jListen1.echoText.substring(
							jListen1.echoText.indexOf("function _setPerspectiveState()"), 
							jListen1.echoText.indexOf("function _setSelectionState()")-1);
					stateCommand = stateCommand.substring(
							stateCommand.indexOf("moveto 0.0"), 
							stateCommand.indexOf("slab")); 

					
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
					uPerspective = vals[1];
					vPerspective = vals[2];
					wPerspective = vals[3];
					aPerspective = vals[4];
					
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
					
					//Wierd things happen when this is run without the Perspective state being changed.
					//  So we will only modify from the perspective if there is actually something that has happened.
					if(!(uPerspective==0.0 && wPerspective ==0.0 ))
					{
						RotationMatrix rotMat = new RotationMatrix(0, 0, 0, uPerspective, vPerspective, wPerspective, Math.toRadians(-aPerspective));
						
						//do the matrix multiplication
						double[] rotVector = rotMat.timesXYZ(x, y, z);
						
						axisEnd0.x = rotVector[0];
						axisEnd0.y = rotVector[1];
						axisEnd0.z = rotVector[2];	
					}
					else
					{
						axisEnd0.x = x;
						axisEnd0.y = y;
						axisEnd0.z = z;	
					}																
										
					if(axisShown)drawAxis();
	            	if(planeShown)drawCircle();
				}
		    }
		}
	}
} 