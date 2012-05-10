package vizster.render;

import java.awt.BasicStroke;
import java.awt.Image;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Entity;
import edu.berkeley.guir.prefuse.render.ImageFactory;
import edu.berkeley.guir.prefuse.render.TextImageItemRenderer;

/**
 * Subclass of TextImageItemRenderer that allows control of image rendering.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterImageRenderer extends TextImageItemRenderer {

	private static final String PHOTO_FIELD = "photo";
	private static final String NAME_FIELD = "name";
	
    private BasicStroke  stroke1 = new BasicStroke(1);
    private BasicStroke  stroke2 = new BasicStroke(2);
    private ImageFactory m_bwimages;
    
    public VizsterImageRenderer() {
        this.setImageAttributeName(PHOTO_FIELD);
        this.setTextAttributeName(NAME_FIELD);
        //this.setTextAttributeName("label");
        //this.setImageAttributeName("image");
        m_bwimages = new ImageFactory();
        this.setRoundedCorner(8,8);
        this.setHorizontalPadding(4);
        this.setVerticalPadding(1);
    } //
    
    public ImageFactory getBlackAndWhiteImageFactory() {
        return m_bwimages;
    } //
    
    public int getRenderType(VisualItem item) {
        ItemRegistry registry = item.getItemRegistry();
        FocusManager fm = registry.getFocusManager();
        FocusSet focusSet = fm.getDefaultFocusSet();
        FocusSet clickSet = fm.getFocusSet(Vizster.CLICK_KEY);
        FocusSet mouseSet = fm.getFocusSet(Vizster.MOUSE_KEY);
        FocusSet searchSet = fm.getFocusSet(Vizster.SEARCH_KEY);
        Entity entity = item.getEntity();
        
        boolean fs = focusSet.contains(entity);
        boolean cs = clickSet.contains(entity);
        boolean ms = mouseSet.contains(entity);
        //boolean ss = searchSet.contains(entity);
        
        if ( ms || item.isHighlighted() ) {
//            Boolean val = (Boolean)item.getVizAttribute("border-off");
//            boolean borderOff = (val != null && val.booleanValue());
//            if ( borderOff ) {
//                return RENDER_TYPE_FILL;
//            } else {
                return RENDER_TYPE_DRAW_AND_FILL;
//            }
        } else if ( (fs || cs) && searchSet.size() > 0 ) {
            return RENDER_TYPE_DRAW;
        } else if ( fs || cs ) {
            return RENDER_TYPE_DRAW_AND_FILL;
        } else if ( item.isHighlighted() )
            return RENDER_TYPE_FILL;
        else
            return RENDER_TYPE_NONE;
    } //
    
    protected Image getImage(VisualItem item) {
	    if ( !m_showImages ) return null;
		String imageLoc = getImageLocation(item);
		int[] hvalue = (int[])item.getVizAttribute("highlightValue");
		int val = (hvalue == null ? 0 : hvalue[0]);
		ImageFactory ifact = (val >= 0 ? m_images : m_bwimages);
		return ( imageLoc == null ? null : ifact.getImage(imageLoc) );
	} //
    
    public BasicStroke getStroke(VisualItem item) {
        if ( item.isHighlighted() && !item.isFocus() &&
             VizsterLib.getHighlightValue(item) > 1 )
        {
            return stroke1;
        } else if ( item.getSize() > 1.0 ) {
            return new BasicStroke((int)Math.ceil(2*item.getSize()));
        } else {
            return stroke2;
        }
    } //
    
    public void setDrawImages(boolean s) {
        super.setShowImages(s);
    } //
    
    public void setMaxImageDimensions(int width, int height) {
        m_bwimages.setMaxImageDimensions(width, height);
        super.setMaxImageDimensions(width, height);
    } //
    
} // end of class VizsterImageRenderer
