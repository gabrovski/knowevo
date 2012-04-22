/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package graphdrawer;

import java.io.*;
import java.net.*;
import java.security.*;


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
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String name;

	    while((name = br.readLine()) != null) {
		DBBuilder.getGraphFor(name, max_depth, pngpath+name+".png");
		System.out.println("graph for "+name+" is ready");
	    }

	    sockd.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }


}
