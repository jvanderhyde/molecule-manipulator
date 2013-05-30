//A listener for mouse events that passes the events on to another component.
//Created by James Vanderhyde, 30 May 2013

package molMan;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * A listener for mouse events that passes the events on to another component.
 * This is useful when the destination component has handlers that we want
 * to use.
 * @author James Vanderhyde
 */
public class MouseCopier implements MouseListener, MouseMotionListener,
        MouseWheelListener
{
    private MouseListener destMouse;
    private MouseMotionListener destMouseMotion;
    private MouseWheelListener destMouseWheel;
    
    private boolean active;

    public MouseCopier(MouseListener destMouse, 
                       MouseMotionListener destMouseMotion,
                       MouseWheelListener destMouseWheel)
    {
        this.destMouse=destMouse;
        this.destMouseMotion=destMouseMotion;
        this.destMouseWheel=destMouseWheel;
        
        this.active = true;
    }
    
    public void activate()
    {
        this.active = true;
    }

    public void deactivate()
    {
        this.active = false;
    }
    
    public boolean isActive()
    {
        return this.active;
    }

    @Override
    public void mouseClicked(MouseEvent evt)
    {
        //if (destMouse != null)
        //    destMouse.mouseClicked(evt);
        //Do nothing because we do not want to copy picks
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
        if (!evt.isPopupTrigger())
        {
            if (active && destMouse != null)
                destMouse.mousePressed(evt);
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt)
    {
        if (active && destMouse != null)
            destMouse.mouseReleased(evt);
    }

    @Override
    public void mouseEntered(MouseEvent evt)
    {
        if (active && destMouse != null)
            destMouse.mouseEntered(evt);
    }

    @Override
    public void mouseExited(MouseEvent evt)
    {
        if (active && destMouse != null)
            destMouse.mouseExited(evt);
    }

    @Override
    public void mouseDragged(MouseEvent evt)
    {
        if (active && destMouseMotion != null)
            destMouseMotion.mouseDragged(evt);
    }

    @Override
    public void mouseMoved(MouseEvent evt)
    {
        if (active && destMouseMotion != null)
            destMouseMotion.mouseMoved(evt);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt)
    {
        if (active && destMouseWheel != null)
            destMouseWheel.mouseWheelMoved(evt);
    }

}

