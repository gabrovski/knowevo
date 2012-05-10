package vizster.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import edu.berkeley.guir.prefuse.render.Renderer;
import edu.berkeley.guir.prefuse.util.ColorMap;

/**
 * Legend
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org</a>
 */
@SuppressWarnings("serial")
public class Legend extends JPanel {

    private String[] labels;
    private ColorMap cmap;
    
    private Font font = new Font("SansSerif",Font.PLAIN,11);
    private int height = -1;
    private int width = -1;
    private int size = 20;
    
    public Legend(String[] labels, ColorMap cmap) {
        this.cmap = cmap;
        if ( labels == null ) {
            double min = cmap.getMinValue();
            double max = cmap.getMaxValue();
            labels = new String[6];
            int len = labels.length;
            for ( int i=0; i<len; i++ ) {
                int val = (int)Math.round((i/((double)len-1))*(max-min)+min);
                labels[i] = String.valueOf(val);
            }
        }
        this.labels = labels;
    } //
    
    public void paint(Graphics2D g, Component c, int x, int y) {
        int height = labels.length;
        y += 5;
        
        double min = cmap.getMinValue();
        double max = cmap.getMaxValue();
        double len = (double)labels.length-1;
        
        FontMetrics fm = g.getFontMetrics(font);
        g.setFont(font);
        
        for ( int i=0; i<height; i++ ) {
            double v = ((len-i)/len)*(max-min)+min;
            g.setPaint(cmap.getColor(v));
            g.fillRect(x,y,size,size);
            g.drawString(labels[height-1-i],x+size+5,y+fm.getAscent());
            y += size;
        }
    } //
    
    public int getHeight() {
        if ( height == -1 ) {
            height = size*labels.length + 10;
        }
        return height;
    } //
    
    public int getWidth() {
        if ( width == -1 ) {
	        int mw = 0;
	        FontMetrics fm = Renderer.DEFAULT_GRAPHICS.getFontMetrics(font);
	        for ( int i=0; i<labels.length; i++ ) {
	            int w = fm.stringWidth(labels[i]);
	            if ( w > mw ) mw = w;
	        }
	        width = size+15 + mw;
        }
        return width; 
    } //
    
} // end of class Legend
