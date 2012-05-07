/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;

import vizster.VizsterDBLoader;
import vizster.VizsterDisplay;
import vizster.Vizster;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.action.ActionMap;
import edu.berkeley.guir.prefuse.activity.ActionList;
import edu.berkeley.guir.prefuse.focus.DefaultFocusSet;
import edu.berkeley.guir.prefuse.graph.DefaultGraph;
import edu.berkeley.guir.prefuse.graph.Entity;
import edu.berkeley.guir.prefuse.graph.GraphLib;
import edu.berkeley.guir.prefuse.graph.event.GraphLoaderListener;
import edu.berkeley.guir.prefuse.graph.external.GraphLoader;
import edu.berkeley.guir.prefusex.controls.DragControl;
import edu.berkeley.guir.prefusex.controls.FocusControl;
import edu.berkeley.guir.prefusex.controls.PanControl;
import edu.berkeley.guir.prefusex.controls.ZoomControl;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import knowevo.springbox.CategoryScoreMachine;
import knowevo.springbox.Node;
import prefusex.community.CommunitySet;
import prefusex.lucene.TextSearchFocusSet;
import prefusex.lucene.TextSearchPanel;
import myvizster.render.VizsterRendererFactory;
import myvizster.ui.CommunityPanel;
import myvizster.ui.ProfilePanel;
import myvizster.ui.VizsterMenuBar;

/**
 *
 * @author gabrovski
 */
public class VizsterDrawer extends myvizster.Vizster {
    
    private static final int MAX_DEPTH = 1;
    
    public VizsterDrawer(String name) {
        super(name);
        
    }
    
    @Override
    public void loadGraph(String name, String bla) {
        
        VizsterDBBuilder vdbb = new VizsterDBBuilder(new CategoryScoreMachine());
        try {
            vdbb.buildGraph(name, MAX_DEPTH);
            vdbb.convertGraph();
            ItemRegistry registry = getRegistry();
            ActionList filter = getFilter();

            registry.setGraph(vdbb.getVGraph());

//            edu.berkeley.guir.prefuse.graph.Node r = 
//                    GraphLib.getMostConnectedNodes(registry.getGraph())[0]; 
//            
//            registry.getDefaultFocusSet().set(r);
//            registry.getFocusManager().getFocusSet(CLICK_KEY).set(r);
            //centerDisplay();

            filter.runNow();
            if (isAnimate()) {
                getForces().runNow();
            } else {
                runStaticLayout();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
