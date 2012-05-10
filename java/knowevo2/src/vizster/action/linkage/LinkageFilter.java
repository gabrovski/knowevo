package vizster.action.linkage;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.filter.Filter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Edge;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.util.display.DisplayLib;

/**
 * LinkageFilter
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class LinkageFilter extends Filter {

    private static final String[] types = new String[] {
        ItemRegistry.DEFAULT_NODE_CLASS, ItemRegistry.DEFAULT_EDGE_CLASS};
    
    private HashMap<Node, int[]> counts = new HashMap<Node, int[]>();
    private HashSet<Node> keepers = new HashSet<Node>();
    private ArrayList<Node> newlyFiltered = new ArrayList<Node>();    
    private boolean includeOthers = true;
    
    public LinkageFilter() {
        this(false);
    } //
    
    public LinkageFilter(boolean gc) {
        super(types, gc);
    } //
    
    public void run(ItemRegistry registry, double frac) {
        FocusManager fman = registry.getFocusManager();
        FocusSet focusSet = fman.getFocusSet(Vizster.CLICK_KEY);
        if ( focusSet.size() < 2 )
            return;
        
        Node clickNode = null;
        Graph fg = registry.getFilteredGraph();
        Iterator<?> iter;
        
        // for each focus, tally visitation counts for neighbors
        iter = focusSet.iterator();
        while ( iter.hasNext() ) {
            Node n = (Node)iter.next();
            if ( clickNode == null )
                clickNode = n;
            keepers.add(n);
            Iterator<?> niter = n.getNeighbors();
            while ( niter.hasNext() ) {
                Node nn = (Node)niter.next();
                int[] count = (int[])counts.get(nn);
                if ( count == null ) {
                    count = new int[] {1};
                    counts.put(nn,count);
                } else {
                    count[0]++;
                }
            }
        }

        // add nodes seen multiple times to keepers
        iter = counts.keySet().iterator();
        while ( iter.hasNext() ) {
            Node n = (Node)iter.next();
            int[] count = (int[])counts.get(n);
            if ( count[0] > 1 ) {
                keepers.add(n);
            }
        }

        // get size value for representative click node
        NodeItem clickItem = registry.getNodeItem(clickNode);
        double size = clickItem.getSize();
        
        // now filter the nodes and edges...
        iter = keepers.iterator();
        while ( iter.hasNext() ) {
            Node n = (Node)iter.next();
            NodeItem item = registry.getNodeItem(n,false);
            if ( item == null ) {
                item = registry.getNodeItem(n,true);
                newlyFiltered.add(item);
            }
            item.setSize(size);
            item.touch();
            item.setVisible(true);
           
            int hval = focusSet.contains(n) ? 0 : 1;
            VizsterLib.setHighlightValue(item, hval);
            item.setHighlighted(true);
            
            // highlight community
            VisualItem aitem = registry.getAggregateItem(n);
            if ( aitem != null ) {
                aitem.setHighlighted(true);
                registry.touch(aitem.getItemClass());
            }

            Iterator<?> eiter = n.getEdges();
            while ( eiter.hasNext() ) {
                Edge e = (Edge)eiter.next();
                Node nn = e.getAdjacentNode(n);
                if ( keepers.contains(nn) ) {
                    NodeItem nitem = registry.getNodeItem(nn,false);
                    if ( nitem == null ) {
                        nitem = registry.getNodeItem(nn,true);
                        newlyFiltered.add(nitem);
                    }
                    EdgeItem eitem = registry.getEdgeItem(e,false);
                    if ( eitem == null ) {
                        eitem = registry.getEdgeItem(e,true);
                        fg.addEdge(eitem);
                    }
                    eitem.touch();
                    eitem.setVisible(true);
                    VizsterLib.setHighlightValue(eitem,1);
                    eitem.setHighlighted(true);
                } else if ( includeOthers && registry.getNodeItem(nn) != null ) {
                    if ( registry.getEdgeItem(e,false) == null ) {
                        EdgeItem eitem = registry.getEdgeItem(e,true);
                        VizsterLib.setHighlightValue(eitem,-1);
                        fg.addEdge(eitem);
                    }
                }
            }
        }
        
        iter = newlyFiltered.iterator();
        while ( iter.hasNext() ) {
            NodeItem nitem = (NodeItem)iter.next();
            Point2D loc = nitem.getLocation();
            DisplayLib.getCentroid(registry, nitem.getNeighbors(), loc);
            nitem.updateLocation(loc.getX(),loc.getY());
        }
        
        counts.clear();
        keepers.clear();
        newlyFiltered.clear();
        
        super.run(registry, frac);
    } //
    
} // end of class LinkageFilter
