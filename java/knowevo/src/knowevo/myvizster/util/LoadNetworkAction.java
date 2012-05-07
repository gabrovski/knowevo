package knowevo.myvizster.util;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JFileChooser;

import knowevo.myvizster.Vizster;
import knowevo.myvizster.ui.VizsterMenuBar;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
public class LoadNetworkAction extends AbstractAction {

    private Vizster vizster;
    private JFileChooser chooser;
    
    public LoadNetworkAction(Vizster vizster) {
        this.vizster = vizster;
        chooser = new JFileChooser();
    } //

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        String datafile = null;
        String startUID = null;
        AbstractButton jc = (AbstractButton)arg0.getSource();
        
        if ( VizsterMenuBar.LOAD.equals(jc.getActionCommand()) ) {
	        // choose file to save to
	        File f = null;
	        int rval = chooser.showOpenDialog(vizster);
	        if( rval == JFileChooser.APPROVE_OPTION ) {
	           f = chooser.getSelectedFile();
	        } else {
	            return;
	        }
	        datafile = f.toString();
        } else {
            startUID = Vizster.DEFAULT_START_UID;
        }
        vizster.loadGraph(datafile, startUID);
    } //

} // end of class SaveVisibleGraphAction
