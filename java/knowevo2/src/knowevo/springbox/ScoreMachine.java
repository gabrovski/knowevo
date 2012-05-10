/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author gabrovski
 */
public abstract class ScoreMachine {

    private DBBuilder db;

   public void setDBB(DBBuilder d) {
       db = d;
   }
           
    public DBBuilder getDBB() {
        return db;
    }

    public abstract float getScore(Node u, Node v) throws SQLException;
}
