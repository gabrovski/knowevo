package knowevo.articlerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;

//import 	net.sf.javaml.clustering.mcl.*;

public class Ranker {
    
//    public static void main(String[] args) {
//	Graph g = buildSeeAlsoGraph(args[0]);
//	System.out.println("Graph built");
//
//    	g.populateBackEdges();
//	System.out.println("back edges populated");
//
//	g.calculateIterRank(100000, 0.85, 100);
//    	g.saveGraph(args[1]);
//
//	//MarkovCluster mcl = new MarkovCluster(wg);
//	//mcl.sparseCluster(2, 0);
//	//mcl.saveClusters("markcluster");
//	//mclJAR("tfdf.txt", "mcljar.txt");
//    }

    
    public static Graph buildSeeAlsoGraph(String path) {
        Graph g = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            ArrayList<Article> al = new ArrayList<Article>();
            Article[] arts;
            
            String line = br.readLine();
            while (line != null) {
                al.add(getSeealsoArticle(line));
                line = br.readLine();
            }
            
            arts = new Article[al.size()];
            for (int i = 0; i < arts.length; i++)
                arts[i] = al.get(i);
            g = new Graph(arts);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return g;
    }
    
    private static Article getSeealsoArticle(String line) {
        Article art;
        String[] parts = line.split(" ");
        int id = Integer.parseInt(parts[0].split(":")[0]);
        
        int[] edges = new int[parts.length-1];
        for (int i = 0; i < edges.length && parts[i+1].length() != 0; i++)
            edges[i] = Integer.parseInt(parts[i+1]);
        
        art = new Article(id, edges);
	//System.out.println("\t"+line);
        return art;
    }
        
    private static Map<Integer, Double> getTFDFMap(String line, boolean weighted, double cutoff) {
    	Map<Integer, Double> res = new Hashtable<Integer, Double>();
    	line = line.substring(1, line.length()-1);
    	String[] items = line.split(",");
    	String[] pair;
	Double d;
    	for (String item : items) {
    		if (item.equals(""))
    			continue;
    		
    		pair = item.split(":");
		if (pair[1].equals("NaN"))
		    continue;
		d = Double.parseDouble(pair[1].trim());
		if (!weighted && d < cutoff)
		    continue;
    		res.put(Integer.parseInt(pair[0].trim()), d);
    	}
    	return res;
    }
}

