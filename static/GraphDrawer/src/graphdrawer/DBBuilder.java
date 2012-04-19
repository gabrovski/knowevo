/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdrawer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.gephi.io.database.drivers.PostgreSQLDriver;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.*;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
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
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 *
 * @author sasho
 */
public class DBBuilder {
    
    private static final String DBNAME = "knowevo";
    private static final String DBUSER = "knowevo";
    private static final String PW = "12345c7890";
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final SQLDriver SD = new PostgreSQLDriver();
    private static final String URL = SQLUtils.getUrl(SD, HOST, PORT, DBNAME);
    
    private Connection conn;
    private ProjectController pc;
    private Workspace workspace;

    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private Graph graph;
    
    private PNGExporter exporter;
    private ExportController ec;
    
    public static void getGraphFor(String name, int max_depth, String out) {
        DBBuilder dbb = new DBBuilder();
        dbb.buildGraph(name, max_depth);
        dbb.export(name);
    }
    
    private DBBuilder() {
        try {
            conn = SD.getConnection(URL, DBUSER, PW);
            
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();
            Workspace workspace = pc.getCurrentWorkspace();

            //Get controllers and models
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            
            graph = graphModel.getUndirectedGraph();
           
            ec = Lookup.getDefault().lookup(ExportController.class);
            exporter = (PNGExporter) ec.getExporter("png"); 
            exporter.setWorkspace(workspace);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void export(String name) {
         try {
            ec.exportFile(new File(name), exporter);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
    
    private ResultSet getQuery(String query) {
        try {
            Statement s = conn.createStatement();
            return s.executeQuery(query); 
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
      
    private void buildGraph(String name, int max_depth) {
        Queue<Neighbor> queue = new LinkedList<Neighbor>();
        
        try {
              ResultSet rs = this.getQuery("select name from gravebook_article "+
                    "where name = "+name);
            
            if (rs.next()) 
                queue.add(new Neighbor(rs.getString("name"), 0));
            buildGraph(queue, max_depth);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void buildGraph(Queue<Neighbor> queue, int max_depth) {
        Set<String> seen = new HashSet<String>();
        Neighbor curr;
        Node parent;
        
        while(!queue.isEmpty()) {
            curr = queue.remove();
            if (curr.depth < max_depth)
                break;
            
            if (!seen.contains(curr.name)) {
                seen.add(curr.name);
                parent = graphModel.factory().newNode(curr.name);
                parent.getNodeData().setLabel(curr.name);
                graph.addNode(parent);
            }
            else 
                parent = graph.getNode(curr.name);
            
            
        }
    }
    
    private Node getOrAddNode(String name) {
        return null;
    }
    
    private class Neighbor {
        String name;
        int depth;
        
        public Neighbor(String n, int d) {
            name = n;
            depth = d;
        }
    }
    
    public static void buildData() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Import database
        EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
        db.setDBName("knowevo");
        db.setHost("localhost");
        db.setUsername("knowevo");
        db.setPasswd("12345c7890");
        db.setSQLDriver(new PostgreSQLDriver());
        db.setPort(5432);
        
        db.setNodeQuery("SELECT name as id, name as label from gravebook_article");
        db.setEdgeQuery("SELECT u.name as source, v.name as target, p.id as label,"+
                    "0.0 as weight "+
                    "from gravebook_article u, gravebook_article v,  gravebook_article_people p " +
                    "where p.from_article_id = u.name and p.to_article_id = v.name ");
        ImporterEdgeList edgeListImporter = new ImporterEdgeList();
        Container container = importController.importDatabase(db, edgeListImporter);
        container.setAllowAutoNode(false);      //Don't create missing nodes
        container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);   //Force UNDIRECTED

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());
        
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(200f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        
        //Preview
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
        model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("test.pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
//        String url = SQLUtils.getUrl(db.getSQLDriver(), db.getHost(), db.getPort(), db.getDBName());
//        Connection connection = null;
//        try {
//            //System.err.println("Try to connect at " + url);
//            connection = db.getSQLDriver().getConnection(url, db.getUsername(), db.getPasswd());
//            System.err.println("Database connection established");
//        } catch (SQLException ex) {
//            if (connection != null) {
//                try {
//                    connection.close();
//                    System.err.println("Database connection terminated");
//                } catch (Exception e) { /* ignore close errors */ }
//            }
//            System.err.println("Failed to connect at " + url);
//            ex.printStackTrace(System.err);
//        }
//        if (connection == null) {
//            System.err.println("Failed to connect at " + url);
//        }
//        
//        //Close connection
//        if (connection != null) {
//            try {
//                connection.close();
//                //System.err.println("Database connection terminated");
//            } catch (Exception e) { /* ignore close errors */ }
//        }
    }
}
