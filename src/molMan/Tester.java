/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package molMan;

import javax.swing.JFrame;

/**
 *
 * @author mixxm_000
 */
public class Tester {
    
    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        MolManGUI disp = new MolManGUI();
        frame.add(disp);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
}
