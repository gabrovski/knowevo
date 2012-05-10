package vizster.action;

import java.util.Iterator;

import vizster.Vizster;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.filter.Filter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * AuraFilter
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class AuraFilter extends Filter {

    public AuraFilter() {
        super(Vizster.AURA_CLASS, true);
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
        FocusManager fman = registry.getFocusManager();
        FocusSet set = fman.getFocusSet(Vizster.SEARCH_KEY);
        
        synchronized ( set ) {
	        Iterator<?> iter = set.iterator();
	        while ( iter.hasNext() ) {
	            Node n = (Node) iter.next();
	            NodeItem nitem = registry.getNodeItem(n);
	            if ( nitem != null ) {
	                VisualItem item = registry.getItem(Vizster.AURA_CLASS, n, true, true);
	                item.setInteractive(false);
	            }
	        }
        }
        
        // garbage collect
        super.run(registry, frac);
    } //
    
} // end of class AuraFilter
