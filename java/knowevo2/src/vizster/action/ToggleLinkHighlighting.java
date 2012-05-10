package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import vizster.Vizster;

/**
 * ToggleLinkHighlighting
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ToggleLinkHighlighting extends AbstractAction {

    private Vizster vizster;

    public ToggleLinkHighlighting(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        AbstractButton b = (AbstractButton)ae.getSource();
        vizster.setLinkHighlighting(b.isSelected());
    } //

} // end of class ToggleLinkHighlighting
