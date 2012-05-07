/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;

import knowevo.springbox.CategoryScoreMachine;

/**
 *
 * @author gabrovski
 */
public class VizsterRunner {
    
    
    public static void getGraphFor(String name, int max_depth, String tmp) {
        try {
            VizsterDBBuilder vd = new VizsterDBBuilder(new CategoryScoreMachine(), tmp);
            vd.buildGraph(name, max_depth);
            vd.convertGraph();
            
            VizsterDrawer vdr = new VizsterDrawer(name);
            vdr.loadGraph(tmp, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
