/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package molMan;

import java.applet.Applet;
import javax.swing.JFrame;

/**
 *
 * @author mixxm_000
 */
public class App extends Applet
{
    /**
     *
     */
    @Override
    public void init() 
    {
        JFrame frame = new JFrame();
        MolManGUI disp = new MolManGUI();
        frame.add(disp);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}
