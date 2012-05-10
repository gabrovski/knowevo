package vizster.action;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.graph.io.XMLGraphWriter;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
@SuppressWarnings("serial")
public class SaveVisibleNetworkAction extends AbstractAction {

    private Vizster vizster;
    private FileDialog chooser;
    
    public SaveVisibleNetworkAction(Vizster vizster) {
        this.vizster = vizster;
        chooser = new FileDialog(vizster, "Save your data...");
    } //

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        // choose file to save to
        File f = null;
        chooser.setVisible(true);
        
        f = new File(chooser.getFile()); //TODO: Add directory
        
        // write out the current graph
        ItemRegistry registry = vizster.getItemRegistry();
        XMLGraphWriter gw = new XMLGraphWriter();
        try {
            gw.writeGraph(registry.getFilteredGraph(), f);
        } catch ( Exception e ) {
            e.printStackTrace();
            VizsterLib.defaultError(vizster, "Error saving file!");
        }
    } //

} // end of class SaveVisibleNetworkAction
