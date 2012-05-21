/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

/**
 *
 * @author gabrovski
 */
public class Node implements Comparable {
    
    private String name;
    private int id;
    private int totalscore;
    
    public Node(String n, int i) {
        name = n;
        id = i;
        totalscore = 0;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
    
    public void addToScore(int i) {
        totalscore +=1;
    }
    
    public int getScore() {
        return totalscore;
    }

    @Override
    public int compareTo(Object t) {
        Node n = (Node) t;
        return totalscore - n.totalscore;
    }
}
