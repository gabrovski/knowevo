package vizster.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import vizster.Vizster;
import vizster.ui.VizsterXRayColorFunction;

/**
 * Updates which attribute is visualized in the attribute comparison mode
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ColorAction extends AbstractAction {
	//TODO: Move these files to vizste.action package. Examine other package incosinsitencies
    private Vizster vizster;
    private JToggleButton prev;
    
    public ColorAction(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        JToggleButton tog = (JToggleButton)e.getSource();
        
        if ( prev != null && prev != tog ) {
            prev.setSelected(false);
            prev.putClientProperty("on", Boolean.FALSE);
            prev = null;
        }
        
        Boolean onB = (Boolean)tog.getClientProperty("on");
        boolean on = (onB == null ? tog.isSelected() : !onB.booleanValue());
        if ( on ) {
            Integer attrI = (Integer)tog.getClientProperty("attr");
            tog.putClientProperty("on", Boolean.TRUE);
            int idx = attrI.intValue();
            VizsterXRayColorFunction cf = vizster.getComparisonColorFunction();
            cf.setCurrentAttribute(idx);
            vizster.setMode(Vizster.COMPARE_MODE);
            prev = tog;
        } else {
            JToggleButton inv = (JToggleButton)tog.getClientProperty("inv");
            inv.doClick();
            tog.putClientProperty("on", Boolean.FALSE);
            vizster.setMode(Vizster.BROWSE_MODE);
        }
        vizster.redraw();
    } //

} // end of class ColorAction
