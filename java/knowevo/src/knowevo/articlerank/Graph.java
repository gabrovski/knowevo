package knowevo.articlerank;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.Iterator;
import java.io.*;

public class Graph {

    //private Article[] articles;
    private TreeMap<Integer, Article> articles;

    public Graph(Article[] ars) {
	articles = new TreeMap<Integer, Article>();
	for (int i = 0; i < ars.length; i++) {
	    articles.put(ars[i].getId(), ars[i]);
	}
    }

    public int getArticleNum() {
        return articles.size();
    }
    
    @SuppressWarnings("unchecked")
    public void populateBackEdges() {
        //ArrayList[] beds = new ArrayList[articles.length];
        int[] edges;
	TreeMap<Integer, ArrayList<Integer>> beds = new TreeMap<Integer, ArrayList<Integer>>();

	Iterator<Integer> it = articles.keySet().iterator();
	int id;
	int toArticle;
	while (it.hasNext()) {
	    id = it.next();
	    edges = articles.get(id).getEdges();
	    for (int i = 0; i < edges.length; i++) {
		//System.out.println(edges[i]+" "+articles.containsKey(edges[i]));
		if (articles.containsKey(edges[i])){
		    toArticle = articles.get(edges[i]).getId();
		    if (beds.containsKey(toArticle)) {
			beds.get(toArticle).add(id);
		    }
		    else {
			ArrayList<Integer> al = new ArrayList<Integer>();
			al.add(id);
			beds.put(toArticle, al);
		    }
		}
	    }
	}

	it = articles.keySet().iterator();
	while (it.hasNext()) {
	    id = it.next();
	    articles.get(id).setBackEdges(beds.get(id));
	}
	
    }

    public void calculateIterRank(double norm, double damp, int reps) {
        double init = norm / getArticleNum();
	Iterator<Integer> it = articles.keySet().iterator();
	int id;

	while (it.hasNext()) {
	    id = it.next();
	    articles.get(id).setRank(norm);
	}

        double rank;
        int[] backEdges;
        Article art;
	it = articles.keySet().iterator();
        for (int i = 0; i < reps; i++) {
	    while (it.hasNext()) {
		id = it.next();
		rank = 0;
                backEdges = articles.get(id).getBackEdges();
                for (int k = 0; k < backEdges.length; k++) {
                    art = articles.get(backEdges[k]);
                    rank += art.getRank() / art.getNumEdges();
                }
                rank = (1 - damp) / getArticleNum() + damp * rank;
                articles.get(id).setRank(rank);
	    }	    
        }
    }
    
        
    public void saveGraph(String out) {
	try {
	    BufferedWriter bw = new BufferedWriter(new FileWriter(out));
	    Iterator<Integer> it = articles.keySet().iterator();
	    int id;
	    while (it.hasNext()) {
		id = it.next();
		bw.write(articles.get(id).getId()+"");
		bw.write(":");
		bw.write(articles.get(id).getRank()+"");
		bw.write("\n");
	    }
	    bw.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
