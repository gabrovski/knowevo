/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import knowevo.springbox.DBBuilder;
import knowevo.springbox.Edge;
import knowevo.springbox.Node;
import knowevo.springbox.ScoreMachine;

/**
 *
 * @author gabrovski
 */
public class VizsterDBStreamBuilder extends VizsterDBBuilder {
    
    private DataOutputStream dos;
    private OutputStreamWriter dosw;
    private OutputStream os;
    
    public VizsterDBStreamBuilder(ScoreMachine sm, Socket sd) {
        super(sm);
  
        try {
            os = sd.getOutputStream();
            dos = new DataOutputStream(os);
            dosw = new OutputStreamWriter(dos);
            setBw(new BufferedWriter(dosw));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        super.close();
        try {
            dos.close();
            os.close();
            dosw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
