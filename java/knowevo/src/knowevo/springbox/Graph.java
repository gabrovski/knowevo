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
    
    public void limitEdges(int limit) {
        Collections.sort(edges);
        ArrayList<Edge> newlist = new ArrayList<Edge>();
        for (int j = edges.size()-1, i = 0; i < limit && j > -1; i++, j--)
            newlist.add(edges.get(j));
        edges = newlist;
        
        nodemap = new HashMap<String, Node>();
        for (Edge e: edges) {
            Node ns[] = e.getNodes();
            for (Node n: ns)
                if (!nodemap.containsKey(n.getName()))
                    nodemap.put(n.getName(), n);
        }
    }
    
    public void limitNodes(int limit) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        Iterator<Node> it = nodemap.values().iterator();
        while (it.hasNext())
            nodes.add(it.next());
        
        Collections.sort(nodes);
        nodemap = new HashMap<String, Node>();
        for (int j = nodes.size()-1, i = 0; i < limit && j > -1; i++, j--) {
            nodemap.put(nodes.get(j).getName(), nodes.get(j));
        }
        
        ArrayList<Edge> newlist = new ArrayList<Edge>();
        boolean incl = true;
        for (Edge e: edges) {
            Node ns[] = e.getNodes();
            incl = true;
            for (Node n: ns)
                if (!nodemap.containsKey(n.getName())) {
                    incl = false;
                    break;
                }
            
            if (incl) {
                //System.out.println("dwadwa");
                newlist.add(e);
            }
        }
        edges = newlist;
    }
}
