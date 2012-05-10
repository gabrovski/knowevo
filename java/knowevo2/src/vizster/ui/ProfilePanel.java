package vizster.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;

import prefusex.lucene.LuceneSearcher;
import prefusex.lucene.TextSearchFocusSet;
import vizster.Vizster;
import vizster.action.ColorAction;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.event.FocusEvent;
import edu.berkeley.guir.prefuse.event.FocusListener;
import edu.berkeley.guir.prefuse.graph.Entity;

/**
 * Displays profile contents and controls for visualizing individual
 * attributes.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class ProfilePanel extends JPanel {

    private JLabel[]        labels;
    private JTextArea[]     values;
    private JToggleButton[] colorers;
    
    //ColorLib.getColor(255,142,102);
    //ColorLib.getColor(255,192,152);
    
    private Color rowBackground = new Color(216,221,250);
    private Color highlight = new Color(255,252,102);
    
    private Vizster vizster;
    private Entity curProfile;
    private TextSearchFocusSet searchSet;
    private LuceneSearcher searcher;
    private HighlightPainter hlp = 
        new DefaultHighlighter.DefaultHighlightPainter(highlight);
    
    private final char[] words = {',','.','?',' '};
    private final char[] phrase = {',','.','?'};
    private char[] limiters = words;
    
    public ProfilePanel(Vizster vizster) {
        this.vizster = vizster;
        setBackground(Color.WHITE);
        initUI();
        
        FocusManager fmanager = vizster.getItemRegistry().getFocusManager();
        searchSet = (TextSearchFocusSet)fmanager.getFocusSet(Vizster.SEARCH_KEY);
        searchSet.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                updateTextHighlight();
            } //
        });
        searcher = searchSet.getLuceneSearcher();
    } //
    
    private void initUI() {
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        Font f = new Font("SansSerif",Font.BOLD,12);
        
        ColorAction colorAction = new ColorAction(vizster);
        ButtonGroup buttG = new ButtonGroup();
        JToggleButton inv = new JCheckBox();
        buttG.add(inv);
        
        Dimension d = new Dimension(22,17);
        colorers = new JToggleButton[LABEL.length];
        for ( int i=1; i < LABEL.length; i++ ) {
            int attr = VizsterXRayColorFunction.getAttributeIndex(ATTR[i]);
            if ( attr != -1 ) {
                colorers[i] = new JCheckBox();
                colorers[i].putClientProperty("attr",new Integer(attr));
                colorers[i].putClientProperty("inv", inv);
                colorers[i].addActionListener(colorAction);
                buttG.add(colorers[i]);
            } else {
                colorers[i] = new JToggleButton() {
                    public void paintComponent(Graphics g) {}
                };
                colorers[i].setEnabled(false);
            }
            colorers[i].setMaximumSize(d);
            colorers[i].setPreferredSize(d);
            colorers[i].setBackground(Color.WHITE);
        }
        
        // causes clicks on the label to forward to the checkbox
        MouseListener mL = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if ( !SwingUtilities.isLeftMouseButton(e) ) return;
                Object src = e.getSource();
                for ( int i=1; i<labels.length; i++) {
                    if ( src == labels[i] )
                        colorers[i].doClick();
                }
            } //
        };
        labels = new JLabel[LABEL.length];
        for ( int i=1; i < LABEL.length; i++ ) {
            labels[i] = new JLabel(LABEL[i]);
            labels[i].setHorizontalAlignment(SwingConstants.RIGHT);
            labels[i].setVerticalAlignment(SwingConstants.TOP);
            labels[i].setFont(f);
            if ( i%2 != 0 )
                labels[i].setBackground(rowBackground);
            labels[i].addMouseListener(mL);
        }
        
        f = new Font("SansSerif",Font.PLAIN,12);
        
        values = new JTextArea[ATTR.length];
        for ( int i=1; i < ATTR.length; i++ ) {
            values[i] = new JTextArea();
            values[i].setSelectedTextColor(Color.BLACK);
            values[i].setSelectionColor(highlight);
            values[i].setColumns(15);
            values[i].setLineWrap(true);
            values[i].setWrapStyleWord(true);
            values[i].setFont(f);
            values[i].setEditable(false);
            if ( i%2 != 0 )
                values[i].setBackground(rowBackground);
            //values[i].addCaretListener(caret);
            values[i].addMouseListener(mouse);
            values[i].addMouseMotionListener(mouse);
        }
        
        values[0] = new JTextArea();
        values[0].setEditable(false);
        values[0].setFont(new Font("SansSerif",Font.BOLD,32));
        Box b = new Box(BoxLayout.X_AXIS);
        b.add(Box.createHorizontalGlue());
        b.add(values[0]);
        b.add(Box.createHorizontalGlue());
        
        
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        gbl.setConstraints(b, c);
        add(b);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        for ( int i=1; i < LABEL.length; i++ ) {
            addAttribute(i,gbl,c);
        }
    } //
    
    private void addAttribute(int i, GridBagLayout gbl, GridBagConstraints c) {
        c.gridwidth = 1;
        
        gbl.setConstraints(labels[i], c);
        add(labels[i]);
        
        gbl.setConstraints(colorers[i],c);
        add(colorers[i],c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(values[i], c);
        add(values[i]);
    } //
    
    public void updatePanel(Entity e) {
        curProfile = e;
        for ( int i=ATTR.length-1; i >= 0; i-- ) {
            String val = e.getAttribute(ATTR[i]);
            if ( val == null ) val = "??";
            values[i].setText(val);
        }
        updateTextHighlight();
    } //
    
    private void updateTextHighlight() {
        for ( int i=ATTR.length-1; i >= 0; i--)
            values[i].getHighlighter().removeAllHighlights();
        if ( curProfile != null && searchSet.contains(curProfile) ) {
            String query = cleanQuery(searchSet.getQuery());
            for ( int i=ATTR.length-1; i >= 0; i--)
                updateTextHighlight(values[i], query);
        }
    } //
    
    private String cleanQuery(String q) {
        q = q.toLowerCase();
        if ( q.startsWith("\"") && q.endsWith("\"") ) {
            q = q.substring(1,q.length()-1);
        }
        return q;
    } //
    
    private void updateTextHighlight(JTextArea value, String query) {
        String text = value.getText().toLowerCase();
        Highlighter hl = value.getHighlighter();
        int idx = 0, len = query.length();
        while ( (idx=text.indexOf(query, idx)) != -1 && 
                (idx==0 || isLimiter(text.charAt(idx-1)))) {
            try {
                hl.addHighlight(idx, idx+len, hlp);
            } catch ( Exception e ) {}
            idx += len;
        }
    } //
    
    
    private boolean getWord(JTextComponent jtc, Point p, int[] span) {
        String txt = jtc.getText();
        int idx = jtc.viewToModel(p);
        int len = jtc.getText().length();
        
        int start = idx-1, end = idx;
        // grow bounds in both directions
        for ( ; start >= 0 && !isLimiter(txt.charAt(start)); start-- );
        start++;
        for ( ; end < len && !isLimiter(txt.charAt(end)); end++ );

        if ( start < end ) {
            // trim string so no leading/trailing spaces
            for ( ; txt.charAt(start) == ' '; start++);
            for ( ; txt.charAt(end-1) == ' '; end--);
            // set span for return value
            span[0] = start;
            span[1] = end;
            return true;
        } else {
            return false;
        }
    } //
    
    private boolean isLimiter(char c) {
        for ( int i=0; i<limiters.length; i++ ) {
            if ( c == limiters[i] )
                return true;
        }
        return false;
    } //
    
    private void setLimiters(char[] limiters) {
        this.limiters = limiters;
    } //
    
//    private CaretListener caret = new CaretListener() {
//        public void caretUpdate(CaretEvent evt) {
//            JTextComponent src = (JTextComponent)evt.getSource();
//            int dot = evt.getDot(), mark = evt.getMark();
//            if ( evt.getDot() != evt.getMark() ) {
//                vizster.search(src.getSelectedText());
//            }
//        } //
//    };
    
    private MouseInputAdapter mouse = new MouseInputAdapter() {
        private boolean drag;
        private int[] span = new int[2];
        
        public void mouseEntered(MouseEvent evt) {
            JTextComponent src = (JTextComponent)evt.getSource();
            src.requestFocus();
        } //
        
        public void mouseMoved(MouseEvent evt) {
            JTextComponent src = (JTextComponent)evt.getSource();
            Point p = evt.getPoint();
            if ( !getWord(src,p,span) ) {
                return;
            }
            try {
	            String query = src.getText(span[0],span[1]-span[0]);
	            if ( searcher.numHits(query) > 1 ) {
	                Caret c = src.getCaret();
	                c.setDot(span[0]);
	                c.moveDot(span[1]);
	            }
            } catch ( Exception e ) {
                // do nothing
            }
        } //
        
        public void mouseClicked(MouseEvent evt) {
            JTextComponent src = (JTextComponent)evt.getSource();
            Point p = evt.getPoint();
            setLimiters((evt.getClickCount()==2 ? phrase : words));
            if ( getWord(src,p,span) ) {
                try {
                    String txt = src.getText(span[0],span[1]-span[0]);
                    setLimiters(words);
                    if ( txt.indexOf(" ") != -1 )
                        txt = "\""+txt+"\"";
                    vizster.search(txt);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        } //
        
        public void mouseDragged(MouseEvent evt) {
            drag = true;
        } //
        
        public void mouseReleased(MouseEvent evt) {
            if ( drag ) {
                JTextComponent src = (JTextComponent)evt.getSource();
                Caret c = src.getCaret();
                if ( c.getDot() != c.getMark() ) {
                    String txt = src.getSelectedText();
                    if ( txt.indexOf(" ") != -1 )
                        txt = "\""+txt+"\"";
                    vizster.search(txt);
                }
                drag = false;
            }
        } //
    };
    
    public static final String[] ATTR = {
        "name",
        "uid",
        "nfriends",
        "age",
        "gender",
        "status",
/*        "interested_in",
        "preference",*/
        "location",
        "hometown",
        "occupation",
        "interests",
        "music",
        "books",
        "tvshows",
        "movies",
        "membersince",
        "lastlogin",
        "lastmod",
        "about",
        "want_to_meet"
    };
    
    public static final String[] LABEL = {
        "Name",
        "User ID",
        "Friends",
        "Age",
        "Gender",
        "Status",
/*        "interested_in",
        "preference",*/
        "Location",
        "Hometown",
        "Occupation",
        "Interests",
        "Music",
        "Books",
        "TV Shows",
        "Movies",
        "Member Since",
        "Last Login",
        "Last Updated",
        "About",
        "Want to Meet"
    };
    
// Attributes are...
//    "uid",
//    "name",
//    "age",
//    "gender",
//    "status",
//    "interested_in",
//    "preference",
//    "location",
//    "hometown",
//    "occupation",
//    "interests",
//    "music",
//    "books",
//    "tvshows",
//    "movies",
//    "membersince",
//    "lastlogin",
//    "lastmod",
//    "about",
//    "want_to_meet",
//    "photourl"
    
} // end of class ProfilePanel
