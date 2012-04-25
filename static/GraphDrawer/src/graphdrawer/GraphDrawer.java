/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdrawer;

import java.io.File;
import java.io.IOException;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author sasho
 */
public class GraphDrawer {
    
    private static final int PORT = 62541;
    private static final int MAX_DEPTH = 3;
    private static final String PNGPATH = "/home/sasho/cs/knowevo/static/pngs/";
    
    public static void main(String[] args) {
	try {
	    GraphServer.runServer(PORT, MAX_DEPTH, PNGPATH);
	    //DBBuilder.getGraphFor("Alan Turing", 1, "test.png");
	    //testUndirectedGraph();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    
    
    private static void testUndirectedGraph() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        
        //Create three nodes
        Node n0 = graphModel.factory().newNode("n0");
        n0.getNodeData().setLabel("Node 0");
        Node n1 = graphModel.factory().newNode("n1");
        n1.getNodeData().setLabel("Node 1");
        Node n2 = graphModel.factory().newNode("n2");
        n2.getNodeData().setLabel("Node 2");
        
        //Create three edges
        Edge e1 = graphModel.factory().newEdge(n1, n2, 1f, false);
        Edge e2 = graphModel.factory().newEdge(n0, n2, 2f, false);
        Edge e3 = graphModel.factory().newEdge(n2, n0, 2f, false);   //This is e2's mutual edge

        //Append as a Directed Graph
        UndirectedGraph directedGraph = graphModel.getUndirectedGraph();
        directedGraph.addNode(n0);
        directedGraph.addNode(n1);
        directedGraph.addNode(n2);
        directedGraph.addEdge(e1);
        directedGraph.addEdge(e2);
        directedGraph.addEdge(e3);

        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        //PNGExporter exporter = (PNGExporter) ec.getExporter("png");     //Get GEXF exporter
        //exporter.setWorkspace(workspace);
        
        try {
            ec.exportFile(new File("undir_gml.svg"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
}
