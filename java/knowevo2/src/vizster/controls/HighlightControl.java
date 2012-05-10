package vizster.controls;

import java.awt.event.MouseEvent;
import java.util.TimerTask;

import vizster.VizsterLib;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.activity.Activity;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;

/**
 * HighlightControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class HighlightControl extends ControlAdapter {

    private Object   focusKey;
    private Activity activity;
    
    private TimerTask task;
    private long delay = 500;
    
    public HighlightControl(Activity act, Object key) {
        this.activity = act;
        this.focusKey = key;
    } //
    
    public void itemEntered(VisualItem vi, MouseEvent e) {
        if ( !shouldProcess(vi) ) return;
        
        if ( task != null )
            task.cancel();
        
        ItemRegistry registry = vi.getItemRegistry();
        synchronized ( registry ) {
            FocusManager fman = registry.getFocusManager();
            FocusSet fset = fman.getFocusSet(focusKey);
            fset.set(vi.getEntity());
            registry.touch(vi.getItemClass());
        }
        runActivity();
    } //

    public void itemExited(VisualItem vi, MouseEvent e) {
        if ( !shouldProcess(vi) ) return;
        
        ItemRegistry registry = vi.getItemRegistry();
        if ( registry != null ) {
            FocusManager fman = registry.getFocusManager();
            FocusSet fset = fman.getFocusSet(focusKey);
            fset.remove(vi.getEntity());
            registry.touch(vi.getItemClass());
        }
        task = new TimerTask() {
            public void run() {
                runActivity();
            } //
        };
        VizsterLib.getTimer().schedule(task, delay);
    } //
    
    private boolean shouldProcess(VisualItem item) {
        return item.getItemClass().equals(ItemRegistry.DEFAULT_NODE_CLASS);
    } //
    
    private void runActivity() {
        if ( activity != null ) {
            activity.runNow();
        }
    } //
    
} // end of class HighlightControl
