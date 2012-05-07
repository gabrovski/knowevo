/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.gephibox;

import java.io.*;
import java.net.*;
import java.security.*;
import knowevo.springbox.CategoryScoreMachine;
import knowevo.springbox.CooccurenceScoreMachine;


/**
 *
 * @author sasho
 */
public class GraphServer implements Runnable {

    private Socket sockd;
    private int max_depth;
    private String pngpath;
    
    public static void runServer(int port, int maxd, String path) {	
	try{
	    ServerSocket listener = new ServerSocket(port);

	    while (true) {
		GraphServer gs = new  GraphServer(listener.accept(), maxd, path);
                System.out.println("accepted connection");
		Thread t = new Thread(gs);
		t.start();

	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

     
    public GraphServer(Socket sd, int maxd, String path) {
	sockd = sd;
	max_depth = maxd;
	pngpath = path;
    }

    public void run () {
	try {
	    DataInputStream in = new DataInputStream (sockd.getInputStream());
            DataOutputStream out = new DataOutputStream (sockd.getOutputStream());
            
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
	    String name;
            
	    while((name = br.readLine()) != null) {
                System.out.println("graph for "+name);
		GephiDBBuilder.getGraphFor(new CooccurenceScoreMachine(), name, max_depth, pngpath+name+".svg");
		System.out.println("graph for "+name+" is ready");
                break;
	    }            
            bw.write("done");
            bw.flush();
            
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
