package vizster.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import vizster.Vizster;
import vizster.ui.ProfileLookupDialog;

/**
 * ProfileSearchAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class ProfileSearchAction implements ActionListener {

    private Vizster vizster;
    private ProfileLookupDialog lookup;
    
    public ProfileSearchAction(Vizster vizster) {
        this.vizster = vizster;
        lookup = new ProfileLookupDialog(vizster, null); 
	
    } //
    
    public void actionPerformed(ActionEvent ae) {
        if ( lookup != null )
            lookup.setVisible(true);
        else
            JOptionPane.showMessageDialog(vizster, 
                    "Unable to connect to search engine.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
    } //

} // end of class ProfileSearchAction
