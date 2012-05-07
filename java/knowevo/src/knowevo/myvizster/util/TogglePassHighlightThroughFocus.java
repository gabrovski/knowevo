package knowevo.myvizster.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import knowevo.myvizster.Vizster;

/**
 * ToggleHighlightPassThroughFocus
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class TogglePassHighlightThroughFocus extends AbstractAction {

    private Vizster vizster;

    public TogglePassHighlightThroughFocus(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    public void actionPerformed(ActionEvent ae) {
        AbstractButton b = (AbstractButton)ae.getSource();
        vizster.setPassHighlightTroughFocus(b.isSelected());
    } //

} // end of class TogglePassHighlightThroughFocus
