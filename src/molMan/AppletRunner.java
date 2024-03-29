//A main method to run molman as an application
//Created by James Vanderhyde, 29 Apr 2013
//Modify as desired.

package molMan;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AppletRunner
{
    public static void main(String[] args)
    {
        final java.awt.Frame f = new java.awt.Frame("Molly");
        final Molly applet = new Molly();
        f.add(applet);
        f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                f.setVisible(false);
                applet.stop();
                applet.destroy();
                f.dispose();
                //System.exit(0);//should not be necessary unless something is not cleaned up correctly in applet
            }
        });
        f.setSize(1110,720);
        
        f.setVisible(true);//must be done before applet.init()
        applet.init();
        applet.start();
        f.setVisible(true);//forces a repaint
    }
}
