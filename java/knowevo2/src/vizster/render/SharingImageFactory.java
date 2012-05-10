package vizster.render;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

import edu.berkeley.guir.prefuse.render.ImageFactory;

/**
 * An image factory subclass, which upon loading an image
 *  adds it to an additional factory as well. This reduces image loading
 *  times by sharing data.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class SharingImageFactory extends ImageFactory {

    private DesaturateFilter dsat = new DesaturateFilter(150);
    private ImageFactory m_shared;
    private ImageFactory m_bwfactory1;
    private ImageFactory m_bwfactory2;
    
    public SharingImageFactory(ImageFactory f2, ImageFactory bwf1, ImageFactory bwf2) {
        m_shared = f2;
        m_bwfactory1 = bwf1;
        m_bwfactory2 = bwf2;
    } //
    
    public Image addImage(String location, Image image) {
        Image img = super.addImage(location, image);
        ImageProducer prod = new FilteredImageSource(img.getSource(), dsat);
        Image bwimage = Toolkit.getDefaultToolkit().createImage(prod);
        bwimage.getWidth(null);
        m_bwfactory1.addImage(location, bwimage);
        
        Image img2 = m_shared.addImage(location, image);
        prod = new FilteredImageSource(img2.getSource(), dsat);
        bwimage = Toolkit.getDefaultToolkit().createImage(prod);
        bwimage.getWidth(null);
        m_bwfactory2.addImage(location, bwimage);
        
        return img;
    } //
    
	public class DesaturateFilter extends RGBImageFilter {
        static final float CO_RedBandWeight = 0.2125f;
        static final float CO_GreenBandWeight = 0.7154f;
        static final float CO_BlueBandWeight = 0.0721f;
        private int lighten = 0;
        
        public DesaturateFilter() {
            this(0);
        } //
        
        public DesaturateFilter(int lighten) {
            this.lighten = lighten;
            canFilterIndexColorModel = false;
        } //

        public int filterRGB(int x, int y, int rgb) {
            int a = rgb & 0xff000000;
            float r = ((rgb & 0xff0000) >> 16);
            float g = ((rgb & 0x00ff00) >> 8);
            float b = (rgb & 0x0000ff);

            r *= CO_RedBandWeight;
            g *= CO_GreenBandWeight;
            b *= CO_BlueBandWeight;

            int gray = Math.min(((int)(r+g+b))+lighten,0xff);
            gray = gray & 0xff;

            return a | (gray << 16) | (gray << 8) | gray;
        } //
    } // end of inner class DesaturateFilter
    
} // end of class SharingImageFactory
