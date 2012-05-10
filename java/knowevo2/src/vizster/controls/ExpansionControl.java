package vizster.controls;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Entity;

/**
 * ExpansionControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class ExpansionControl extends ControlAdapter {

    private Object focusSetKey = FocusManager.DEFAULT_KEY;
    protected int ccount;
    protected Entity curFocus = null;
    
    /**
     * Creates a new FocusControl that changes the focus when an item is 
     * clicked the specified number of times. A click value of zero indicates
     * that the focus should be changed in response to mouse-over events.
     * @param clicks the number of clicks needed to switch the focus.
     */
    public ExpansionControl(int clicks) {
        ccount = clicks;
    } //
    
    private boolean isAllowedType(VisualItem item) {
        return item.getItemClass() == ItemRegistry.DEFAULT_NODE_CLASS;
    } //
    
    public void itemEntered(VisualItem item, MouseEvent e) {
        if ( isAllowedType(item) ) {
            Display d = (Display)e.getSource();
            d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    } //
    
    public void itemExited(VisualItem item, MouseEvent e) {
        if ( isAllowedType(item) ) {
            Display d = (Display)e.getSource();
            d.setCursor(Cursor.getDefaultCursor());
        }
    } //
    
    public void itemClicked(VisualItem item, MouseEvent e) {
        if ( isAllowedType(item) && ccount > 0 && 
             SwingUtilities.isLeftMouseButton(e)    && 
             e.getClickCount() == ccount )
        {
        	Entity focus = item.getEntity();
        	if ( focus != curFocus ) {
	            ItemRegistry registry = item.getItemRegistry();
	            FocusManager fm = registry.getFocusManager();
	            FocusSet fs = fm.getFocusSet(focusSetKey);
	            
	            if ( fs.contains(focus) ) {
	                if ( fs.size() > 1 )
	                    fs.remove(focus);
	                else
	                    Toolkit.getDefaultToolkit().beep();
	            } else {
	                fs.add(focus);
	            }
	            registry.touch(item.getItemClass());
        	}
        }
    } // 
    
} // end of class ExpansionControl
