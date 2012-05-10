package knowevo.myvizster.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import knowevo.myvizster.Vizster;

/**
 * ProfileLookupDialog
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class ProfileLookupDialog extends JDialog {

    private String[] FIELDS = 
        new String[] {"uid", "name", "age", "location", "hometown",
            "occupation", "interests", "music"};
//        new String[] {"uid", "name", "age", "location", "hometown",
//            "occupation", "interests", "music", "books", "tvshows",
//            "movies", "about", "want_to_meet"};
    
    private int MAX_HITS = 200;
    
    private JLabel      counter;
    private JTable	    results;
    private JTextField  searchField;
    private JButton	    searchButton;
    private JButton     loadButton;
    private JButton     cancelButton;
    private JScrollPane scroller;
    
    private Vizster vizster;
    private LookupTableModel tableModel;
    
    private Directory     directory;
    private Analyzer      analyzer;
    private IndexSearcher searcher;
    private IndexReader   reader;    
    
    public ProfileLookupDialog(Vizster vizster, Directory dir) {
        setTitle("Profile Search");
        
        this.vizster = vizster;
        directory = dir;
        analyzer = new StandardAnalyzer();
        
        // open the reader and searcher
        try {
	        reader = IndexReader.open(dir);
	        searcher = new IndexSearcher(reader);
	    } catch ( Exception e ) {
	        e.printStackTrace();
	    }
	    
	    tableModel = new LookupTableModel();
	    
	    initUI();
	    pack();
    } //
    
    private void initUI() {
        counter      = new JLabel("No Matches");
        results      = new JTable(tableModel);
        searchField  = new JTextField();
        searchButton = new JButton("Search");
        loadButton   = new JButton("Load Profile");
        cancelButton = new JButton("Cancel");
        
        JPanel p = new JPanel();
        p.add(counter);
        
        Box topBox = new Box(BoxLayout.X_AXIS);
        topBox.add(Box.createHorizontalStrut(10));
        topBox.add(p);
        topBox.add(Box.createHorizontalStrut(5));
        topBox.add(Box.createHorizontalGlue());
        topBox.add(searchField);
        topBox.add(Box.createHorizontalStrut(5));
        topBox.add(searchButton);
        topBox.add(Box.createHorizontalGlue());
        topBox.add(Box.createHorizontalStrut(10));

        scroller = new JScrollPane(results);
        scroller.setPreferredSize(new Dimension(600,400));
        
        Box botBox = new Box(BoxLayout.X_AXIS);
        botBox.add(Box.createHorizontalGlue());
        botBox.add(Box.createHorizontalStrut(5));
        botBox.add(loadButton);
        botBox.add(Box.createHorizontalStrut(5));
        botBox.add(cancelButton);
        botBox.add(Box.createHorizontalStrut(5));
        
        Container c = this.getContentPane();
        c.add(topBox,   BorderLayout.NORTH);
        c.add(scroller, BorderLayout.CENTER);
        c.add(botBox,   BorderLayout.SOUTH);
        
        AbstractAction searcher = new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                search(searchField.getText());
            } //
        };
        searchField.addActionListener(searcher);
        searchButton.addActionListener(searcher);
        
        loadButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                int sel = results.getSelectedRow();
                if ( sel < 0 ) return;
                String uid = (String)results.getValueAt(sel,1);
                ProfileLookupDialog.this.setVisible(false);
                vizster.loadGraph(null, uid);
            } //
        });
        
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                ProfileLookupDialog.this.setVisible(false);
            } //
        });
    } //
    
    public void search(String query) {
        try {
            Query q = MultiFieldQueryParser.parse(query, FIELDS, analyzer);
            Hits hits = searcher.search(q);
            if ( hits.length() == 0 ) {
                counter.setText("No Matches");
            } else if ( hits.length() == 1 ) {
                counter.setText("1 Match");
            } else {
                counter.setText(hits.length()+" Matches");
            }
            tableModel.setHits(hits);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } //
    
    private String rewrite(String query) {
        String[] toks = query.split(" ");
        StringBuffer sbuf = new StringBuffer();
        for ( int i=0; i<toks.length; i++ ) {
            if ( i > 0 )
                sbuf.append(" AND ");
            sbuf.append(toks[i]);
        }
        return sbuf.toString();
    } //
    
    public class LookupTableModel extends AbstractTableModel {
        private Hits hits;
        
        public Hits getHits() {
            return hits;
        } //
        
        public void setHits(Hits hits) {
            this.hits = hits;
            this.fireTableStructureChanged();
            this.fireTableDataChanged();
        } //
        
        public int getRowCount() {
            return (hits == null ? 0 : Math.min(hits.length(), MAX_HITS));
        } //
        
        public int getColumnCount() {
            return FIELDS.length;
        } //
        
        public Class getColumnClass(int i) {
            return Object.class;
        } //
        
        public Object getValueAt(int x, int y) {
            try {
                Document d = hits.doc(x);
                return d.getField(FIELDS[y]).stringValue();
            } catch (Exception e) {
                return "";
            }
        } //

        public String getColumnName(int i) {
            return FIELDS[i];
        } //
        
        public boolean isCellEditable(int x, int y) {
            return false;
        } //
    } // end of inner class LookupTableModel
    
} // end of class ProfileLookupDialog
