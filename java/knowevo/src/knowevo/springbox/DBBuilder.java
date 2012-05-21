/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.gephi.io.database.drivers.PostgreSQLDriver;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;

/**
 *
 * @author gabrovski
 */
public abstract class DBBuilder {

    private static final String DBNAME = "knowevo";
    private static final String DBUSER = "knowevo";
    private static final String PW = "12345c7890";
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final SQLDriver SD = new PostgreSQLDriver();
    private static final String URL = SQLUtils.getUrl(SD, HOST, PORT, DBNAME);
    private Connection conn;
    private Graph graph;
    private ScoreMachine smach;
    private int num_nodes;

    public DBBuilder(ScoreMachine sm) {
        try {
            conn = SD.getConnection(URL, DBUSER, PW);
            graph = new Graph();
            
            smach = sm;
            smach.setDBB(this);
            
            num_nodes = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public ResultSet getQuery(String query, String[] args)
            throws SQLException {
        PreparedStatement s = conn.prepareStatement(query);
        for (int i = 0; i < args.length; i++) {
            s.setString(i + 1, args[i]);
        }
        return s.executeQuery();
    }

    public void buildGraph(String name, int max_depth, boolean peers_only)
            throws SQLException {
        
        Queue<Neighbor> queue = new LinkedList<Neighbor>();

        ResultSet rs = this.getQuery("select g.name from "
                + " gravebook_article g "
                + "where g.name = ?",
                new String[]{name});

        if (rs.next()) {
            queue.add(new Neighbor(rs.getString("name"), 0));
        }

        buildGraph(queue, max_depth, peers_only);
    }

    private void buildGraph(Queue<Neighbor> queue, int max_depth, boolean peers_only)
            throws SQLException {
        Set<String> seen = new HashSet<String>();
        Neighbor curr;
        Node parent;
        Node child;
        Edge edge;
        String str;
        
        String dbname = "gravebook_article_people";
        if (peers_only)
            dbname = "gravebook_article_peers";

        while (!queue.isEmpty()) {
            curr = queue.remove();
            if (curr.depth > max_depth) {
                break;
            }

            seen.add(curr.name);
            parent = getOrCreateNode(curr.name);
            
            ResultSet rs = getQuery("select to_article_id "
                    + "from "+dbname+" "
                    + "where from_article_id = ?",
                    new String[]{curr.name});

            while (rs.next()) {
                str = rs.getString("to_article_id");
                //System.out.println(str);

                child = getOrCreateNode(str);
                if (!seen.contains(str)) {
                    seen.add(str);
                    queue.offer(new Neighbor(str, curr.depth + 1));
                }

                float score = smach.getScore(parent, child);
                edge = new Edge(parent, child, score, true);
                graph.addEdge(edge);
            }
        }
    }

    private Node getOrCreateNode(String name) {
        Node n = graph.getNode(name);
        if (n == null) {
            n = new Node(name, num_nodes++);
            graph.addNode(n);
        }
        return n;
    }

    public abstract void convertNode(Node n);

    public abstract void convertEdge(Edge e);

    public void convertGraph() {
        Iterator<Node> nit = getGraph().getNodes();
        while (nit.hasNext()) {
            convertNode(nit.next());
        }
        
        Iterator<knowevo.springbox.Edge> eit = getGraph().getEdges();
        while (eit.hasNext()) {
            convertEdge(eit.next());
        }
    }
    
    public void convertGraph(int limit) {
        graph.limitNodes(limit);
        convertGraph();
    }
}
