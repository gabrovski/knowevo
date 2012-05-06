/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.gephibox;

import java.awt.Color;
import java.io.*;
import java.sql.*;
import java.util.*;
import knowevo.springbox.DBBuilder;
import knowevo.springbox.ScoreMachine;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.*;
import org.gephi.io.exporter.preview.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author sasho
 */
public class GephiDBBuilder extends DBBuilder {
    
    private ProjectController pc;
    private Workspace workspace;

    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private Graph gephiGraph;
    
    private SVGExporter exporter;
    private ExportController ec;
    
    private PreviewModel previewModel;
    
    public static void getGraphFor(ScoreMachine sm, String name, int max_depth, String out) 
	throws SQLException
    {
        GephiDBBuilder dbb = new GephiDBBuilder(sm);
        dbb.buildGraph(name, max_depth);
        dbb.convertGraph();
	dbb.converge(100);
        dbb.export(out);
    }
    
    private GephiDBBuilder(ScoreMachine sm) {
        super(sm);
        
        try {
            pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();
            workspace = pc.getCurrentWorkspace();

            //Get controllers and models
	    graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            
            gephiGraph = graphModel.getDirectedGraph();
           
            ec = Lookup.getDefault().lookup(ExportController.class);
            exporter = (SVGExporter) ec.getExporter("svg"); 
            exporter.setWorkspace(workspace);

	    previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void export(String name) {
         try {
             System.out.println(name);
            ec.exportFile(new File(name), exporter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void converge(int times) {

	YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(30f);

        layout.initAlgo();
        for (int i = 0; i < times && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        
        //Preview
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.BLUE));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
	previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(14));
    }
    
    @Override 
    public void convertNode(knowevo.springbox.Node node) {
        Node n = graphModel.factory().newNode(node.getName());
        n.getNodeData().setLabel(node.getName());
        gephiGraph.addNode(n);
    }
    
    @Override 
    public void convertEdge(knowevo.springbox.Edge edge) {
        Node parent = gephiGraph.getNode(edge.getFirst().getName());
        Node child = gephiGraph.getNode(edge.getSecond().getName());
        Edge e = graphModel.factory().newEdge(parent, child, edge.getScore(), edge.isDirected());
        gephiGraph.addEdge(e);
    }
}
