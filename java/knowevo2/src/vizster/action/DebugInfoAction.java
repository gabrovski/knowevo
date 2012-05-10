package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import vizster.Vizster;
import edu.berkeley.guir.prefuse.Display;

/**
 * Turns on the display of a debugging info string on the Vizster display.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class DebugInfoAction extends AbstractAction {

    private Vizster vizster;
    
    public DebugInfoAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Display d = vizster.getDisplay();
        d.setDebug(!d.getDebug());
    } //

} // end of class DebugInfoAction