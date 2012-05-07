package knowevo.myvizster.util;

import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import knowevo.myvizster.Vizster;
import knowevo.myvizster.VizsterDBLoader;
import knowevo.myvizster.VizsterLib;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * Allows users to jump to a given user id.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class GotoAction extends AbstractAction {

    private Vizster vizster;
    
    public GotoAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    public void actionPerformed(ActionEvent e) {
        // get the friendster uid from the user
        String uid = JOptionPane.showInputDialog(
                        vizster,
                        "Enter the User ID for the profile to go to.",
                        "Go To Profile",
                        JOptionPane.QUESTION_MESSAGE);
        if ( uid == null ) {
            // user canceled, so do nothing
            return;
        }
        
        // load the profile as needed
        VizsterDBLoader loader = vizster.getLoader();
        Node n = null;
        try {
            n = loader.getProfileNode(uid);
        } catch (SQLException e1) {
            // bail if profile not found
            VizsterLib.defaultError(vizster,
                    "Couldn't find the requested profile!");
            return;
        }
        
        // set the profile node as the new focus
        ItemRegistry registry = vizster.getItemRegistry();
        FocusManager fmanager = registry.getFocusManager();
        fmanager.getFocusSet(Vizster.CLICK_KEY).set(n);
        registry.getDefaultFocusSet().set(n);
    } //

} // end of class GotoAction
