package vizster.action;

import java.util.Iterator;

import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;

/**
 * BorderToggleAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class InvertToggleAction extends AbstractAction {

    private int numFlashes = 3;
    
    public void run(ItemRegistry registry, double frac) {
        // [0-on, 0.2-off, 0.4-on, 0.6-off, 0.8-on, 1.0-off]
        int scalar = 2*numFlashes-1;
        Boolean val = (int)(frac*scalar)%2 == 0 ? Boolean.TRUE : Boolean.FALSE;
        
        // set the values
        Iterator<?> iter = registry.getNodeItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setVizAttribute("invert",val);
        }
        iter = registry.getEdgeItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setVizAttribute("invert",val);
        }
    } //

} // end of class InvertToggleAction
