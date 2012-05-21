/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import knowevo.articlerank.*;
import knowevo.springbox.CategoryScoreMachine;
import knowevo.springbox.CooccurenceScoreMachine;
import knowevo.springbox.gephibox.GephiDBBuilder;
import knowevo.springbox.gephibox.GraphServer;
import knowevo.springbox.vizster.VizsterDrawer;
import knowevo.springbox.vizster.VizsterRunner;
import knowevo.springbox.vizster.VizsterServer;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import vizster.Vizster;

/**
 *
 * @author gabrovski
 */
public class main {

    private static final int PORT = 62541;
    private static final int MAX_DEPTH = 1;
    //private static final String PNGPATH = "/home/gabrovski/cs/knowevo/static/pngs/";
    private static final boolean PEERS_ONLY = false;

    public static void main(String args[]) {
        runserver(args);
        //poetsParser("/home/gabrovski/cs/misha/poets.csv");
    }

    private static void runserver(String args[]) {
        System.out.println("starting");
        try {
            if (args.length == 0) {
                VizsterRunner.getGraphFor("Abraham Lincoln", 2, "tmp", PEERS_ONLY);
                //GephiDBBuilder.getGraphFor(new CooccurenceScoreMachine(), "Aleister Crowley", MAX_DEPTH, "test.svg", PEERS_ONLY);
            } else if (args[0].equals("ranker")) {
                knowevo.articlerank.Graph g = Ranker.buildSeeAlsoGraph(args[1]);
                System.out.println("Graph built");

                g.populateBackEdges();
                System.out.println("back edges populated");

                g.calculateIterRank(100000, 0.85, 100);
                g.saveGraph(args[2]);
            } else if (args[0].equals("server")) {
                int port = Integer.parseInt(args[1]);
                int max_depth = Integer.parseInt(args[2]);
                boolean peers_only = false;
                if (args[3].equals("true")) {
                    peers_only = true;
                }

                System.out.println("server at " + port + " wiht depth " + max_depth + " with peers only = " + peers_only);
                try {
                    //GraphServer.runServer(PORT, MAX_DEPTH, PNGPATH, PEERS_ONLY);
                    VizsterServer.runServer(port, max_depth, peers_only);
                    //DBBuilder.getGraphFor("Alan Turing", 1, "test.png");
                    //testUndirectedGraph();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //args[-1] = null;//trigger error, too lazy to fix
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Vizster v = new Vizster("132132", "/home/gabrovski/downloads/vizster.sample.xml");
            System.out.println("usage: \n\tranker input output\n\t server");
        }
    }

    private static void poetsParser(String path) {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();


        //Append as a Directed Graph
        UndirectedGraph directedGraph = graphModel.getUndirectedGraph();


        ExportController ec = Lookup.getDefault().lookup(ExportController.class);

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            Node master = null;
            Node n;
            while ((line = br.readLine()) != null) {
                if (line.equals("")) {
                    master = null;
                } else if (master == null) {
                    master = directedGraph.getNode(line);
                    if (master == null) {
                        master = graphModel.factory().newNode(line);
                        master.getNodeData().setLabel(line);
                        directedGraph.addNode(master);
                        map.put(line, new ArrayList<String>());
                    }
                } else {
                    n = directedGraph.getNode(line);
                    if (n == null) {
                        n = graphModel.factory().newNode(line);
                        n.getNodeData().setLabel(line);
                        map.put(line, new ArrayList<String>());
                    }
                    Edge e = graphModel.factory().newEdge(master, n, 1f, false);
                    directedGraph.addNode(n);
                    directedGraph.addEdge(e);
                    map.get(master.getNodeData().getLabel()).add(line);
                }
            }
            br.close();
        } catch (Exception e) {
        }

        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(30f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }

        //Preview
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLUE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(14));

        try {
            ec.exportFile(new File("/home/gabrovski/cs/misha/poets.svg"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
        doSeparatePoets(map);
    }
    
    private static void doSeparatePoets(HashMap<String, ArrayList<String>> map) {
        Iterator<String> it = map.keySet().iterator();
        String master;
        
        while (it.hasNext()) {
            master = it.next();
            if (map.get(master).size() > 0)
                doPoetGraph(map, master);
       }
    }
    
    private static void doPoetGraph(HashMap<String, ArrayList<String>> map, String master) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph directedGraph = graphModel.getUndirectedGraph();
        
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        
        ArrayList<String> queue = new ArrayList<String>();
        ArrayList<Integer> depthqueue = new ArrayList<Integer>();
        queue.add(master);
        depthqueue.add(0);
        int depth = 0;
        
        while (queue.size() != 0 && depth < 3) {
            String name = queue.remove(0);
            depth = depthqueue.remove(0);
            
            Node n = directedGraph.getNode(name);
            if (n == null) {
                n = graphModel.factory().newNode(name);
                n.getNodeData().setLabel(name);
                directedGraph.addNode(n);
            }
            
            for (String to: map.get(master)) {
                Node slave = directedGraph.getNode(to);
                if (slave == null) {
                    slave = graphModel.factory().newNode(to);
                    slave.getNodeData().setLabel(to);
                    directedGraph.addNode(slave);
                }
                Edge e = graphModel.factory().newEdge(n, slave);
                directedGraph.addEdge(e);
                queue.add(to);
                depthqueue.add(depth+1);
            }
        }

        //Preview
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLUE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.05f));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
        
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        
        try {
            ec.exportFile(new File("/home/gabrovski/cs/misha/"+master+".svg"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
}
