/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo;

import java.awt.Color;
import java.util.*;
import java.io.*;
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
/**
 *
 * @author gabrovski
 */
public class Poets {
    
    private HashMap<String, HashMap<String, Integer>> edges;
    
    public Poets(String path) {
        edges = new HashMap<String, HashMap<String, Integer>>();
        
        parsePoets(path);
    }
    
    private void addPoet(String p1, String p2) {
        if (!edges.containsKey(p1))
            edges.put(p1, new HashMap<String, Integer>());
        if (!edges.containsKey(p2))
            edges.put(p2, new HashMap<String, Integer>());
        
        int score = 0;
        if (edges.get(p1).containsKey(p2))
            score = edges.get(p1).get(p2);
        edges.get(p1).put(p2, score+1);
        
        score = 0;
        if (edges.get(p2).containsKey(p1))
            score = edges.get(p2).get(p1);
        edges.get(p2).put(p1, score+1);
    }
    
    private void parsePoets(String path) {
       
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            ArrayList<String> poets = new ArrayList<String>();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("")) {
                    for (int i = 0; i < poets.size(); i++)
                        for (int j = i+1; j < poets.size(); j++)
                            addPoet(poets.get(i), poets.get(j));
                    
                    poets = new ArrayList<String>();
                } 
                else {
                    poets.add(line);
                }
            }
            br.close();
        } 
        catch (Exception e) { }
        
    }
    
    public void drawGraphs(String path) {
        Iterator<String> it = edges.keySet().iterator();
        while (it.hasNext())
            doPoetGraph(path, it.next());
    }
    
    private void doPoetGraph(String path, String poet) {
        System.out.println(poet);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph directedGraph = graphModel.getUndirectedGraph();
        
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        
        ArrayList<String> queue = new ArrayList<String>();
        ArrayList<Integer> depthqueue = new ArrayList<Integer>();
        queue.add(poet);
        depthqueue.add(0);
        int depth = 0;
        int nodecount =0;
        
        while (queue.size() != 0 && depth < 3 && nodecount < 20) {
            String name = queue.remove(0);
            depth = depthqueue.remove(0);
            
            Node n = directedGraph.getNode(name);
            if (n == null) {
                n = graphModel.factory().newNode(name);
                n.getNodeData().setLabel(name);
                directedGraph.addNode(n);
                nodecount++;
            }
            
            Iterator<String> it = edges.get(poet).keySet().iterator();
            while (it.hasNext()) {
                String to = it.next();
                Node slave = directedGraph.getNode(to);
                if (slave == null) {
                    slave = graphModel.factory().newNode(to);
                    slave.getNodeData().setLabel(to);
                    directedGraph.addNode(slave);
                }
                Edge e = graphModel.factory().newEdge(n, slave, edges.get(poet).get(to)*15, false);
                directedGraph.addEdge(e);
                queue.add(to);
                depthqueue.add(depth+1);
            }
        }

        //Preview
        PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLUE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.005f));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(12));
        
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(120f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        
        try {
            ec.exportFile(new File(path+poet+".svg"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
 }
