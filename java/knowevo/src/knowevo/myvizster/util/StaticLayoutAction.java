package knowevo.myvizster.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import knowevo.myvizster.Vizster;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
public class StaticLayoutAction extends AbstractAction {

    private Vizster vizster;
    
    public StaticLayoutAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        vizster.runStaticLayout();
    } //

} // end of class StaticLayoutAction
