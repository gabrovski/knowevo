package vizster.ui;

import java.util.Iterator;

import prefusex.community.CommunitySet;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * CommunityEdgeLabeler
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class CommunityEdgeLabeler extends AbstractAction {

    private Object m_key;
    
    public CommunityEdgeLabeler(Object communityKey) {
        m_key = communityKey;
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
        FocusManager fman = registry.getFocusManager();
        CommunitySet comm = (CommunitySet)fman.getFocusSet(m_key);
        
        // catch early exit case
        if ( comm.getCommunityCount() == 0 )
        	return;
        
        // iterate over edges, mark those that are between communities
        Iterator<?> iter = registry.getEdgeItems();
        while ( iter.hasNext() ) {
            EdgeItem ei = (EdgeItem)iter.next();
            NodeItem n1 = (NodeItem)ei.getFirstNode();
            NodeItem n2 = (NodeItem)ei.getSecondNode();
            int c1 = comm.getCommunity((Node)n1.getEntity());
            int c2 = comm.getCommunity((Node)n2.getEntity());
            boolean b = ( c1 != c2 && c1 != -1 && c2 != -1 );
            Boolean val = b ? Boolean.TRUE : Boolean.FALSE;
            ei.setVizAttribute("extraCommunity", val);
        }
    } //

} // end of class CommunityEdgeLabeler
