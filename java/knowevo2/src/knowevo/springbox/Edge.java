/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

/**
 *
 * @author gabrovski
 */
public class Edge {
    private Node first, second;
    private float score;
    private boolean directed;
    
    public Edge(Node a, Node b, float s, boolean dir) {
        first = a;
        second = b;
        score = s;
        directed = dir;
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
}
