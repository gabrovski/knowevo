package vizster.ui;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import prefusex.community.CommunitySet;
import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.action.filter.Filter;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * CommunityFilter
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class CommunityFilter extends Filter {
    
    private Object m_key;
    private ArrayList<AggregateItem> m_aggs = new ArrayList<AggregateItem>(50);
    
    public CommunityFilter(Object communityKey) {
        super(ItemRegistry.DEFAULT_AGGR_CLASS, true);
        m_key = communityKey;
    } //
    
    public void run(ItemRegistry registry, double frac) {
        FocusManager fman = registry.getFocusManager();
        CommunitySet comm = (CommunitySet)fman.getFocusSet(m_key);
        
        int num = comm.getCommunityCount();
        
        // filter the aggregates to use
        for ( int i=0; i<num; i++ ) {
        	Set<?> set = comm.getCommunityMembers(i);
        	m_aggs.add(getAggregate(registry, set));
        }
        
        // garbage collect the aggregates
        super.run(registry, frac);
        
        // fill out the aggregate mappings
        for ( int i=0; i<num; i++ ) {
            Set<?> set = comm.getCommunityMembers(i);
            AggregateItem aitem = (AggregateItem)m_aggs.get(i);
            
            Iterator<?> iter = set.iterator();
            while ( iter.hasNext() ) {
                Node node = (Node)iter.next();
                registry.addMapping(node,aitem);
            }
        }
        
        // clear the temporary list
        m_aggs.clear();
    } //
    
    protected AggregateItem getAggregate(ItemRegistry registry, Set<?> set) {
    	AggregateItem aitem = null;
    	boolean highlight = true;
    	
    	Node n = null;
    	Iterator<?> iter = set.iterator();
    	while ( iter.hasNext() ) {
    		n = (Node)iter.next();
    		aitem = registry.getAggregateItem(n);
    		if ( aitem != null && aitem.getDirty() > 0 ) {
    			highlight = aitem.isHighlighted();
    			break;
    		} else {
    			aitem = null;
    		}
    	}
    	if ( aitem == null ) {
    		aitem = registry.getAggregateItem(n, true);
    	}
    	
    	registry.removeMappings(aitem);
    	
    	float[] poly = (float[])aitem.getVizAttribute("polygon");
    	Paint color = aitem.getColor();
    	Paint fill  = aitem.getFillColor();
    	
    	aitem.init(registry, ItemRegistry.DEFAULT_AGGR_CLASS, n);
    	aitem.setVizAttribute("polygon", poly);
    	aitem.setColor(color);
    	aitem.setFillColor(fill);
    	aitem.setHighlighted(highlight);
    	aitem.setLocation(0,0);
        aitem.setAggregateSize(set.size());
        aitem.setInteractive(false);
        aitem.setVisible(true);
        return aitem;
    } //
    
} // end of class CommunityFilter
