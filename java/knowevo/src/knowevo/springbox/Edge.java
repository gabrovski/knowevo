/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

/**
 *
 * @author gabrovski
 */
public class Edge implements Comparable {
    private Node first, second;
    private float score;
    private boolean directed;
    
    public Edge(Node a, Node b, float s, boolean dir) {
        first = a;
        second = b;
        score = s;
        directed = dir;
        
        first.addToScore((int)Math.round(s));
        second.addToScore((int) Math.round(s));
    }
    
    public Node[] getNodes() {
        return new Node[] {first, second};
    }
    
    public Node getFirst() {
        return first;
    }
    
    public Node getSecond() {
        return second;
    }
    
    public float getScore() {
        return score;
    }
    
    public boolean isDirected() {
        return directed;
    }

    @Override
    public int compareTo(Object t) {
        Edge e = (Edge) t;
        return (int) Math.round(score -e.getScore());
    }
}
