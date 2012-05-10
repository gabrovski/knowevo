package vizster.ui;

import java.util.Iterator;

import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.assignment.SizeFunction;

/**
 * VizsterSizeFunction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class VizsterSizeFunction extends SizeFunction {

    private double scaleThresh = .75;
    private double targetScale = 1.05;
    private double scale = 1.0;
    private boolean magnify = false;
    
    public void run(ItemRegistry registry, double frac) {
        Display display = registry.getDisplay(0);
        double s = display.getScale();
        scale = ( s<scaleThresh ? targetScale/s : 1.0 );
        super.run(registry, frac);
        scale = 1.0;
        
        // set actual size to avoid list overlap
        Iterator<?> iter = registry.getItems();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            item.setSize(1.0);
        }
    } //

    public double getSize(VisualItem item) {
        double rval = (magnify && shouldScale(item) ? scale : 1.0);
        return rval;
    } //
    
    private boolean shouldScale(VisualItem item) {
        return item.isHighlighted();
    } //
    
    public boolean isMagnify() {
        return magnify;
    } //

    public void setMagnify(boolean magnify) {
        this.magnify = magnify;
    } //
    
    public double getScaleThresh() {
        return scaleThresh;
    } //

    public void setScaleThresh(double scaleThresh) {
        this.scaleThresh = scaleThresh;
    } //
    
} // end of class VizsterSizeFunction
