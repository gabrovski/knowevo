package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;

import edu.berkeley.guir.prefusex.force.ForcePanel;

import vizster.Vizster;

/**
 * Brings up a dialog allowing users to configure the force simulation
 *  that provides Vizster's layout.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ForceConfigAction extends AbstractAction {

    private JDialog dialog;
    
    // TODO: remove usage of vizster from the constructor
    public ForceConfigAction(Vizster vizster) {
        dialog = new JDialog(vizster, false);
        dialog.setTitle("Configure Force Simulator");
        JPanel forcePanel = new ForcePanel(vizster.getForceSimulator());
        dialog.getContentPane().add(forcePanel);
        dialog.pack();
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        dialog.setVisible(true);
    } //

} // end of class ForceConfigAction
