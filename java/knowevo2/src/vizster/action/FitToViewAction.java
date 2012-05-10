package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import vizster.Vizster;

/**
 * FitToViewAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
@SuppressWarnings("serial")
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
