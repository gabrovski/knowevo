package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Exits the current application
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ExitAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    } //

} // end of class ExitAction
