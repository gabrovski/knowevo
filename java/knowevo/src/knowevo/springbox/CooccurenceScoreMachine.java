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
		getDBB().getQuery("select count(it.from_article_id) from ("
                    + "select from_article_id from"
                    + " gravebook_article_linked_by a" 
                    + " where to_article_id = ?"
                    + " intersect"
                    + " select from_article_id from "
                    + " gravebook_article_linked_by"
                    + " where to_article_id = ?"
                    + ") it",
                    new String[] {un, vn});
	    
	  
	    if (rs.next()) 
		res += rs.getInt("count");
            System.out.println(res);

	    return res;
    }
    
}
