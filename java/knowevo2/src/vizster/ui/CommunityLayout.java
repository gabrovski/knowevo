package vizster.ui;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;

import prefusex.community.CommunitySet;
import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.util.GeometryLib;

/**
 * CommunityLayout
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class CommunityLayout extends AbstractAction {

    private Object m_key;
    
    // buffer for computing convex hulls
    private double[] m_pts;
    
    public CommunityLayout(Object communityKey) {
        m_key = communityKey;
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
        FocusManager fman = registry.getFocusManager();
        CommunitySet comm = (CommunitySet)fman.getFocusSet(m_key);
        
        // do we have any communities to process?
        int num = comm.getCommunityCount();
        if ( num == 0 ) return;
        
        // update buffers
        int maxsz = 0;
        for ( int i=0; i<num; i++ )
            maxsz = Math.max(maxsz, 4*2*comm.getCommunityMembers(i).size());
        if ( m_pts == null || maxsz > m_pts.length ) {
            m_pts    = new double[maxsz];
        }
        
        int growth = 5;
        for ( int i=0; i<num; i++ ) {
            
            AggregateItem aitem = null;
            
            int idx = 0;
            Set<?> set = comm.getCommunityMembers(i);
            Node node = null;
            Iterator<?> iter = set.iterator();
            while ( iter.hasNext() ) {
                node = (Node)iter.next();
                NodeItem item = registry.getNodeItem(node);
                if ( item != null ) {
                    addPoint(m_pts,idx,item,growth);
                    idx += 2*4;
                }
            }
            aitem = registry.getAggregateItem(node);
            
            // if no community members are visible, do nothing
            if ( idx == 0 ) continue;
            
            // compute convex hull
            double[] nhull = GeometryLib.convexHull(m_pts, idx);
            
            // prepare viz attribute array
            float[]  fhull = (float[])aitem.getVizAttribute("polygon");
            if ( fhull == null || fhull.length < nhull.length )
                fhull = new float[nhull.length];
            else if ( fhull.length > nhull.length )
                fhull[nhull.length] = Float.NaN;
            
            // copy hull values
            for ( int j=0; j<nhull.length; j++ )
                fhull[j] = (float)nhull[j];
            aitem.setVizAttribute("polygon", fhull);
        }
    }
    
    protected void addPoint(double[] pts, int idx, VisualItem item, int growth) {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
        double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
        pts[idx]   = minX; pts[idx+1] = minY;
        pts[idx+2] = minX; pts[idx+3] = maxY;
        pts[idx+4] = maxX; pts[idx+5] = minY;
        pts[idx+6] = maxX; pts[idx+7] = maxY;
    } //
    
} // end of class CommunityLayout
