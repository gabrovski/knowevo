package vizster.action.linkage;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import vizster.Vizster;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.action.assignment.Layout;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * VizsterCircleLayout
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class VizsterCircleLayout extends Layout {

    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(ItemRegistry registry, double frac) {
	    FocusSet fset = registry.getFocusManager().getFocusSet(Vizster.CLICK_KEY);
	    
	    int nn = fset.size();
	    
	    Rectangle2D r = super.getLayoutBounds(registry);	
		double height = r.getHeight();
		double width = r.getWidth();
		double cx = r.getCenterX();
		double cy = r.getCenterY();

		double radius = 0.45 * (height < width ? height : width);

		Iterator<?> nodeIter = fset.iterator();
		for (int i=0; nodeIter.hasNext(); i++) {
		    NodeItem n = registry.getNodeItem((Node)nodeIter.next());
		    if ( !n.isFixed() )
		        n.setFixed(true);
		    double angle = (2*Math.PI*i) / nn;
		    double x = Math.cos(angle)*radius + cx;
		    double y = Math.sin(angle)*radius + cy;
		    this.setLocation(n, null, x, y);
		}
	} //

} // end of class VizsterCircleLayout
