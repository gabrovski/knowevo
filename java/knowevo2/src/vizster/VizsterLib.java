package vizster;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Scanner;
import java.util.Timer;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;

import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.io.XMLGraphReader;

import vizster.ui.LoginDialog;

/**
 * Library of useful routines supporting the Vizster application
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizter(AT)jheer.org
 */
public class VizsterLib {

    public static final int DEFAULT_ERROR = 0;
    public static final int PROFILE_ERROR = 1;
    
    /**
     * Exit the application upon occurrence of an error
     * @param e the exception accompanying the error, if any
     * @param c the parent component, if exists 
     * @param msg the error message
     */
    public static final void errexit(Exception e, Component c, String msg) {
        errexit(DEFAULT_ERROR, e, c, msg);
    } //
    
    /**
     * Exit the application upon occurrence of an error
     * @param type the type of error
     * @param e the exception accompanying the error, if any
     * @param c the parent component, if exists 
     * @param msg the error message
     */
    public static final void errexit(int type, Exception e, Component c, String msg) {
        if ( e != null )
            e.printStackTrace();
        switch ( type ) {
        case PROFILE_ERROR:
            profileLoadError(c,msg);
            break;
        default:
            defaultError(c,msg);
        }
        System.exit(1);
    } //
    
    
    /**
     * Show an error dialog for a failed profile load
     * @param c the parent component
     * @param uid the user id of the failed profile
     */
    public static final void profileLoadError(Component c, String uid) {
        JOptionPane.showMessageDialog(c, "Error loading profile: "+uid,
                "Error Loading Profile", JOptionPane.ERROR_MESSAGE);
    } //
    
    /**
     * Show an error dialog
     * @param c the parent component
     * @param msg the error message
     */
    public static final void defaultError(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg);
    } //
    
    public static final boolean authenticate(Vizster owner, int retries) {
        LoginDialog ld = new LoginDialog(owner);
        ld.setVisible(true);
        return ld.isLoggedIn();
    } //
    
    public static final Graph loadGraph(String graphfile) 
    	throws FileNotFoundException, IOException
    {
    	XMLGraphReader gl = new XMLGraphReader();
    	try {
    		return gl.loadGraph(graphfile);
    	} catch(MalformedByteSequenceException mbse) {
    		System.err.println(graphfile + " contains a bad character. Reformatting file and trying again...");    		
    		File inputFile = new File(graphfile);
    		FileWriter writer = new FileWriter(graphfile + ".reformat");
    		for(Scanner sc = new Scanner(inputFile); sc.hasNext(); ) {
    			String line = sc.nextLine();
    			writer.write(removeDiatrecials(line));
    		}    		
    		writer.flush();
    		writer.close();    		
    		return gl.loadGraph(graphfile + ".reformat");
    	}
    } //
    
    public static String removeDiatrecials(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    	String ret = "";
    	for(char c : s.toCharArray())
    		if(c < 128) 
    			ret += c;    	
    	return ret;
    }
    
    public static final void setLookAndFeel() {
        try {
            String laf = UIManager.getSystemLookAndFeelClassName();             
            UIManager.setLookAndFeel(laf);  
        } catch ( Exception e ) {}
    } //
    
    public static final void setHighlightValue(VisualItem item, int val) {
        if ( item == null ) return;
        int[] value = (int[])item.getVizAttribute("highlightValue");
        if ( value == null ) {
            value = new int[1];
            item.setVizAttribute("highlightValue", value);
        }
        value[0] = val;
    } //
    
    public static final int getHighlightValue(VisualItem item) {
        if ( item == null ) return -1;
        int[] val = (int[])item.getVizAttribute("highlightValue");
        return ( val==null ? 0 : val[0] );
    } //
    
    private static Timer s_timer;
    
    public static final Timer getTimer() {
        if ( s_timer == null )
            s_timer = new Timer() {
            	public void cancel() {
            	    // do nothing, do not let others cancel the timer
            	} //
        	};
        return s_timer;
    } //
    
} // end of class VizsterLib
