package knowevo.myvizster.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import knowevo.myvizster.Vizster;
import knowevo.myvizster.util.DBConnectAction;

/**
 * Login dialog for Vizster.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class LoginDialog extends JDialog {

    private static final int LABELW = 75;
    private static final int FIELDW = 200;
    
    private Vizster vizster;
    private JLabel failL;
    
    private JTextField curFields[];
    private boolean login = false;
    
    public LoginDialog(Vizster owner) {
        super(owner, "Vizster Login", true);
        vizster = owner;
        curFields = null;
        initUI();
        
        // set starting screen location in center
        Dimension screenSize =
            Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = this.getPreferredSize();
        setLocation(screenSize.width/2-(dialogSize.width/2),
                    screenSize.height/2-(dialogSize.height/2));
        screenSize = dialogSize = null;
    } //
    
    private void initUI() {
        getContentPane().setBackground(Color.WHITE);
        
        // set up Vizster logo
        URL logoU = LoginDialog.class.getResource("logo.png");
        ImageIcon logoI = new ImageIcon(logoU, "Vizster Logo");
        Dimension d = new Dimension(LABELW+FIELDW, 
                                    logoI.getIconHeight());
        JLabel logoL = new JLabel(logoI);
        logoL.setIconTextGap(0);
        logoL.setPreferredSize(d);
        logoL.setMaximumSize(d);
        
        Box l = new Box(BoxLayout.X_AXIS);
        l.add(logoL);
        l.setPreferredSize(d);
        l.setMaximumSize(d);
        
        // set up feedback label
        failL = new JLabel("    ", SwingConstants.CENTER);
        failL.setForeground(Color.RED);
        d = new Dimension(LABELW+FIELDW,15);
        failL.setPreferredSize(d);
        failL.setMaximumSize(d);
        
        Box f = new Box(BoxLayout.X_AXIS);
        f.add(Box.createHorizontalGlue());
        f.add(failL);
        f.add(Box.createHorizontalGlue());
        f.setPreferredSize(d);
        f.setMaximumSize(d);
        
        // set up input fields
        JLabel inputL = new JLabel("Login:", SwingConstants.RIGHT);
        JLabel passwL = new JLabel("Password:", SwingConstants.RIGHT);
        JLabel dbhostL = new JLabel("DB URL:", SwingConstants.RIGHT);
        JLabel dbnameL = new JLabel("DB Name:", SwingConstants.RIGHT);
        
        String[] defs = loadLoginProperties();
        JTextField inputF = new JTextField(defs[0]);
        JTextField passwF = new JPasswordField(defs[1]);
        JTextField dbhostF = new JTextField(defs[2]);
        JTextField dbnameF = new JTextField(defs[3]);
        curFields = new JTextField[] {
                inputF, passwF, dbhostF, dbnameF};
        
        final JButton loginB = new JButton("Login");
        final JButton cancelB = new JButton("Cancel");
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.setVisible(false);
            } //
        };
        loginB.addActionListener(new DBConnectAction(this));
        cancelB.addActionListener(al);
        
        Box b = new Box(BoxLayout.X_AXIS);
        b.add(Box.createHorizontalGlue());
        b.add(loginB);
        b.add(Box.createHorizontalStrut(5));
        b.add(cancelB);
        b.add(Box.createHorizontalStrut(5));
        b.setPreferredSize(new Dimension(LABELW+FIELDW,30));
        b.setMaximumSize(new Dimension(LABELW+FIELDW,30));
        
        Box y = new Box(BoxLayout.Y_AXIS);
        y.add(Box.createVerticalStrut(15));
        y.add(Box.createVerticalGlue());
        y.add(l);
        y.add(Box.createVerticalGlue());
        y.add(Box.createVerticalStrut(5));
        y.add(f);
        y.add(Box.createVerticalStrut(5));
        y.add(getBox(inputL, inputF));
        y.add(getBox(passwL, passwF));
        y.add(getBox(dbhostL, dbhostF));
        y.add(getBox(dbnameL, dbnameF));
        y.add(Box.createVerticalStrut(15));
        y.add(b);
        y.add(Box.createVerticalGlue());
        y.add(Box.createVerticalStrut(10));
        
        Box x = new Box(BoxLayout.X_AXIS);
        x.add(Box.createHorizontalStrut(10));
        x.add(y);
        x.add(Box.createHorizontalStrut(10));
        
        getContentPane().add(x);
        pack();
    } //
    
    private Box getBox(JComponent c1, JComponent c2) {
        c1.setPreferredSize(new Dimension(LABELW,20));
        c1.setMaximumSize(new Dimension(LABELW,20));
        c2.setPreferredSize(new Dimension(FIELDW,20));
        c2.setMaximumSize(new Dimension(FIELDW,20));
        
        Box b = new Box(BoxLayout.X_AXIS);
        b.add(Box.createHorizontalGlue());
        b.add(c1);
        b.add(Box.createHorizontalStrut(10));
        b.add(c2);
        b.add(Box.createHorizontalGlue());
        
        b.setPreferredSize(new Dimension(LABELW+FIELDW,30));
        b.setMaximumSize(new Dimension(LABELW+FIELDW,30));
        return b;
    } //
    
    public void setLoggedIn(boolean s) {
        login = s;
    } //
    
    public boolean isLoggedIn() {
        return login;
    } //
    
    public void setError(String msg) {
        failL.setText(msg);
        failL.validate();
    } //
    
    public String[] getLoginInfo() {
        return new String[] { 
            curFields[0].getText(),
            curFields[1].getText(), 
            curFields[2].getText(), 
            curFields[3].getText()        
        };
    } //
    
    public String[] loadLoginProperties() {
        String[] props = new String[] {"","","",""};
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("login.properties"));
            props[0] = prop.getProperty("login","");
            props[1] = prop.getProperty("password","");
            props[2] = prop.getProperty("dbhost","");
            props[3] = prop.getProperty("dbname","");
        } catch ( Exception e ) {}
        return props;
    } //
    
    public void saveLoginProperies(String[] props) {
        Properties prop = new Properties();
        prop.setProperty("login",    props[0]);
        prop.setProperty("password", props[1]);
        prop.setProperty("dbhost",   props[2]);
        prop.setProperty("dbname",   props[3]);
        try {
            prop.save(new FileOutputStream("login.properties"),
                "Login Properties");
        } catch ( Exception e ) {}
    } //
    
} // end of class LoginDialog
