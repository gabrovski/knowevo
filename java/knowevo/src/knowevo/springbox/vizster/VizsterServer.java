/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import knowevo.springbox.CooccurenceScoreMachine;
import knowevo.springbox.ScoreMachine;

/**
 *
 * @author gabrovski
 */
public class VizsterServer implements Runnable {
    
    private Socket sockd;
    private int max_depth;
    private String pngpath;
    private boolean peers_only;
    private int limit;
    
    private VizsterDBBuilder vdb;
    
    public static void runServer(int port, int maxd, boolean peers_only, int limit) {	
        
	try{
	    ServerSocket listener = new ServerSocket(port);

	    while (true) {
		VizsterServer gs = new  VizsterServer(listener.accept(), maxd, peers_only, new CooccurenceScoreMachine(), limit);
                System.out.println("accepted connection");
		Thread t = new Thread(gs);
		t.start();

	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

     
    public VizsterServer(Socket sd, int maxd, boolean po, ScoreMachine sm, int lim) {
	sockd = sd;
	max_depth = maxd;
        peers_only = po;
        vdb = new VizsterDBBuilder(sm);
        limit = lim;
    }

    public void run () {
	try {
	    DataInputStream in = new DataInputStream (sockd.getInputStream());
            DataOutputStream out = new DataOutputStream (sockd.getOutputStream());
            
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            vdb.setBw(bw);
            
	    String name;
            
	    while((name = br.readLine()) != null) {
                System.out.println("graph for "+name);
                vdb.buildGraph(name, max_depth, peers_only);
                System.out.println("converting graph for "+name);
                vdb.convertGraph(limit);
		System.out.println("graph for "+name+" is ready");
                break;
	    }            
            

            br.close();
            in.close();
            bw.close();
            out.close();

	    sockd.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
