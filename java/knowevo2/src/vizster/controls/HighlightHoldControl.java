package vizster.controls;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import vizster.Vizster;
import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.activity.Activity;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Entity;

/**
 * MagnifyControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightHoldControl extends ControlAdapter {

    public static final int MOUSE_OVER_MODE     = 0;
    public static final int CLICK_MODE          = 1;
    public static final int CLICK_AND_HOLD_MODE = 2;
    
    private Vizster vizster;
    private long delay = 1000;
    
    private int mouseButton = MouseEvent.BUTTON1;
    private int mouseMask = MouseEvent.BUTTON1_DOWN_MASK;
    
    private int mode;
    private TimerTask task;
    
    private FocusSet focusSet;
    private Entity curFocus;
    private Object focusSetKey;
    private Activity activity;
    
    public HighlightHoldControl(Activity act, Object focusSetKey, Vizster vizster, int mode) {
        if ( mode < MOUSE_OVER_MODE || mode > CLICK_AND_HOLD_MODE )
            throw new IllegalArgumentException("Unrecognized mode.");
        this.vizster = vizster;
        this.mode = mode;
        this.activity = act;
        this.focusSetKey = focusSetKey;
    } //
    
    public void itemEntered(VisualItem vi, MouseEvent e) {
        if ( mode != MOUSE_OVER_MODE ) return;
        final VisualItem item = vi;
        task = new TimerTask() {
            public void run() {
                setFocus(item);
                setMagnify(true);
            }
        };
        VizsterLib.getTimer().schedule(task,delay);
    } //
    
    public void itemExited(VisualItem vi, MouseEvent e) {
        if ( mode != MOUSE_OVER_MODE ) return;
        task.cancel();
        setMagnify(false);
        clearFocus();
    } //
    
    public void itemDragged(VisualItem vi, MouseEvent e) {
        if ( (e.getModifiersEx() & mouseMask) == 0 ) return;
        if ( mode == CLICK_MODE ) return;
        task.cancel();
    } //
    
    public void itemPressed(VisualItem vi, MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode != CLICK_AND_HOLD_MODE ) return;
        final VisualItem item = vi;
        task = new TimerTask() {
            public void run() {
                setFocus(item);
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
        setFocus(vi);
    } //
    
    public void mouseReleased(MouseEvent e) {
        if ( e.getButton() != mouseButton ) return;
        if ( mode == MOUSE_OVER_MODE ) return;
        setMagnify(false);
        clearFocus();
    } //
    
    private void setMagnify(boolean state) {
        vizster.setMagnify(state);
    } //
    
    private void setFocus(VisualItem item) {
        Entity focus = item.getEntity();
        if ( focus != curFocus ) {
            ItemRegistry registry = item.getItemRegistry();
	        FocusManager fm = registry.getFocusManager();
	        focusSet = fm.getFocusSet(focusSetKey);
	        curFocus = focus;
	        focusSet.set(focus);
	        registry.touch(item.getItemClass());
    	}
        runActivity();
    } //
    
    private void clearFocus() {
        if ( focusSet != null ) {
            focusSet.clear();
            focusSet = null;
            curFocus = null;
            runActivity();
        }
    } //
    
    private void runActivity() {
        if ( activity != null ) {
            activity.runNow();
        }
    } //
    
} // end of class HighlightHoldControl
