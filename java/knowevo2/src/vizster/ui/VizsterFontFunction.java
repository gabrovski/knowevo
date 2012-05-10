package vizster.ui;

import java.awt.Font;

import vizster.VizsterLib;

import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.assignment.FontFunction;
import edu.berkeley.guir.prefuse.util.FontLib;

/**
 * VizsterFontFunction
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class VizsterFontFunction extends FontFunction {

    private int m_fontSize;
    private String m_fontFamily = "SansSerif";
    private Font m_focusFont;
    
    public VizsterFontFunction() {
        this(11);
    } //
    
    public VizsterFontFunction(int size) {
        setFontSize(size);
    } //
    
    public Font getFont(VisualItem item) {
        if (item.isHighlighted() || item.getDOI() == 0) {
            int hvalue = VizsterLib.getHighlightValue(item);
            if ( hvalue == 0 || hvalue == 1 )
                return m_focusFont;
        }
        return defaultFont;
    } //
    
    public void setFontSize(int size) {
        m_fontSize = size;
        setDefaultFont(FontLib.getFont(m_fontFamily,Font.PLAIN,m_fontSize));
        m_focusFont = FontLib.getFont(m_fontFamily,Font.PLAIN,m_fontSize);
    } //
    
} // end of class FontFunction
