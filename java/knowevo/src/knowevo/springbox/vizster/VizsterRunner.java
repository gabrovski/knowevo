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
    
    
    public static void getGraphFor(String name, int max_depth, String tmp, boolean peers_only) {
        try {
            VizsterDBBuilder vd = new VizsterDBBuilder(new CategoryScoreMachine(), tmp);
            vd.buildGraph(name, max_depth, peers_only);
            vd.convertGraph();
            
            VizsterApplet vdr = new VizsterApplet();
            vdr.init();
            //vdr.getVizsterFrame().loadGraph(tmp, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
