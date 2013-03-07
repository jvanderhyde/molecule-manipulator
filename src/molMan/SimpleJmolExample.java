package molMan;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
//import org.biojava.bio.structure.Structure;
//import org.biojava.bio.structure.io.PDBFileReader;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
 
 
public class SimpleJmolExample 
{
    JmolSimpleViewer viewer;
    //Structure structure; 
    String structurePbd;
 
    JmolPanel jmolPanel;
    JFrame frame ;
 
    public static void main(String[] args)
    {
        try 
        {
            //PDBFileReader pdbr = new PDBFileReader();          
            //pdbr.setPath("/Path/To/PDBFiles/");
 
            String pdbCode = "5pti";
 
            //Structure struc = pdbr.getStructureById(pdbCode);
 
            SimpleJmolExample ex = new SimpleJmolExample();
            //ex.setStructure(struc);
            ex.setStructure();
 
        } catch (Exception e){
            e.printStackTrace();
        }
    }
 
 
    public SimpleJmolExample() {
        frame = new JFrame();
        frame.addWindowListener(new ApplicationCloser());
        Container contentPane = frame.getContentPane();
        jmolPanel = new JmolPanel();
 
        jmolPanel.setPreferredSize(new Dimension(400,400));
        contentPane.add(jmolPanel);
 
        frame.pack();
        frame.setVisible(true); 
 
    }
    public void setStructure(){//Structure s) {
 
        //frame.setName(s.getPDBCode());
 
        // actually this is very simple
        // just convert the structure to a PDB file
 
        //String pdb = s.toPDB();
 
        //structure = s;
        JmolSimpleViewer viewer = jmolPanel.getViewer();
 
        // Jmol could also read the file directly from your file system
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
 
    public void setTitle(String label){
        frame.setTitle(label);
    }
 
    public JmolSimpleViewer getViewer(){
 
        return jmolPanel.getViewer();
    }
 
 
    static class ApplicationCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }
 
    static class JmolPanel extends JPanel {
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
 
        public JmolSimpleViewer getViewer() {
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