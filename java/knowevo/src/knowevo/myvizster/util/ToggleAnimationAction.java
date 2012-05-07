package knowevo.myvizster.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import knowevo.myvizster.Vizster;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
public class ToggleAnimationAction extends AbstractAction {

    private Vizster vizster;
    
    public ToggleAnimationAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        vizster.setAnimate(!vizster.isAnimate());
    } //

} // end of class ToggleAnimationAction
