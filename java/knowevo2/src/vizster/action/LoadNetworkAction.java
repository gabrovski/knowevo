package vizster.action;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

import vizster.Vizster;
import vizster.ui.VizsterMenuBar;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
@SuppressWarnings("serial")
public class LoadNetworkAction extends AbstractAction {

    private Vizster vizster;    
    private FileDialog chooser;
    
    public LoadNetworkAction(Vizster vizster) {
        this.vizster = vizster;
        chooser = new FileDialog(vizster, "Choose network file");
    } //

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        String datafile = null;
        String startUID = null;
        AbstractButton jc = (AbstractButton)arg0.getSource();
        
        if ( VizsterMenuBar.LOAD.equals(jc.getActionCommand()) ) {        	
        	chooser.setVisible(true);
        	datafile = chooser.getFile();
        	if(datafile == null)
        		return;
        	datafile = chooser.getDirectory() + "\\" + chooser.getFile(); //TODO: Make file separator system independent        	
        } else {
            startUID = Vizster.DEFAULT_START_UID;
        }
        vizster.loadGraph(datafile, startUID);
    } //

} // end of class SaveVisibleGraphAction
