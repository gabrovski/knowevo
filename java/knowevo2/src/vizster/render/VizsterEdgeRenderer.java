package vizster.render;

import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.render.DefaultEdgeRenderer;

/**
 * Vizster edge renderer, toggles edge width based on highlighted status
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterEdgeRenderer extends DefaultEdgeRenderer {

    public int getLineWidth(VisualItem item) {
        if ( item.isHighlighted() ) {
            int hval = VizsterLib.getHighlightValue(item);
            if ( hval == 0 || hval == 1 ) {
                return m_width*2;
            }
        }
        return m_width;
    } //
    
} // end of class VizsterEdgeRenderer
