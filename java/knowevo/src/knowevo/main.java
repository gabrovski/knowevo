/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo;

import knowevo.articlerank.*;
import knowevo.springbox.CategoryScoreMachine;
import knowevo.springbox.CooccurenceScoreMachine;
import knowevo.springbox.gephibox.GephiDBBuilder;
import knowevo.springbox.gephibox.GraphServer;
import knowevo.springbox.vizster.VizsterDrawer;
import knowevo.springbox.vizster.VizsterRunner;
import vizster.Vizster;

/**
 *
 * @author gabrovski
 */
public class main {
    
    private static final int PORT = 62541;
    private static final int MAX_DEPTH = 1;
    private static final String PNGPATH = "/home/gabrovski/cs/knowevo/static/pngs/";
    private static final boolean PEERS_ONLY = true;
    
    
    public static void main(String args[]) {
        System.out.println("starting");
        try {
            if (args.length == 0) {
                //VizsterRunner.getGraphFor("Abraham Lincoln", 2, "tmp", PEERS_ONLY);
                GephiDBBuilder.getGraphFor(new CooccurenceScoreMachine(), "Aleister Crowley", MAX_DEPTH, "test.svg", PEERS_ONLY);
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
                try {
                    GraphServer.runServer(PORT, MAX_DEPTH, PNGPATH, PEERS_ONLY);
                    //DBBuilder.getGraphFor("Alan Turing", 1, "test.png");
                    //testUndirectedGraph();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
