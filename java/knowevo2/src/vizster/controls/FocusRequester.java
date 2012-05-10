package vizster.controls;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * FocusRequester
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class FocusRequester extends MouseAdapter {

    public void mouseEntered(MouseEvent evt) {
        Component c = evt.getComponent();
        c.requestFocus();
    } //
    
} // end of class FocusRequester
