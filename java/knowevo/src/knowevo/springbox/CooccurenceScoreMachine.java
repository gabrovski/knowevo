/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author gabrovski
 */
public class CooccurenceScoreMachine extends ScoreMachine {
    
    @Override
    public float getScore(Node u, Node v) 
        throws SQLException
    {
        float res = 0;
	    String un = u.getName();
	    String vn = v.getName();
	    
            ResultSet rs = 
		getDBB().getQuery("select count",
                    new String[] {un, vn});
	    
	  
	    if (rs.next()) 
		res += rs.getInt("count");

	    return res;
    }
    
}
