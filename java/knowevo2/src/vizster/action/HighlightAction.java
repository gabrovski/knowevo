package vizster.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Edge;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * HighlightAction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightAction extends AbstractAction {

    private ArrayList<Node>  queue = new ArrayList<Node>();
    private ArrayList<Node> queue2 = new ArrayList<Node>();
    private HashSet<Node>  visited = new HashSet<Node>();
    
    private int hops = 2;
    private boolean showEdges;
    private boolean skipFoci;
    
    public HighlightAction() {
        this(2);
    } //
    
    public HighlightAction(int hops) {
        this(2,true,true);
    } //
    
    public HighlightAction(int hops, boolean showEdges, boolean skipFoci) {
        this.hops = hops;
        this.showEdges = showEdges;
        this.skipFoci = skipFoci;
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
        Node nn = null;
        NodeItem n = null;
        FocusManager fman = registry.getFocusManager();
        FocusSet highlight = fman.getFocusSet(Vizster.HIGHLIGHT_KEY);
        FocusSet mouseover = fman.getFocusSet(Vizster.MOUSE_KEY);
        FocusSet search    = fman.getFocusSet(Vizster.SEARCH_KEY);
        FocusSet fs;
        
        synchronized ( highlight ) {
            synchronized ( mouseover ) {
                fs = ( highlight.size() > 0 ? highlight : mouseover );
                if ( fs.size() > 0 ) {
                    nn = (Node)fs.iterator().next();
                    n = registry.getNodeItem(nn);
                }
            }
        }
        
        boolean restore = (n==null && search.size()==0);
        int defaultValue = (restore || search.size()==0 ? 0 : -1);
        Iterator<?> iter = registry.getNodeItems();
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
            item.setHighlighted(restore);
        }
        
        if ( restore ) {
            // do nothing more if highlight node was not found
            return;
        } else if ( n == null ) {
            // process search items only
            iter = search.iterator();
            while ( iter.hasNext() ) {
                Node node = (Node)iter.next();
                NodeItem nitem = registry.getNodeItem(node);
                VizsterLib.setHighlightValue(nitem,0);
                VisualItem aitem = registry.getAggregateItem(node);
                if ( aitem != null ) {
                    aitem.setHighlighted(true);
                    registry.touch(aitem.getItemClass());
                }
            }
            return;
        }
            
        FocusSet focusSet = registry.getDefaultFocusSet();
        VizsterLib.setHighlightValue(n,0);
        n.setHighlighted(true);
        
        queue.clear();
        queue2.clear();
        visited.clear();
        
        queue.add(nn);
        visited.add(nn);
        
        for ( int depth = 1; depth <= hops; depth++ ) {
            for ( int i = queue.size()-1; i >= 0; i-- ) {
                Node node = (Node)queue.remove(i);
                
                // don't trace paths through focus nodes
                if ( skipFoci && depth > 1 && focusSet.contains(node) ) {
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
                        nitem.setHighlighted(true);
                        VizsterLib.setHighlightValue(nitem,depth);
                        VisualItem aitem = registry.getAggregateItem(nitem.getEntity());
                        if ( aitem != null ) {
                            aitem.setHighlighted(true);
                            registry.touch(aitem.getItemClass());
                        }
                        registry.touch(nitem.getItemClass());
                    }
                    if ( visit && eitem != null && eitem.isVisible() )
                    {
                        if ( showEdges ) {
                            eitem.setHighlighted(true);
                            VizsterLib.setHighlightValue(eitem,depth);
                            registry.touch(eitem.getItemClass());
                        } else {
                            VizsterLib.setHighlightValue(eitem,0);
                        }
                    }
                    // add node to next queue 
                    if ( visit && depth < hops ) {
                        queue2.add(nnode);
                        visited.add(nnode);
                    }
                }
            }
            // swap queues
            ArrayList<Node> tmp = queue;
            queue = queue2;
            queue2 = tmp;
        }
    } //

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        if ( hops < 0 || hops > 2 ) {
            throw new IllegalArgumentException("Hops value must be between 0 and 2");
        }
        this.hops = hops;
    } //
    
    public boolean isShowEdges() {
        return showEdges;
    } //

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
    } //

    public boolean isSkipFoci() {
        return skipFoci;
    } //

    public void setSkipFoci(boolean skipFoci) {
        this.skipFoci = skipFoci;
    } //
    
} // end of class HighlightAction
