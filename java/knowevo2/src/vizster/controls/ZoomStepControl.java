package vizster.controls;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.event.ControlAdapter;

/**
 * ZoomStepControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class ZoomStepControl extends ControlAdapter {

    private Vizster vizster;
    private TimerTask task;
    private long delay = 250L;
    private long down;
    
    private int mouseButton = MouseEvent.BUTTON3;
    private int mouseMask = MouseEvent.BUTTON3_DOWN_MASK;
    
    private boolean zoomIn = false;
    
    public ZoomStepControl(Vizster vizster) {
        this.vizster = vizster;
    } //
    
    public void mouseClicked(MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( zoomIn ) {
	        if ( task != null ) {
	            task.cancel();
	            task = null;
	        }
	        long now = System.currentTimeMillis();
	        if ( now-down < 200L )
	            vizster.resetDisplay();
        } else {
            vizster.resetDisplay();
        }
    } //

    public void mousePressed(final MouseEvent e) {
        if ( !zoomIn ) return;
        if ( e.getButton() != mouseButton ) return;
        down = System.currentTimeMillis();
        task = new TimerTask() {
            public void run() {
                Display d = vizster.getDisplay();
                double s = 0.99/d.getScale();
                d.animateZoom(e.getPoint(), s, 2000L);
                task = null;
            } //
        };
        VizsterLib.getTimer().schedule(task, delay);
    } //

    public void mouseReleased(MouseEvent e) {
        if ( !zoomIn ) return;
        if ( e.getButton() != mouseButton ) return;
        if ( task != null ) {
            task.cancel();
            task = null;
        }
    } //
    
    public void mouseDragged(MouseEvent e) {
        if ( !zoomIn ) return;
        if ( (e.getModifiersEx() & mouseMask) == 0 ) return;
        if ( task != null ) {
            task.cancel();
            task = null;
        }
    } //
    
} // end of class ZoomStepControl
