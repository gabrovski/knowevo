package knowevo.myvizster.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import knowevo.myvizster.Vizster;

/**
 * FitToViewAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class FitToViewAction extends AbstractAction {

    private Vizster vizster;
    
    public FitToViewAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        vizster.resetDisplay();
    } //

} // end of class FitToViewAction
