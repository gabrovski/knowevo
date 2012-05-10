package vizster.controls;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.event.ControlAdapter;

/**
 * MagnifyControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class MagnifyControl extends ControlAdapter {

    public static final int MOUSE_OVER_MODE     = 0;
    public static final int CLICK_MODE          = 1;
    public static final int CLICK_AND_HOLD_MODE = 2;
    
    private Vizster vizster;
    private long delay = 1000;
    
    private int mouseButton = MouseEvent.BUTTON1;
    private int mouseMask = MouseEvent.BUTTON1_DOWN_MASK;
    
    private int mode;
    private TimerTask task;
    
    public MagnifyControl(Vizster vizster, int mode) {
        if ( mode < MOUSE_OVER_MODE || mode > CLICK_AND_HOLD_MODE )
            throw new IllegalArgumentException("Unrecognized mode.");
        this.vizster = vizster;
        this.mode = mode;
    } //
    
    public void itemEntered(VisualItem vi, MouseEvent e) {
        if ( mode != MOUSE_OVER_MODE ) return;
        task = new TimerTask() {
            public void run() {
                setMagnify(true);
            }
        };
        VizsterLib.getTimer().schedule(task,delay);
    } //
    
    public void itemExited(VisualItem vi, MouseEvent e) {
        if ( mode != MOUSE_OVER_MODE ) return;
        task.cancel();
        setMagnify(false);
    } //
    
    public void itemDragged(VisualItem vi, MouseEvent e) {
        if ( (e.getModifiersEx() & mouseMask) == 0 ) return;
        if ( mode == CLICK_MODE ) return;
        task.cancel();
    } //
    
    public void itemPressed(VisualItem vi, MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode != CLICK_AND_HOLD_MODE ) return;
        task = new TimerTask() {
            public void run() {
                setMagnify(true);
            }
        };
        VizsterLib.getTimer().schedule(task,delay);
    } //
    
    public void itemReleased(VisualItem vi, MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode != CLICK_AND_HOLD_MODE ) return;
        task.cancel();
    } //
    
    public void itemClicked(VisualItem vi, MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode != CLICK_MODE ) return;
        setMagnify(true);
    } //
    
    public void mouseReleased(MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode == MOUSE_OVER_MODE ) return;
        setMagnify(false);
    } //
    
    private void setMagnify(boolean state) {
        vizster.setMagnify(state);
    } //
    
} // end of class MagnifyControl
