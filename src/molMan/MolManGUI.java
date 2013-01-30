/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package molMan;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author mixxm_000
 */
public class MolManGUI extends JPanel
{
    private final int W = 1400;
    private final int H = 800;
    
    public MolManGUI()
    {
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
        //adds each section to applet window
        this.add(top);
        this.add(middle);
        this.add(bottom);
    }
}
