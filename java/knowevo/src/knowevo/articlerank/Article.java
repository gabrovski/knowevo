package knowevo.articlerank;

import java.util.ArrayList;


public class Article implements Comparable {

    private int id;
    private int[] edges;
    private int[] backEdges;
    private double rank;

    public Article() {
        id = -1;
    }

    public Article(int i, int[] ed) {
        id = i;
        edges = ed;
    }
    
    public int getNumEdges() {
      return edges.length;
    }
    
    public int getNumBackEdges() {
      return backEdges.length;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public int[] getEdges() {
        return edges;
    }

    public void setEdges(int[] e) {
        edges = e;
    }

    public int[] getBackEdges() {
        return backEdges;
    }

    public void setBackEdges(int[] bed) {
        backEdges = bed;
    }
    
    public void setBackEdges(ArrayList arr) {
	if (arr!= null){
	    int len = arr.size();
	    backEdges = new int[len];
	    for (int i = 0; i < len; i++) {
		backEdges[i] = ((Integer) arr.get(i)).intValue();
	    }
	    arr = null;
	}
	else {
	    backEdges = new int[0];
	}
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double d) {
        rank = d;
    }

    /*reversed!*/
    public int compareTo(Object o) {
	Article a = (Article) o;
	double r = getRank()-a.getRank();
	if (r > 0) {
	    return -11;
	}
	if (r < 0) 
	    return 1;
	return 0;
    }
}
