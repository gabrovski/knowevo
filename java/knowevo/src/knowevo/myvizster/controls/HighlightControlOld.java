package knowevo.myvizster.controls;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import knowevo.myvizster.VizsterLib;

import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.activity.Activity;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Edge;
import edu.berkeley.guir.prefuse.graph.Node;


/**
 * NeighborhoodHighlighter
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightControlOld extends ControlAdapter {

    private Activity update = null;
    private boolean highlightWithInvisibleEdge = false;
    private int hops = 1;
    private boolean enabled = true;
    
    /**
     * Creates a new highlight control.
     */
    public HighlightControlOld(int hops) {
        this(hops, null);
    } //
    
    /**
     * Creates a new highlight control that runs the given activity
     * whenever the neighbor highlight changes.
     * @param update the update Activity to run
     */
    public HighlightControlOld(int hops, Activity update) {
        this.hops = hops;
        this.update = update;
    } //
    
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (enabled && item instanceof NodeItem && item.getItemRegistry() != null)
            setNeighborHighlight((NodeItem)item, true);
    } //
    
    public void itemExited(VisualItem item, MouseEvent e) {
        if (enabled && item instanceof NodeItem && item.getItemRegistry() != null)
            clearHighlight(item);
    } //
    
    public void clearHighlight(VisualItem src) {
        ItemRegistry registry = src.getItemRegistry();
        int defaultValue = 0;
        Iterator iter = registry.getNodeItems();
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
        if ( update != null )
            update.runNow();
    } //
    
    public void setNeighborHighlight(NodeItem n, boolean state) {
        ItemRegistry registry = n.getItemRegistry();
        
        int defaultValue = state?-1:0;
        Iterator iter = registry.getNodeItems();
        while ( iter.hasNext() ) {
            VizsterLib.setHighlightValue((VisualItem)iter.next(),defaultValue);
        }
        iter = registry.getEdgeItems();
        while ( iter.hasNext() ) {
            VizsterLib.setHighlightValue((VisualItem)iter.next(),defaultValue);
        }
        
        Node nn = (Node)n.getEntity();
        FocusSet focusSet = registry.getDefaultFocusSet();
        synchronized ( registry ) {
            VizsterLib.setHighlightValue(n,0);
            n.setHighlighted(true);
            
            ArrayList queue  = new ArrayList();
            ArrayList queue2 = new ArrayList();
            HashSet visited = new HashSet();
            queue.add(nn);
            visited.add(nn);
            
            for ( int depth = 1; depth <= hops; depth++ ) {
	            for ( int i = queue.size()-1; i >= 0; i-- ) {
	                Node node = (Node)queue.remove(i);
	                
	                // don't trace paths through focus nodes
	                if ( depth > 1 && focusSet.contains(node) ) {
	                    continue;
	                }
	                
	                // process neighbors
	                iter = node.getEdges();
	                while ( iter.hasNext() ) {
	                    Edge edge = (Edge)iter.next();
	                    Node nnode = (Node)edge.getAdjacentNode(node);
	                    EdgeItem eitem = registry.getEdgeItem(edge);
	                    NodeItem nitem = registry.getNodeItem(nnode);
	                    
	                    boolean visit = (!visited.contains(nnode));
	                    if ( visit && nitem != null && nitem.isVisible() ) 
	                    {
	                        nitem.setHighlighted(state);
	                        VizsterLib.setHighlightValue(nitem,depth);
	                        registry.touch(nitem.getItemClass());
	                    }
	                    if ( visit && eitem != null && eitem.isVisible() )
	                    {
	                        eitem.setHighlighted(state);
	                        VizsterLib.setHighlightValue(eitem,depth);
	                        registry.touch(eitem.getItemClass());
	                    }
	                    // add node to next queue 
                        if ( visit && depth < hops ) {
                            queue2.add(nnode);
                            visited.add(nnode);
                        }
	                }
	            }
	            // swap queues
	            ArrayList tmp = queue;
	            queue = queue2;
	            queue2 = tmp;
            }
        }
        if ( update != null ) {
            update.runNow();
        }
    } //
    
    
    
    /**
     * Indicates if neighbor nodes with edges currently not visible still
     * get highlighted.
     * @return true if neighbors with invisible edges still get highlighted,
     * false otherwise.
     */
    public boolean isHighlightWithInvisibleEdge() {
        return highlightWithInvisibleEdge;
    } //
   
    /**
     * Determines if neighbor nodes with edges currently not visible still
     * get highlighted.
     * @param highlightWithInvisibleEdge assign true if neighbors with invisible
     * edges should still get highlighted, false otherwise.
     */
    public void setHighlightWithInvisibleEdge(boolean highlightWithInvisibleEdge) {
        this.highlightWithInvisibleEdge = highlightWithInvisibleEdge;
    } //
    
    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    } //
    
    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    } //
    
} // end of class NeighborhoodHighlighter
