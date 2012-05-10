package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import vizster.Vizster;
import vizster.ui.VizsterMenuBar;

/**
 * Updates which color map is used in attribute comparison mode
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ColorMapAction extends AbstractAction {

    private Vizster vizster;
    
    public ColorMapAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        int map = -1;
        if ( cmd == VizsterMenuBar.GMAP ) {
            map = 0;
        } else if ( cmd == VizsterMenuBar.HMAP ) {
            map = 1;
        } else if ( cmd == VizsterMenuBar.CMAP ) {
            map = 2;
        }
        vizster.getComparisonColorFunction().setColorMap(map);
        vizster.redraw();
    } //

} // end of class ColorMapAction
