/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo;

import knowevo.articlerank.*;
import knowevo.springbox.CategoryScoreMachine;
import knowevo.springbox.gephibox.GephiDBBuilder;
import knowevo.springbox.vizster.VizsterDrawer;
import knowevo.springbox.vizster.VizsterRunner;
import vizster.Vizster;

/**
 *
 * @author gabrovski
 */
public class main {
    
    public static void main(String args[]) {
        System.out.println("starting");
        try {
            if (args.length == 0) {
            VizsterRunner.getGraphFor("Abraham Lincoln", 2, "tmp");
            }
            
            else if (args[0].equals("ranker")) {
                Graph g = Ranker.buildSeeAlsoGraph(args[1]);
                System.out.println("Graph built");

                g.populateBackEdges();
                System.out.println("back edges populated");

                g.calculateIterRank(100000, 0.85, 100);
                g.saveGraph(args[2]);
            }
            else if (args[0].equals("server")) {
                
            }
            else {
                //args[-1] = null;//trigger error, too lazy to fix
                
            }
        }
        catch (Exception e)  {
            e.printStackTrace();
            //Vizster v = new Vizster("132132", "/home/gabrovski/downloads/vizster.sample.xml");
            System.out.println("usage: \n\tranker input output\n\t server");
        }
    }
}
