package vizster.ui;

import java.awt.Color;
import java.awt.Paint;

import prefusex.community.CommunitySet;
import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.assignment.ColorFunction;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.util.ColorLib;
import edu.berkeley.guir.prefuse.util.ColorMap;

/**
 * Color function used for Vizster's browsing mode
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterBrowsingColorFunction extends ColorFunction {

    private FocusSet mouseSet;
    private FocusSet profileSet;
    private FocusSet searchSet;
    private CommunitySet communitySet;
    
//    private Color mouseColor     = ColorLib.getColor(255,125,125);
    private Color focusColor     = ColorLib.getColor(200,0,0);
//    private Color edgeHighlight  = ColorLib.getColor(255,200,125);
//    private Color defaultColor   = ColorLib.getColor(220,220,255);
//    private Color fixedColor     = ColorLib.getColor(245,200,245);
//    private Color searchColor    = ColorLib.getColor(125,125,255);
    
//    private Color auraColor      = ColorLib.getColor(125,125,255,125);
//    private Color fadedAura      = ColorLib.getColor(220,220,255,125);
//    private Color auraColor      = ColorLib.getColor(127,137,164,125);
    
//    private Color fadedAura      = ColorLib.getColor(255,255,150,125);
//    private Color auraColor      = fadedAura.darker();
    
//    private Color highlight1     = ColorLib.getColor(200,0,0);
//    private Color highlight2     = ColorLib.getColor(255,200,125);
//    private Color highlight3     = ColorLib.getColor(255,255,150);
    
//    private Color highlight1     = ColorLib.getColor(255,125,125);
//    private Color highlight2     = ColorLib.getColor(255,200,125);
//    private Color highlight3     = ColorLib.getColor(255,200,200);    
    
//    private Color highlight0     = ColorLib.getColor(255,117,77);
    private Color highlight1     = ColorLib.getColor(255,142,102);
    private Color highlight2     = ColorLib.getColor(255,192,152);
    private Color highlight3     = ColorLib.getColor(255,242,202);
//    private Color mouseColor     = highlight1;
    
    private Color border1 = highlight1.darker();
    private Color border2 = highlight1.darker();
    private Color border3 = highlight1.darker();
    
    private Color grayOut        = ColorLib.getColor(225,225,225); //200,125);
    private Color invisible      = ColorLib.getColor(243,243,243); //230,125);
    
    private ColorMap communityColor;
    private ColorMap fadedCommColor;
    
    private ColorMap highlightMap = 
        new ColorMap(new Paint[] {highlight1,highlight2,highlight3},0,2);
    private ColorMap borderMap = 
        new ColorMap(new Paint[] {border1,border2,border3},0,2);
    
    public void updateCommunityMap(CommunitySet community) {
        if ( community == null || community.size() == 0 ) {
            communityColor = null;
            fadedCommColor = null;
        } else {
        	int maxc = community.getMaxCommunityCount();
            communityColor = new ColorMap(
            	ColorMap.getCategoryMap(maxc, 0.6f/*0.4f*/, 0.4f, 1.f, 125),
                0, maxc-1);
            fadedCommColor = new ColorMap(
            	ColorMap.getCategoryMap(maxc, 0.3f, 0.2f, 1.f, 125),
                0, maxc-1);
            System.out.println("max comm count = "+community.getMaxCommunityCount());
        }
    } //
    
    public void run(ItemRegistry registry, double frac) {
        FocusManager fmanager = registry.getFocusManager();
        mouseSet = fmanager.getFocusSet(Vizster.MOUSE_KEY);
        profileSet = fmanager.getFocusSet(Vizster.CLICK_KEY);
        searchSet = fmanager.getFocusSet(Vizster.SEARCH_KEY);
        communitySet = (CommunitySet)fmanager.getFocusSet(Vizster.COMMUNITY_KEY);
        
        super.run(registry, frac);
        
        mouseSet = null;
        searchSet = null;
        communitySet = null;
    } //
    
    public Paint getColor(VisualItem item) {
        if ( item instanceof DecoratorItem ) {
            //return auraColor;
            //return ColorLib.getColor(0,0,0,0);
            return ColorLib.getColor(127,137,164,125);
        } else if ( item instanceof AggregateItem ) {
            return Color.WHITE;
        } else if ( profileSet.contains(item.getEntity()) ) {
            return focusColor;
        }
        int hvalue = VizsterLib.getHighlightValue(item);
        if ( item.isHighlighted() && hvalue > 0 ) {
            Boolean val = (Boolean)item.getVizAttribute("invert");
            boolean invert = (val != null && val.booleanValue());
            ColorMap cmap = invert ? highlightMap : borderMap;
            return cmap.getColor(hvalue);
        } else if ( hvalue < 0 && searchSet.contains(item.getEntity()) ) {
          return ColorLib.getColor(127,137,164,125);
        } else if ( hvalue < 0 && !mouseSet.contains(item.getEntity()) ) {
            return (item instanceof EdgeItem ? invisible : grayOut);
        } else if ( item instanceof EdgeItem ) {
            return Color.LIGHT_GRAY;
        }  else {
            return Color.BLACK;
        }
    } //
    
    public Paint getFillColor(VisualItem item) {
        if ( item instanceof DecoratorItem ) {
            VisualItem dec = ((DecoratorItem)item).getDecorated();
            if ( VizsterLib.getHighlightValue(dec) < 0 ) {
                //return fadedAura;
                return ColorLib.getColor(220,220,255,125);
            } else {
                //return ColorLib.getColor(127,137,164,125);
                return ColorLib.getColor(127,137,164,125);
            }
            //} else {
            //    return auraColor;
            //}
        } else if ( item instanceof AggregateItem ) {
            if ( communityColor == null ) {
                return null;
            } else {
                ColorMap cmap = (item.isHighlighted() ? communityColor : fadedCommColor);
                int idx = communitySet.getCommunity((Node)item.getEntity());
                return cmap.getColor(idx);
            }
        } 
//        else if ( mouseSet.contains(item.getEntity()) ) {
//            return mouseColor;
//        }
        
        int hvalue = VizsterLib.getHighlightValue(item);
        if ( item.isHighlighted() && hvalue >= 0 ) {
            Boolean val = (Boolean)item.getVizAttribute("invert");
            boolean invert = (val != null && val.booleanValue());
            ColorMap cmap = invert ? borderMap : highlightMap;
            return cmap.getColor(hvalue);
            //return highlightMap.getColor(hvalue);
//        } else if ( hvalue < 0 ) {
//            return grayOut;
        } else {
            return Color.WHITE;
        }
    } //

} // end of class VizsterBrowsingColorFunction
