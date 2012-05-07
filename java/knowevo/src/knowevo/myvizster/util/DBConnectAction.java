package knowevo.myvizster.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import knowevo.myvizster.Vizster;
import knowevo.myvizster.VizsterDBLoader;
import knowevo.myvizster.ui.LoginDialog;

/**
 * Attempts a connection to a database
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class DBConnectAction extends AbstractAction {

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String PROTOCOL = "jdbc:mysql:";
    private LoginDialog loginD;
    
    public DBConnectAction(LoginDialog login) {
        loginD = login;
    } //
    
    public void actionPerformed(ActionEvent e) {
        try {
            loginD.setError(" ");
            String[] auth = loginD.getLoginInfo();
            if ( auth == null ) return;
            Vizster vizster = (Vizster)loginD.getOwner();
            VizsterDBLoader loader = vizster.getLoader();
            loader.connect(DRIVER,
                    // first use the host and database name
                    PROTOCOL + "//" + auth[2] + "/" + auth[3],
                    auth[0],    // now use the login
                    auth[1]);   // and finally the password
            loginD.setLoggedIn(true);
            loginD.saveLoginProperies(auth);
            loginD.setVisible(false);
        } catch (Exception ex) {
            loginD.setLoggedIn(false);
            String msg = ex.getMessage();
            String error = "";
            if ( msg.indexOf("java.net.") != -1 ) {
                error = "Invalid database URL";
            } else if ( msg.indexOf("Unknown database") != -1 ) {
                error = "Unknown database";
            } else {
                error = "Incorrect login or password";
            }
            loginD.setError(error);
        }
    } //

} // end of class DBConnectAction
