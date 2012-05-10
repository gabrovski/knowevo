package vizster.controls;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import vizster.VizsterLib;

import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.activity.Activity;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Entity;

/**
 * HighlightFreezeControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightFreezeControl extends ControlAdapter {

    private FocusSet focusSet;
    private Entity curFocus;
    private Object focusSetKey;
    private Activity activity;
    
    private TimerTask task;
    private long delay = 200L;
    
    public HighlightFreezeControl(Activity act, Object focusSetKey) {
        this.activity = act;
        this.focusSetKey = focusSetKey;
    } //
    
    public void setFocus(VisualItem item) {
        Entity focus = item.getEntity();
        if ( focus != curFocus ) {
            ItemRegistry registry = item.getItemRegistry();
	        FocusManager fm = registry.getFocusManager();
	        focusSet = fm.getFocusSet(focusSetKey);
	        curFocus = focus;
	        focusSet.set(focus);
	        registry.touch(item.getItemClass());
    	}
    } //
    
    public void clearFocus() {
        focusSet.clear();
        focusSet = null;
        curFocus = null;
    } //
    
    public void itemClicked(final VisualItem item, MouseEvent e) {
        if ( isAllowedType(item) && SwingUtilities.isLeftMouseButton(e) ) {
            if ( e.getClickCount() == 1 ) {
	        	task = new TimerTask() {
	        	    public void run() {
	        	        setFocus(item);
	        	        runActivity();
	        	    }
	        	};
	        	VizsterLib.getTimer().schedule(task,delay);
            } else if ( e.getClickCount() > 1 ) {
                if ( task != null)
                    task.cancel();
            }
        }
    } //
    
    public void mouseClicked(MouseEvent e) {
        if ( SwingUtilities.isLeftMouseButton(e) &&
             focusSet != null )
        {
            if ( task != null )
                task.cancel();
            clearFocus();
            runActivity();
        }
    } //
    
    private boolean isAllowedType(VisualItem item) {
        return item.getItemClass().equals(ItemRegistry.DEFAULT_NODE_CLASS);
    } //
    
    private void runActivity() {
        if ( activity != null ) {
            activity.runNow();
        }
    } //
    
} // end of class HighlightFreezeControl
