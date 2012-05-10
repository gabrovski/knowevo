package vizster.controls;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import vizster.Vizster;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.Node;

/**
 * LinkageControl
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class LinkageControl extends ControlAdapter {

    private Vizster vizster;
    private ArrayList<Node> fixedList = new ArrayList<Node>();
    private boolean isEnabled = false;
    private boolean persist = false;
    private long down;
    private long thresh = 500L;
    
//    private TimerTask task;
//    private long delay = 500L;
    
    private NodeItem tempFocus = null;
    private FocusSet focusSet;
    
    public LinkageControl(Vizster vizster) {
        this.vizster = vizster;
        ItemRegistry registry = vizster.getItemRegistry();
        focusSet = registry.getFocusManager().getFocusSet(Vizster.CLICK_KEY);
    } //
    
    public void itemKeyPressed(VisualItem item, KeyEvent e) {
        if ( isSpaceBar(e) ) {
            if ( !isEnabled ) {
                isEnabled = true;
                down = System.currentTimeMillis();
                if ( item.getItemClass().equals(ItemRegistry.DEFAULT_NODE_CLASS) ) {
                    tempFocus = (NodeItem)item;
                    focusSet.add(tempFocus.getEntity());
                    vizster.setReferrer(tempFocus);
                }
                if ( !setFixed(vizster.getItemRegistry(), true) ) {
                    isEnabled = false;
                    return;
                }
                showLinkage();
            } else if ( persist ) {
               down = System.currentTimeMillis();
            }
        }
    } //

    public void itemKeyReleased(VisualItem item, KeyEvent e) {
        keyReleased(e);
    } //

    public void keyPressed(KeyEvent e) {
        if ( isSpaceBar(e) ) {
            if ( !isEnabled ) {
                isEnabled = true;
                down = System.currentTimeMillis();
                if ( !setFixed(vizster.getItemRegistry(), true) ) {
                    isEnabled = false;
                    return;
                }
                showLinkage();
            } else if ( persist ) {
               down = System.currentTimeMillis();
            }
        }
    } //

    public void keyReleased(KeyEvent e) {
        if ( isSpaceBar(e) && isEnabled ) {
            long up = System.currentTimeMillis();
            if ( persist || up-down > thresh ) {
                isEnabled = false;
                persist = false;
                if ( tempFocus != null ) {
                  // remove focus from focus set
                  focusSet.remove(tempFocus.getEntity());
                }
                hideLinkage();
                
//                task = new TimerTask() {
//                    public void run() {
//                        setFixed(vizster.getItemRegistry(), false);
//                    } //
//                };
//                VizsterLib.getTimer().schedule(task, delay);
                setFixed(vizster.getItemRegistry(), false);
                tempFocus = null;
          } else {
              persist = true;
          }
        }
    } //
    
    public void mouseClicked(MouseEvent e) {
        if ( isEnabled ) {
	        isEnabled = false;
	        persist = false;
	        if ( tempFocus != null ) {
	          // remove focus from focus set
	          focusSet.remove(tempFocus.getEntity());
	        }
	        hideLinkage();
	        setFixed(vizster.getItemRegistry(), false);
        }
    } //
    
    private boolean isSpaceBar(KeyEvent e) {
        return (e.getKeyCode() == KeyEvent.VK_SPACE);
    } //
    
    private void showLinkage() {
        vizster.setLinkageMode(true);
    } //
    
    private void hideLinkage() {
        vizster.setLinkageMode(false);
    } //
    
    private boolean setFixed(ItemRegistry registry, boolean state) {
        synchronized ( registry ) {
            FocusManager fman = registry.getFocusManager();
	        FocusSet click = fman.getFocusSet(Vizster.CLICK_KEY);
            Iterator<?> nodeIter;
            if ( state ) {
		        if (click.size() < 2)
		            return false;
				nodeIter = click.iterator();
            } else {
                if ( fixedList.isEmpty() )
                    return false;
                click.clear();
                nodeIter = fixedList.iterator();
            }
            while ( nodeIter.hasNext() ) {
			    Node n = (Node) nodeIter.next();
			    NodeItem nitem = registry.getNodeItem(n);
			    nitem.setFixed(state);
			    nitem.setWasFixed(state);
			    if ( state ) {
			        fixedList.add(n);
			    } else if ( !state && nitem != tempFocus ) {
			        click.add(n);
			    }
			}
            if ( !state ) {
	            FocusSet mouse = fman.getFocusSet(Vizster.MOUSE_KEY);
	            nodeIter = mouse.iterator();
	            while ( nodeIter.hasNext() ) {
	                Node n = (Node) nodeIter.next();
				    NodeItem nitem = registry.getNodeItem(n);
				    nitem.setFixed(false);
				    nitem.setWasFixed(false);
				    nitem.setFixed(true);
				    //if ( !nitem.isFixed() )
				    //    nitem.setFixed(true);
	            }
            }
        }
        if (!state)
            fixedList.clear();
        return true;
    } //
    
} // end of class LinkageControl
