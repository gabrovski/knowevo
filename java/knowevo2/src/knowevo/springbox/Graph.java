/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

import java.util.*;

/**
 *
 * @author gabrovski
 */
public class Graph {
    
    private Map<String, Node> nodemap;
    private List<Edge> edges;
    
    public Graph() {
        nodemap = new HashMap<String, Node>();
        edges = new ArrayList<Edge>();
    }
    
    public Node getNode(String name) {
        if (nodemap.containsKey(name))
            return nodemap.get(name);
        else
            return null;
    }
    
    public void addNode(Node a) {
        nodemap.put(a.getName(), a);
    }
    
    public void addEdge(Edge e) {
        edges.add(e);
    }
    
    public Iterator<Edge> getEdges() {
        return edges.iterator();
    }
    
    public Iterator<Node> getNodes() {
        return nodemap.values().iterator();
    }
}
