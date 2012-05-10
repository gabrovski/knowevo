package vizster.render;

import vizster.Vizster;
import vizster.ui.DecoratorItem;
import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.render.ImageFactory;
import edu.berkeley.guir.prefuse.render.PolygonRenderer;
import edu.berkeley.guir.prefuse.render.Renderer;
import edu.berkeley.guir.prefuse.render.RendererFactory;
import edu.berkeley.guir.prefuse.render.ShapeRenderer;

/**
 * Provides renderers for the Vizster application. This factory supports
 * semantic zooming, updating image resolutions based on the current
 * zoom level, and two different modes - one for general browsing and
 * one for comparing node attributes.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterRendererFactory implements RendererFactory {

    private double scaleThreshold = 2;
    
    private Vizster vizster;
    private Display display;
    
    private VizsterImageRenderer imageRenderer;
    private VizsterImageRenderer imageRenderer2;
    private VizsterImageRenderer compareRenderer;
    private Renderer edgeRenderer;
    private PolygonRenderer polyRenderer;
    private AuraRenderer auraRenderer;
    
    public VizsterRendererFactory(Vizster vizster) {
        this.vizster = vizster;
        this.display = vizster.getDisplay();
        
        imageRenderer2 = new VizsterImageRenderer();
        imageRenderer2.setMaxImageDimensions(150,150);
        imageRenderer2.setImageSize(0.2);
        imageRenderer2.setHorizontalPadding(2);
        
        imageRenderer = new VizsterImageRenderer();
        
        ImageFactory bw1 = imageRenderer.getBlackAndWhiteImageFactory();
        ImageFactory if2 = imageRenderer2.getImageFactory();
        ImageFactory bw2 = imageRenderer2.getBlackAndWhiteImageFactory();
        
        imageRenderer.setImageFactory(new SharingImageFactory(if2,bw1,bw2));
        imageRenderer.setMaxImageDimensions(30,30);
        imageRenderer.setHorizontalPadding(2);
        
        compareRenderer = new VizsterImageRenderer() {
            public int getRenderType(VisualItem item) {
                int rt = super.getRenderType(item);
                if ( rt == RENDER_TYPE_NONE || rt == RENDER_TYPE_DRAW ) {
                    rt = RENDER_TYPE_FILL;
                }
                return rt;
            } //
        };
        compareRenderer.setDrawImages(false);
        compareRenderer.setRoundedCorner(8,8);
        compareRenderer.setHorizontalPadding(2);
        
        edgeRenderer = new VizsterEdgeRenderer();
        polyRenderer = new PolygonRenderer(PolygonRenderer.EDGE_CURVE);
        auraRenderer = new AuraRenderer(vizster);
    } //
    
    public void setScaleThreshold(double scale) {
        scaleThreshold = scale;
    } //
    
    /**
     * @see edu.berkeley.guir.prefuse.render.RendererFactory#getRenderer(edu.berkeley.guir.prefuse.VisualItem)
     */
    public Renderer getRenderer(VisualItem item) {
        if ( item instanceof DecoratorItem ) {
            return auraRenderer;
        } else if ( item instanceof AggregateItem ) {
            if ( vizster.isXRayMode() ) {
                polyRenderer.setRenderType(ShapeRenderer.RENDER_TYPE_DRAW);
            } else {
                polyRenderer.setRenderType(ShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
            }
            return polyRenderer;
        } else if ( item instanceof EdgeItem ) {
            return edgeRenderer;
        } else if ( item instanceof NodeItem ) {
            if ( vizster.isXRayMode() ) {
                return compareRenderer;
            } else {
                double scale = display.getScale();
                if ( scale >= scaleThreshold ) {
                    return imageRenderer2;
                } else {
                    return imageRenderer;
                }
            }
        } else {
            return null;
        }
    } //

    public void setDrawImages(boolean s) {
        imageRenderer.setDrawImages(s);
    } //
    
} // end of class VizsterRendererFactory
