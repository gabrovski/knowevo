package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import vizster.Vizster;

/**
 * ShowImagesAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ShowImagesAction extends AbstractAction {

    private Vizster vizster;
    
    public ShowImagesAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        AbstractButton b = (AbstractButton)ae.getSource();
        vizster.setShowImages(b.isSelected());
    } //

} // end of class ShowImagesAction
