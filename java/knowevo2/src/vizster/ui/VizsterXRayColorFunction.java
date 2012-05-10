package vizster.ui;

import java.awt.Color;
import java.awt.Paint;
import java.sql.Date;

import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.assignment.ColorFunction;
import edu.berkeley.guir.prefuse.util.ColorLib;
import edu.berkeley.guir.prefuse.util.ColorMap;

/**
 * Color function used for Vizster's attribute comparison (X-Ray) mode.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterXRayColorFunction extends ColorFunction {

    // attributes to support
    // -- quantitative --
    //    "age"    (1-100)
    //    "nfriends"
    // -- dates (->quant) --
    //    "membersince"
    //    "lastlogin"
    //    "lastmod"
    // -- nominal --
    //    "gender" (M x F)
    //    "status" (S, M, OM, IAR, JHTH)
    // -- locations (->nom) --
    //    "location"
    //    "hometown"
    
//    private static final int QUANTITATIVE = 0;
//    private static final int NOMINAL      = 1;
    private static final int DATE         = 2;
    private static final int LOCATION     = 3;
    private static final int NFRIENDS     = 4;
    private static final int AGE          = 5;
    private static final int GENDER       = 6;
    private static final int STATUS       = 7;
    
    public static final String[] ATTRIBUTES = {
            "Number of Friends", "Gender", "Relationship Status", "Age", 
            "Location", "Hometown", "Member Since", "Last Login", "Last Update"
    };
    public static final String[] COLOR_MAPS = {
            "Hot Map", "Cool Map", "Grayscale Map"
    };
  
    private static final String[] attrs = {
            "nfriends", "gender", "status"//, "age", "location", "hometown",
            //"membersince", "lastlogin", "lastmod"
    };
    private static final int[] types = {
            NFRIENDS, GENDER, STATUS, AGE, LOCATION, LOCATION, DATE, DATE, DATE
    };
    
    public static final String[] STATS = {
            "Single", "Married", "Open Marriage", "In a Relationship",
            "Just Here To Help!"
    };
    
    public static final String[] GENDERS = {
            "Female", "Male" 
    };
    
    private static final int ONE_YEAR = 365*24*60*60*1000;
    private static final int MAX_FRIENDS = 50; //TODO: Make this dynamic based on number of friends
    private static final int MAX_AGE = 100;
    
    private Color femaleColor = ColorLib.getColor(255,125,125);
    private Color maleColor   = ColorLib.getColor(125,125,255);
    
    private ColorMap genderMap = new ColorMap(
        new Color[] { femaleColor, maleColor }, 0, 1);
    
    private ColorMap statusMap = new ColorMap(
        getStatusColorArray(),0,4);
    
    private Paint[] array = trimColorArray(ColorMap.getCoolMap(), 0.2, true);
    
    private ColorMap nfriendsMap = new ColorMap(array,0,MAX_FRIENDS);
    
    private ColorMap hotMap = new ColorMap(ColorMap.getHotMap(),-.4,1.4);
    private ColorMap coolMap = new ColorMap(ColorMap.getCoolMap(),-.4,1.4);
    private ColorMap grayMap = new ColorMap(ColorMap.getGrayscaleMap(),-.4,1.4);
    private ColorMap[] maps = new ColorMap[] {grayMap, hotMap, coolMap};
    private ColorMap curMap = coolMap;
    
    private int curAttr = 6;
    
    //== other colors =========================================================
//    private Color grayOut        = ColorLib.getColor(225,225,225);
    private Color invisible      = ColorLib.getColor(0,0,0,0);
    private Color auraColor      = ColorLib.getColor(127,137,164,125);
    private Color fadedAura      = ColorLib.getColor(177,187,214,125);
    private Color nullColor      = Color.BLACK;
    private Color darkEdge       = Color.DARK_GRAY.darker();
    //=========================================================================
    
    
    public void setCurrentAttribute(int attr) {
        if ( attr < 0 || attr > ATTRIBUTES.length )
            throw new IllegalArgumentException();
        curAttr = attr;
    } //
    
    public void setColorMap(int map) {
        if ( map < 0 || map > COLOR_MAPS.length )
            throw new IllegalArgumentException();
        curMap = maps[map];
    } //
    
    public Paint getColor(VisualItem item) {
        if ( item instanceof AggregateItem ) {
            return Color.LIGHT_GRAY;
        } else if ( item instanceof DecoratorItem ) {
            return auraColor;
        } else if ( item instanceof EdgeItem ) {
            int hvalue = VizsterLib.getHighlightValue(item);
            if ( hvalue < 0 ) {
                return darkEdge;
            } else if ( hvalue > 0 ) {
                return Color.GRAY;
            } else {
                return Color.DARK_GRAY;
            }
        } else {
            int hvalue = VizsterLib.getHighlightValue(item);
            if ( hvalue < 0 ) {
                return Color.GRAY;
            } else {
                return Color.WHITE;
            }
        }
    } //
    
    public Paint getFillColor(VisualItem item) {
        if ( item instanceof AggregateItem ) {
            return invisible;
        } else if ( item instanceof DecoratorItem ) {
            return fadedAura;
        } else {
            int hvalue = VizsterLib.getHighlightValue(item);
            if ( hvalue < 0 ) {
                return Color.BLACK;
            } else {
                return getAttributeColor(item);
            }
        }
    } //            
    
    protected Paint getAttributeColor(VisualItem item) {
        String val = item.getAttribute(attrs[curAttr]);
        if ( val == null )
            return nullColor;
        
        switch ( types[curAttr] ) {
        case DATE: {
            // get color for timespan
            Date d = Date.valueOf(val);
            long time = d.getTime();
            if ( attrs[curAttr].equals("lastmod") ) {
                time = System.currentTimeMillis()-time;
            } else {
                Date lm = Date.valueOf(item.getAttribute("lastmod"));
                time = lm.getTime()-time;
            }
            double t = ((double)time) / ONE_YEAR;
            return curMap.getColor(1.0-Math.min(1.0,t));
        } case NFRIENDS: {
            // get color for number of friends
            int nfriends = Integer.parseInt(val);
            return nfriendsMap.getColor(nfriends);
            //return curMap.getColor(Math.min(1.0, ((double)nfriends)/MAX_FRIENDS));
        } case AGE: {
            // get color for age
            int age = Integer.parseInt(val);
            return curMap.getColor(Math.min(1.0, ((double)age)/MAX_AGE));
        } case GENDER: {
            // get color for gender
            double v = GENDERS[1].equalsIgnoreCase(val) ? 1.0 : 0.0;
            return genderMap.getColor(v);
        } case STATUS: {
            // get color for relationship status
            double v = getStatusValue(val);
            return statusMap.getColor(v);
        } case LOCATION:
            // get color for location
            return Color.BLACK;
        }
        return nullColor;
    } //
    
    private double getStatusValue(String status) {
        for ( int i=0; i<STATS.length; i++ ) {
            if ( STATS[i].equals(status) )
                return i;
        }
        return -1;
    } //

    public static int getAttributeIndex(String string) {
        for ( int i=0; i < attrs.length; i++ ) {
            if ( attrs[i].equals(string) )
                return i;
        }
        return -1;
    } //
   
    public String[] getLabels() {
        switch ( types[curAttr] ) {
        case GENDER: {
            return GENDERS;
        } case STATUS: {
            return STATS;
        } default:
            return null;
        }
    } //
    
    public ColorMap getColorMap() {
        switch ( types[curAttr] ) {
        case GENDER: {
            return genderMap;
        } case STATUS: {
            return statusMap;
        } case NFRIENDS: {
            return nfriendsMap;
        } default:
            return curMap;
        }
    } //

    private static Paint[] trimColorArray(Paint[] map, double trimfrac, boolean bothEnds) {
        // 0.4 1.0 0.4 = 1.8
        int trimlen = (int)(trimfrac*map.length);
        int scalar = bothEnds ? 2 : 1;
        Paint[] array = new Paint[map.length-scalar*trimlen];
        System.arraycopy(map, (bothEnds?trimlen:0), array, 0, array.length);
        return array;
    } //
    
    private Paint[] getStatusColorArray() {
        Paint[] p = ColorMap.getHSBMap(5,0.5f,0.8f);
        p[4] = Color.GRAY;
        return p;
    } //
    
} // end of class VizsterXRayColorFunction
