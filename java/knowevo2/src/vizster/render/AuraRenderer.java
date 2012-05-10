package vizster.render;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import vizster.Vizster;
import vizster.ui.DecoratorItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.render.ShapeRenderer;

/**
 * AuraRenderer
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class AuraRenderer extends ShapeRenderer {

    protected Vizster vizster;
    protected RoundRectangle2D m_aura  = new RoundRectangle2D.Float();
    protected int m_corner = 10;
    protected int m_margin = 2;
    
    public AuraRenderer(Vizster vizster) {
        this.vizster = vizster;
        m_aura = new RoundRectangle2D.Float();
        m_aura.setRoundRect(0,0,10,10,m_corner,m_corner);
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.render.ShapeRenderer#getRawShape(edu.berkeley.guir.prefuse.VisualItem)
     */
    protected Shape getRawShape(VisualItem item) {
        if ( !(item.getItemClass().equals(Vizster.AURA_CLASS)) ) {
            throw new IllegalArgumentException(
                "VisualItem must be of item class "+Vizster.AURA_CLASS);
        }
        DecoratorItem ditem = (DecoratorItem)item;
        int margin = getMargin(ditem.getDecorated());
        Rectangle2D b = ditem.getDecorated().getBounds();
        m_aura.setFrame(b.getMinX()-margin, b.getMinY()-margin,
                		b.getWidth()+2*margin, b.getHeight()+2*margin);
        return m_aura;
    } //
    
    protected int getMargin(VisualItem item) {
        if (vizster.isXRayMode() || item.isHighlighted() || item.getDOI() == 0.0) {
            return m_margin+2;
        } else {
            return m_margin;
        }
    } //

} // end of class AuraRenderer
