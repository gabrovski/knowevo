/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdrawer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.gephi.io.database.drivers.PostgreSQLDriver;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.exporter.api.ExportController;
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
