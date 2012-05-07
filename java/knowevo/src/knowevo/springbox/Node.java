/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

/**
 *
 * @author gabrovski
 */
public class Node {
    
    private String name;
    private int id;
    
    public Node(String n, int i) {
        name = n;
        id = i;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
}
