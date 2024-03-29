package molMan;
//Reference the required Java libraries
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.Map;
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
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.constant.EnumCallback;


/**
 * Molly is an applet designed to allow users (primarily chemistry students) to visualize and test for
 * molecular symmetry.  
 * @author Josh Kuestersteffen and Nathan Bohlig.
 * See Mercurial repository on Google Code: https://code.google.com/p/molecule-manipulator/ for 
 * revision history.
 */
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
    private JRadioButton xAlignAxis = new JRadioButton("X");
    private JRadioButton yAlignAxis = new JRadioButton("Y");
    private JRadioButton zAlignAxis = new JRadioButton("Z");
    private JRadioButton noneAlignAxis=new JRadioButton("custom");
    
    ////////////STATE VARIABLES\\\\\\\\\\\\
    private boolean rotateOn = false;
    private boolean inverted = false;
    private boolean reflected = false;
    private boolean rotated = false;
    private boolean rotatedAndReflected = false;
	private boolean axisShown = false;
	private boolean planeShown = false;
	private boolean perspectiveLink = true;
    private int numMolsToLoad = 0; //used to be able to tell when both Jmol windows are finished loading
    private boolean loadFromPubchem = false;
    private boolean axisRotLock = false;
    private String currentMolecule;
    private String[] rotRefString = {"Rotation & Reflection",
            "s1: 360\u00B0", "s2: 180\u00B0", "s3: 120\u00B0", "s4: 90\u00B0", "s5: 72\u00B0", "s6: 60\u00B0",
            "s7: 51\u00B0", "s8: 45\u00B0", "s9: 40\u00B0", "s10: 36\u00B0"};
    private String[] rotString = {"Rotations","c1: 360\u00B0", "c2: 180\u00B0", "c3: 120\u00B0",
        "c4: 90\u00B0", "c5: 72\u00B0", "c6: 60\u00B0", "c7: 51\u00B0", "c8: 45\u00B0", "c9: 40\u00B0", "c10: 36\u00B0"};
    private int rotationAmount;    
    private final int W = 1400;
    private final int H = 800;
	private int yAxisRot = 0;
	int jmolWidth = 0;
	private static final long serialVersionUID = 1L;	
    private float axisRadius = 5.539443f;
	private Vector3 axisEnd0 = new Vector3(axisRadius, 0, 0);
	
	 ////////////GUI Components\\\\\\\\\\\\
	private JmolPanel jmolPanel0;
	private JmolPanel jmolPanel1;
	private JmolViewer view0;
	private JmolViewer view1;
    private JTextField input = new JTextField(30);
    private JTextField axisRotField = new JTextField(""+yAxisRot, 2);    
    private JLabel currentMolLabel = new JLabel("");
    private JLabel axisRotLabel = new JLabel("Rotation of Axis/Plane: ");
	private JCheckBox showAxis = new JCheckBox("Show Axis");
	private JCheckBox showPlane = new JCheckBox("Show Plane");
	private JCheckBox perspectiveLinkBox = new JCheckBox("Link Rot");
	private JCheckBox axisRotLockBox = new JCheckBox("Lock Axis");
    private JSlider axisRotSlider;
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private JComboBox rotRefBox = new JComboBox(rotRefString);
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private JComboBox rotBox = new JComboBox(rotString);
    private JRadioButton pubChemRadioBut = new JRadioButton("PubChem");
    private JRadioButton nciNihRadioBut = new JRadioButton("NCI/NIH");
	
    ////////////LISTENERS\\\\\\\\\\\\    
    private MyChangeListener sliderListener = new MyChangeListener();
    private MyJmolListener jListen0 = new MyJmolListener();
    private MyJmolListener jListen1 = new MyJmolListener();
    private MouseCopier copycat = null;;
     
    /**
     * Runs when the applet is initialized.  It sets up the Jmol viewers and then loads up the GUI.
     */
    @Override
	public void init()
	{		
		//Calculate the appropriate size for jmolPanel	
		jmolWidth = this.getWidth()/3;
		
		jmolPanel0 = new JmolPanel();
        jmolPanel1 = new JmolPanel();

        jmolPanel0.setPreferredSize(new Dimension(jmolWidth,jmolWidth));
        jmolPanel1.setPreferredSize(new Dimension(jmolWidth, jmolWidth));

        setUpGui();  //Sets up the Swing user interface.
        
        setUpMouseListenersForLinkedRotation(); 
        
        loadStructure();  //Finishes initialising the Jmol viewers and load the first molecule.
        
        axisRotSlider.setValue(yAxisRot);  //initializest the slider so it matches the axis angle.
        
        this.setVisible(true);
        this.validate();  
	}
	
	/**
	 * Finishes seting up the Jmol viewers and loads the initial molecules.  
	 */
	public void loadStructure() 
	{ 
        view0 = jmolPanel0.getViewer();
        view0.setJmolStatusListener(jListen0);
        view1 = jmolPanel1.getViewer();        
        view1.setJmolStatusListener(jListen1);
        
        //Load up the initial molecules...
        currentMolecule = "Caffeine";
        loadMoleculesWithCheck();
    }
    
	/**
	 * Sets up the Graphical User Interface using Swing components.  
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
        		ImageIcon logoImage = createImageIcon("/images/logo.png", "Molly Logo");
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
        	
	        JPanel rotTitleFlow = new JPanel(new FlowLayout());
                JLabel rotationTitle = new JLabel("ROTATION");
                    rotationTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
            rotTitleFlow.add(rotationTitle);
	        
	        JPanel rotTextFlow = new JPanel(new FlowLayout());
	    		JLabel rotTextLabel = new JLabel("<html><body>" +
	    				"Rotations occur around an axis of<br>" +
	    				"rotation.  Valid rotation angles are<br>" +
	    				"360&deg;/<i>n</i> for any integer <i>n</i> between 1 and 10.<br>" +
	    				" <br>" +
	    				"To perform a rotation, specify your<br>" +
	    				"desired axis with the options below.<br>" +
	    				"Then select the degrees of rotation from<br>" +
	    				"the drop-down list and click the Rotate<br>" +
	    				"button." +
	    				"</body></html>");
	    		rotTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
    		rotTextFlow.add(rotTextLabel);
	        
	        JPanel rotBoxPan = new JPanel(new FlowLayout());
	        	rotBox.setSelectedIndex(0);
	        rotBoxPan.add(rotBox);
	        
	        JPanel rotationButFlow = new JPanel(new FlowLayout()); 
    			rotButton.addActionListener(handler);
    		rotationButFlow.add(rotButton);
	        
    	rot.add(rotTitleFlow);
    	rot.add(rotTextFlow);
        rot.add(rotBoxPan);
        rot.add(rotationButFlow);
           
        //INVERT Tab\\
        inv = new JPanel();
	        inv.setLayout(new BoxLayout(inv, BoxLayout.Y_AXIS));
	        inv.setAlignmentX(CENTER_ALIGNMENT);
	        
	        JPanel invTitleFlow = new JPanel(new FlowLayout());
                JLabel inversionTitle = new JLabel("INVERSION");
                    inversionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
            invTitleFlow.add(inversionTitle);

        	JPanel invTextFlow = new JPanel(new FlowLayout());
        		JLabel inversionTextLabel = new JLabel("<html><body>" +
        				"Inversion moves all of the molecule&rsquo;s<br>" +
        				"atoms from their original position (x, y, z),<br>" +
        				"through the center point of the molecule,<br>" +
        				"to the opposite position (-x, -y, -z).<br>" +
	    				" <br>" +
	    				"To perform an inversion, <br>" +
	    				"click the Invert button." +
        				"</body></html>");
        		inversionTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
	        invTextFlow.add(inversionTextLabel);
	        
	        JPanel invButFlow = new JPanel(new FlowLayout()); 
        		invertButton.addActionListener(handler);
        	invButFlow.add(invertButton);
        	invButFlow.setAlignmentY(TOP_ALIGNMENT);
        
        inv.add(invTitleFlow);
        inv.add(invTextFlow);
        inv.add(invButFlow);
        
        //REFLECT Tab\\
        ref = new JPanel();
        	ref.setLayout(new BoxLayout(ref, BoxLayout.Y_AXIS));
        	
	        JPanel refTitleFlow = new JPanel(new FlowLayout());
                JLabel reflectionTitle = new JLabel("REFLECTION");
                    reflectionTitle.setFont(new Font("Sans Serif", Font.BOLD, 24));
            refTitleFlow.add(reflectionTitle);
    
	        JPanel refTextFlow = new JPanel(new FlowLayout());
	    		JLabel refTextLabel = new JLabel("<html><body>" +
	    				"Reflections occur through a plane.<br>" +
	    				"Basically each atom is a certain distance,<br>" +
	    				"<i>D</i>, from the plane.  During the reflection,<br>" +
	    				"each atom is moved until it is <i>D</i> away<br>" +
	    				"from the plane on the opposite side.<br>" +
	    				" <br>" +
	    				"To perform a reflection, specify your<br>" +
	    				"desired plane with the options below.<br>" +
	    				"Then click the Reflect button." +
	    				"</body></html>");
	    		refTextLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
    		refTextFlow.add(refTextLabel);
        		       		
	        JPanel reflectionButFlow = new JPanel(new FlowLayout()); 
		        refButton.addActionListener(handler);
		    reflectionButFlow.add(refButton);
	    
	    ref.add(refTitleFlow);
	    ref.add(refTextFlow);
        ref.add(reflectionButFlow);
         
        //ROTATE and REFLECT Tab\\
        rotRef = new JPanel();
	        rotRef.setLayout(new BoxLayout(rotRef, BoxLayout.Y_AXIS));
	        
	        JPanel rotRefTitleFlow = new JPanel(new FlowLayout());
                JLabel rotRefTitle = new JLabel("ROTATION & REFLECTION");
                    rotRefTitle.setFont(new Font("Sans Serif", Font.BOLD, 20));
            rotRefTitleFlow.add(rotRefTitle);
	        
	        JPanel rotRefTextFlow = new JPanel(new FlowLayout());
	    		JLabel rotRefTextLabel = new JLabel("<html><body>" +
	    				"Rotation and Reflection is basically<br>" +
	    				"exactly what it sounds like.  A rotation<br>" +
	    				"is performed around an axis and then the<br>" +
	    				"atoms are reflected through the plane that<br>" +
	    				"is perpendicular to the axis of rotation.<br>" +
	    				" <br>" +
	    				"To perform a Rotation and Reflection, specify<br>" +
	    				"your desired axis/plane with the options<br>" +
	    				"below. Then select your degree of rotation from<br>" +
	    				"the drop-down list and click the Rotate<br>" +
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
        
        rotRef.add(rotRefTitleFlow);
        rotRef.add(rotRefTextFlow);
        rotRef.add(rotRefBoxPan);
        rotRef.add(rotRefButFlow);
        
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.addTab("Rotation", null, rot, "Rotate the molecule around an axis");
        tabs.addTab("Inversion", null, inv, "Invert the molecule through a plane");
        tabs.addTab("Reflection", null, ref, "Reflect the molecule through a plane");
        tabs.addTab("Rot & Ref", null, rotRef, "");
        
        tabs.setPreferredSize(new Dimension(jmolWidth, jmolWidth-50));
       
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
	        
            JPanel alignmentButsBox = new JPanel();
                alignmentButsBox.setLayout(new BoxLayout(alignmentButsBox,BoxLayout.Y_AXIS));
                JPanel alignmentButsLabelFlow = new JPanel(new FlowLayout());
                    alignmentButsLabelFlow.add(new JLabel("Align to molecule axis:"));
                alignmentButsBox.add(alignmentButsLabelFlow);
                JPanel alignmentButsFlow = new JPanel(new FlowLayout());
                    yAlignAxis.addActionListener(handler);
                    xAlignAxis.addActionListener(handler);
                    zAlignAxis.addActionListener(handler);
                    alignmentButsFlow.add(xAlignAxis);
                    alignmentButsFlow.add(yAlignAxis);
                    alignmentButsFlow.add(zAlignAxis);
                    alignmentButsFlow.add(noneAlignAxis);
                    ButtonGroup axesRadioButs = new ButtonGroup();
                    axesRadioButs.add(xAlignAxis);
                    axesRadioButs.add(yAlignAxis);
                    axesRadioButs.add(zAlignAxis);
                    axesRadioButs.add(noneAlignAxis);
                    noneAlignAxis.setSelected(true);
                alignmentButsBox.add(alignmentButsFlow);
	        	
        axisRotSlidersBoxPanel.add(alignmentButsBox);
        
        //I don't like how this works, so I'm removing it for now (jnv)
        //axisRotSlidersBoxPanel.add(yAxisRotFlow);
        //axisRotSlidersBoxPanel.add(axisRotSlider);
        
        JPanel axisRotChecksBoxPanel = new JPanel();
        	axisRotChecksBoxPanel.setLayout(new BoxLayout(axisRotChecksBoxPanel, BoxLayout.Y_AXIS));
        	showAxis.addActionListener(handler);
        	showPlane.addActionListener(handler);
        	axisRotLockBox.addActionListener(handler);
        	axisRotLockBox.setSelected(axisRotLock);
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

	        //Links Panel with Icons for relevant organizations.
	        /*
	        JPanel buttonFlow = new JPanel(new FlowLayout());
		       
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
	        */
	        	        
        southCenterBorder.add(controlButsFlow, BorderLayout.CENTER);
        //southCenterBorder.add(buttonFlow, BorderLayout.SOUTH);
        
        bottom.add(southCenterBorder, BorderLayout.CENTER);
        bottom.add(axisOptionsGroup, BorderLayout.WEST);
        
        //adds each section to applet window
        this.add(top);
        this.add(middle);
        this.add(bottom);
        
	}
    
    private void setUpMouseListenersForLinkedRotation()
    {
        //Get Jmol's mouse listeners
        MouseMotionListener[] mmListeners = jmolPanel0.getMouseMotionListeners();
        MouseListener[] mListeners = jmolPanel0.getMouseListeners();
        MouseWheelListener[] mwListeners = jmolPanel0.getMouseWheelListeners();
        MouseMotionListener mml=null;
        MouseListener ml=null;
        MouseWheelListener mwl=null;
        if (mmListeners.length>0)
            mml=mmListeners[0];
        if (mListeners.length>0)
            ml=mListeners[0];
        if (mwListeners.length>0)
            mwl=mwListeners[0];
        
        //Copy the mouse listener behaviors into the linked view
        copycat = new MouseCopier(ml,mml,null);
        jmolPanel1.addMouseListener(copycat);
        jmolPanel1.addMouseMotionListener(copycat);
        //we don't use the scroll wheel because behavior is weird
        
        //Snap the linked view when the driving view is clicked
        jmolPanel1.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent evt)
            {
                if (Molly.this.perspectiveLink)
                    jmolPanel0.copyOrientation(jmolPanel1);
            }
        });
    }
        
    /**
     * Called automatically by the browser when the applet is destroyed.
     * Also called manually by an application using the applet.
     */
    @Override
    public void destroy()
    {
        killJmolThreads();
    }
    
    /**
     * Looks for threads started by JMOL that need to be killed
     * when the applet is destroyed.
     * JMOL does not clean up after itself.
     */
    private void killJmolThreads()
    {
        Thread[] threads=new Thread[20];
        int numThreads = Thread.enumerate(threads);
        for (int i=0; i<numThreads; i++)
        {
            Thread t=threads[i];
            if (t != null)
            {
                String name = t.getName();
                //System.out.println(name);
                if (name.equals("HoverWatcher"))
                    t.interrupt();
            }
        }

    }
        
    
	
	/**
	 *  This method gets called by Jmol when Jmol is done loading the molecule 
	 *  so that the molecule name in the applet GUI can be updated. 
	 */
	public void changeCurrentMolLabel()
	{
		//The challenge here is dealing with race conditions between the applet and the Jmol applets.
		//  We have got to make sure that the right name is loaded.
		
		String urlName0 = view0.getModelSetPathName();
		
        if(loadFromPubchem)
        {
        	//http://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/caffeine/SDF?record_type=3d
            urlName0 = urlName0.substring(55, urlName0.length()-19); 
            urlName0 = urlName0.replaceAll("%20", " ");//Puts spaces back in if the name is more then one word
        }
        else //Load from NCI/NIH
        {
        	//http://cactus.nci.nih.gov/chemical/structure/PF5/file?format=sdf&get3d=True
        	urlName0 = urlName0.substring(45, urlName0.length()-27); 
        	urlName0 = urlName0.replaceAll("%20", " ");
        }
        currentMolecule = urlName0;
        currentMolLabel.setText("Current Molecule: "+currentMolecule);
        
        //Relayout the applet becasue the label may have changed size
        currentMolLabel.invalidate();
        this.validate();
	}
    
    private void resetAxis()
    {
        resetAxisSize();  //Makes sure that the axis is the right length for the new molecule.
        drawAxis();
        drawCircle();
        if (!axisShown) removeAxis();
        if (!planeShown) removeCircle();
    }
		
	//This method checks the size of the current molecule and adjusts the vector that controls
	//the length of the axis to match.
	private void resetAxisSize()
	{
		//Get the radius from the molecule loaded into view1.
		axisRadius = view1.getRotationRadius();
		
		//Create a temporary vector at the x axis.
		Vector3 orig = new Vector3(axisRadius, 0, 0);
		
		//Rotate the axis so that it has the proper alignment according to the slider.
		Matrix3x3 preliminaryRot = Matrix3x3.rotationMatrix(yAxisRot);		
		orig = preliminaryRot.transform(orig);		
		axisEnd0 = new Vector3(orig.x, 0, orig.y);
	}
	
	/**
	 * Draws the axis according to the vector axisEnd0.
	 */
	public void drawAxis()
	{
		view1.evalString(
	    		"draw axis1 {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"}" +
		    		" {"+-axisEnd0.x+","+-axisEnd0.y+","+-axisEnd0.z+"};");
	}
	
	/**
	 * Draws the plane (circle) perpendicular to the vector axisEnd0.
	 */
	public void drawCircle()
	{
		int scale = (int)Math.ceil(axisRadius * 180.5);
    	
    	view1.evalString(
	    		"draw circle1 CIRCLE {0,0,0} {"+axisEnd0.x+","+axisEnd0.y+","+axisEnd0.z+"} "+
				"SCALE "+scale+" translucent [102,155,255];");
	}
    
    /**
     * Removes the display of the axis
     */
    public void removeAxis()
    {
        view1.evalString("draw axis1 OFF");
    }
    
    /**
     * Removes the display of the plane (circle) perpendicular to the axis
     */
    public void removeCircle()
    {
        view1.evalString("draw circle1 OFF");
    }
	
	/** 
	 * Returns an ImageIcon, or null if the path was invalid.
	 * From: http://docs.oracle.com/javase/tutorial/uiswing/components/icon.html\
	 * 
	 * @param path  The String containing the path to the image file.
	 * @param description  The sting containing the description for the ImageIcon.
	 * @return  Returns the ImageIcon created.
	 */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
		
	/**
	 * ButtonListener handles all of the interaction with GUI components in the Applet.  
	 * @author Josh Kuestersteffen
	 */
	private class ButtonListener implements ActionListener 
	{
        @Override
		public void actionPerformed(ActionEvent e) 
		{
			//Called if the SELECT button is hit and you try to load a molecule.
			if (e.getSource() == selectButton || e.getSource() == input)
			{
				currentMolecule = input.getText();
                loadMoleculesWithCheck();
			}
			
			//Called if the SPIN On/Off button is hit to spin both molecules.
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
			
					//Perform the *********ROTATION*********:
					"select all;" +
					"rotateSelected $axis1 "+ rotationAmount+" 30;"+
					
					//Perform the *********REFLECTION*********
					//Align all the atoms to the y axis for measurements.
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
                
                //Check to see if there have been any transformations...  if so undo them
                if(reflected)
                {
                    reloadMoleculeView1();
                    reflected = !reflected;
                }
                else if (rotated)
                {
                	reloadMoleculeView1();
                	rotated = !rotated;
                }
                else if(rotatedAndReflected)
                {
                	reloadMoleculeView1();
                	rotatedAndReflected = !rotatedAndReflected;
                }
                else if(inverted) 
				{
					view1.evalString("select all; invertSelected POINT {0,0,0};");
					inverted = !inverted;
                }
			}
			//Updates the slider with a new number from the Axis rotation text field.
            else if(e.getSource() == axisRotField)
            {
            	axisRotSlider.setValue(Integer.parseInt(axisRotField.getText()));
            }
			//Turns the Perspective Link on or off.
            else if(e.getSource() == perspectiveLinkBox)
            {
            	perspectiveLink = perspectiveLinkBox.isSelected();
            	
                if (perspectiveLink)
                {
                    copycat.activate();
                    //If this is turning the perspective link on, then we will go ahead and adjust view0.
                    jmolPanel0.copyOrientation(jmolPanel1);
                }
                else
                    copycat.deactivate();
            }
			
			//Turns the axis on or off.
            else if(e.getSource() == showAxis)
            {
            	axisShown = showAxis.isSelected();
            	if (axisShown) drawAxis();
            	else removeAxis();
            }
			
			//Turns the plane on or off.
            else if(e.getSource() == showPlane)
            {
            	planeShown = showPlane.isSelected();
            	if (planeShown) drawCircle();
            	else removeCircle();
            }
			//Aligns the molecule's x axis to axis1.
            else if(e.getSource() == xAlignAxis)
            {
            	//First we set the axis1 so that it is along the x axis/
            	axisRotSlider.setValue(0);            	
            	axisEnd0 = new Vector3(axisRadius, 0, 0);
            	
            	//Then we reset the molecule perspective so that the axes are aligned.
            	view1.evalString("reset;");
      
            	if(perspectiveLink) view0.evalString("reset;");
            	
            	if(axisShown)drawAxis();
            	if(planeShown)drawCircle();
            }
			
			//Aligns the molecule's y axis to axis1.
            else if(e.getSource() == yAlignAxis)
            {
            	axisRotSlider.setValue(0);
            	
            	axisEnd0 = new Vector3(0, axisRadius, 0);
            	
            	view1.evalString(
            			"reset;" +
            			"rotate z 90;");  //We must rotate the molecule so that the y axis is aligned.
            	            	
            	if(perspectiveLink) 
            	{
            		view0.evalString(
                			"reset;" +
                			"rotate z 90;");
            	}
            	
            	if(axisShown)drawAxis();
            	if(planeShown)drawCircle();
            }
			
			//Aligns the molecule's z axis to axis1.
            else if(e.getSource() == zAlignAxis)
            {
            	axisRotSlider.setValue(0);
            	
            	axisEnd0 = new Vector3(0, 0, axisRadius);
            	
            	view1.evalString(
            			"reset;" +
            			"rotate y 90;");//We must rotate the molecule so that the z axis is aligned.
            	
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
			//Locks the axis rotation so that it does not move when the perspective is changed. 
            else if(e.getSource() == axisRotLockBox)
            {
            	axisRotLock = axisRotLockBox.isSelected();
                if (axisRotLock)
                    noneAlignAxis.setSelected(true);
                xAlignAxis.setEnabled(!axisRotLock);
                yAlignAxis.setEnabled(!axisRotLock);
                zAlignAxis.setEnabled(!axisRotLock);
                noneAlignAxis.setEnabled(!axisRotLock);
            }
		}		
	}
    
    private void loadMoleculesWithCheck()
    {
        numMolsToLoad = 2;

        //Tell Jmol to try to load the specified molecule 
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
    
    private void reloadMoleculeView1()
    {
        if(loadFromPubchem)
        {
            view1.evalString("load \":"+currentMolecule+"\";");
        }
        else
        {
            view1.evalString("load \"$"+currentMolecule+"\";");
        }
        resetAxis();
    }

    //Adjusts the location of axis1
    private void repositionAxis()
    {
        //Its rotation should be opposite to that of the molecule.  
        //This method of vector rotation from http://www.blitzbasic.com/Community/posts.php?topic=57616

        float[] aa = jmolPanel1.getAxisAngle();

        //Store the axis values and the angle of rotation.
        // u,v,w is the axis of rotation and a is the rotation amount in degrees
        double uPerspective = aa[0];
        double vPerspective = aa[1];
        double wPerspective = aa[2];
        double aPerspective = aa[3];

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
        RotationMatrix rotMat = new RotationMatrix(0, 0, 0, uPerspective, vPerspective, wPerspective, -aPerspective);

        //do the matrix multiplication/
        double[] rotVector = rotMat.timesXYZ(x, y, z);

        axisEnd0.x = rotVector[0];
        axisEnd0.y = rotVector[1];
        axisEnd0.z = rotVector[2];	

        if(axisShown)drawAxis();
        if(planeShown)drawCircle();		
    }
	
	/**
	 * This class listens to Jmol views and responds when certain events occur.  
	 * @author Josh Kuestersteffen
	 */
	public class MyJmolListener implements JmolStatusListener
	{
		
		@Override
		/**
		 * This Method performs an action when it receives a callback from the Jmol viewer.  
		 * @param type  The type of callback that is coming from the Jmol viewer.  See the list of 
		 * types in the notifyEnabled() method.  
		 * @param data  The data accompanying the callback.  It should be interpreted differently
		 * depending upon what type of callback it is accompanying.  
		 */
		public void notifyCallback(EnumCallback type, Object[] data) 
		{
			switch(type)
			{
				case ATOMMOVED:
					break;
				case MESSAGE:
					break;
				case CLICK: //Called whenever the Jmol screen is clicked.  (The mouse adjusts the molecule orientation)
					//This is where we are able to implement the PERSPECTIVE LINK
					
                    //Perspective linking is now handled by the copycat because
                    // of the terrible lag associated with the old method.
                    // It could be handled like this:
                    //    jmolPanel0.copyOrientation(jmolPanel1);
                    // but there is still a slight lag.
                    
                    //Axis lock is also handled without the use of "show STATE;".
                    
					//This is where we handle the linking of perspectives for the jmol windows
					//Every time there is a mouse event with view1 this will run.
                    if(axisRotLock)
                    {
                        //We also need to adjust the location of axis1
                        repositionAxis();
                    }
					break; 
				case ECHO:
					break;
				case APPLETREADY:
					break;
				case LOADSTRUCT: //Called when the applet is finished loading the new mol.
					//When this case has been hit twice, then it will decrement the variable
                    //twice.  This is when you know they are both done loading.
                    numMolsToLoad--;
                    if (numMolsToLoad == 0)
                    {
                        changeCurrentMolLabel();
                        resetAxis();
                    }
					break;
			}
		}

		@Override
		public boolean notifyEnabled(EnumCallback type) 
		{
			switch (type) 
			{
			  case LOADSTRUCT:
			  case CLICK:
				  return true;
			  case ANIMFRAME:
			  case ECHO:
			  case ERROR:
			  case EVAL:
			  case MEASURE:
			  case MESSAGE:
			  case PICK:
			  case SYNC:
			  case SCRIPT:
			  case APPLETREADY:
			  case ATOMMOVED:
			  case HOVER:
			  case MINIMIZATION:
			  case RESIZE:
              default:
                  return false;
			}
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
	
	/**
	 * This class listens to the sliders (for the planes and axes)  and will adjust things 
	 *   as you move the slider bars.
	 */
	private class MyChangeListener implements ChangeListener
	{
        @Override
		public void stateChanged(ChangeEvent e) 
		{
		    if(e.getSource() == axisRotSlider)
		    {
				int yValue = axisRotSlider.getValue();
		    	axisRotField.setText(""+yValue);
		    	
		    	//Make sure we keep track of just how far we have rotated...
		    	yAxisRot = axisRotSlider.getValue();
				
                repositionAxis();
		    }
		}
	}
} 