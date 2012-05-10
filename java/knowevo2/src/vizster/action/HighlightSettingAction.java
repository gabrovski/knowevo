package vizster.action;

import java.util.Iterator;

import vizster.VizsterLib;

import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;

/**
 * ClearHighlightAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightSettingAction extends AbstractAction {

    private int defaultValue = 0;
    
    public HighlightSettingAction(int defValue) {
        defaultValue = defValue;
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
        Iterator<?> iter;
        
        iter = registry.getNodeItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setHighlighted(false);
            VizsterLib.setHighlightValue(item,defaultValue);
        }
        iter = registry.getEdgeItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setHighlighted(false);
            VizsterLib.setHighlightValue(item,defaultValue);
        }
        iter = registry.getAggregateItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setHighlighted(defaultValue == -1 ? false : true);
        }
    } //

} // end of class HighlightSettingAction
