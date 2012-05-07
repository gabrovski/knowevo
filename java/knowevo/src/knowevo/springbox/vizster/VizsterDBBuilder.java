/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;

import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.activity.ActionList;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.io.XMLGraphReader;
import knowevo.springbox.DBBuilder;
import knowevo.springbox.Node;
import knowevo.springbox.Edge;
import knowevo.springbox.ScoreMachine;

import java.io.*;
import java.sql.SQLException;
import knowevo.springbox.*;

/**
 *
 * @author gabrovski
 */
public class VizsterDBBuilder extends DBBuilder {
    
    private PipedOutputStream pos;
    private PipedInputStream pis;
    private OutputStreamWriter osw;
    
    private Graph vizsterGraph;
    
    
    public VizsterDBBuilder(ScoreMachine sm) {
        super(sm);
        try {
            pos = new PipedOutputStream();
            pis = new PipedInputStream(pos);
            
            osw = new OutputStreamWriter(pos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Graph getVGraph() {
        return vizsterGraph;
    }
    
    @Override
    public void convertGraph() {
        System.out.println("converting");
        try {
            osw.write("<graph directed=\"1\">\n");
            super.convertGraph();
            osw.write("</graph>");
            
            osw.close();
            pos.close();
            
            //read in graph
            XMLGraphReader xgr = new XMLGraphReader();
            vizsterGraph = xgr.loadGraph(pis);
            
            pis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("converted graph");
    }
    
    @Override
    public void convertNode(Node n) {
        try {
            osw.write("\t<node id=\""+n.getId()+"\">\n");
            osw.write("\t\t<att name=\"name\" value=\""+n.getName()+"\"> </att>\n");
            osw.write("\t\t<att name=\"uid\" value=\""+n.getName()+"\"> </att>\n");
            osw.write("\t</node>\n");
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override 
    public void convertEdge(Edge e) {
        try {
            int id1 = e.getFirst().getId();
            int id2 = e.getSecond().getId();
            float score = e.getScore();
            
            osw.write("\t<edge source=\""+id1+"\" target=\""+id2+"\""
                    + " weight=\""+score+"\"> </edge>\n");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
