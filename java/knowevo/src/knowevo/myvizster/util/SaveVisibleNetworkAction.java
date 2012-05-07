package knowevo.myvizster.util;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import knowevo.myvizster.Vizster;
import knowevo.myvizster.VizsterLib;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.graph.io.XMLGraphWriter;

/**
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
public class SaveVisibleNetworkAction extends AbstractAction {

    private Vizster vizster;
    private JFileChooser chooser;
    
    public SaveVisibleNetworkAction(Vizster vizster) {
        this.vizster = vizster;
        chooser = new JFileChooser();
    } //

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        // choose file to save to
        File f = null;
        int rval = chooser.showOpenDialog(vizster);
        if( rval == JFileChooser.APPROVE_OPTION ) {
           f = chooser.getSelectedFile();
        } else {
            return;
        }
        
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
