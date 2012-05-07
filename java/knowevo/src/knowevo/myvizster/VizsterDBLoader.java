package knowevo.myvizster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.graph.DefaultEdge;
import edu.berkeley.guir.prefuse.graph.DefaultNode;
import edu.berkeley.guir.prefuse.graph.Edge;
import edu.berkeley.guir.prefuse.graph.Entity;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.graph.event.GraphLoaderListener;
import edu.berkeley.guir.prefuse.graph.event.GraphLoaderMulticaster;

/**
 * Custom database loader that allows for more fine-grain control over
 * which data is loaded from the database. This is much more memory
 * efficient that using the alternative VizsterDatabaseLoader class.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterDBLoader {

	private static String PHOTOBASE = "file:///c:/vizster/photos/";
	
    /**
     * Query for retrieving a single profile.
     */
    public static final String sQuery 
        = "select * from profiles where uid = ";
    
    /**
     * Query for retrieving all friends of a person
     */
    public static final String nQuery 
        = "select profiles.* from profiles, graph where " +
            "(graph.uid1 = ? AND profiles.uid = graph.uid2)";
    
    /**
     * Query for retrieving all edges incident on all friends of a person
     */
    public static final String eQuery
        = "select g1.* from graph as g1 " +
            "left join graph as g2 on g1.uid1 = g2.uid2 where g2.uid1 = ?";

    /**
     * The default set of profile entries to load
     */
    public static final String[] DEFAULT_COLUMNS =
        {"uid", "name", "location", "age", "photourl"};
    
    /**
     * A set of all the available friendster profile attributes
     */
    public static final String[] ALL_COLUMNS =
        {"uid", "name", "nfriends", "age", "gender", "status", 
         "interested_in", "preference", "location", "hometown",
         "occupation", "interests", "music", "books", "tvshows",
         "movies", "membersince", "lastlogin", "lastmod", "about",
         "want_to_meet", "photourl"};
    
    protected String m_keyField = "uid";
    protected int m_maxSize = 50000;
    protected LinkedHashMap m_cache = new LinkedHashMap(m_maxSize,.75f,true) {
        public boolean removeEldestEntry(Map.Entry eldest) {
            return evict((Entity)eldest.getValue());
        }
    };
    protected GraphLoaderListener m_listener;
    protected Graph m_graph;
    protected ItemRegistry m_registry;
    protected boolean isConnected = false;
    
    private final String m_columns[];
    
    protected String m_neighborQuery;
    protected String m_edgeQuery;
    
    private Vector m_liveQueries;
    
    private Connection m_db;
    private PreparedStatement m_ns, m_es;
    
    /**
     * Creates a new loader to load items from a database retrieving a default
     * set of profile entries for each loaded profile.
     * @param registry the ItemRegistry to load to
     */
    public VizsterDBLoader(ItemRegistry registry) {
        this(registry, DEFAULT_COLUMNS);
    } //
    
    /**
     * Creates a new loader to load items from a database retrieving the given
     * set of profile entries for each loaded profile.
     * @param registry the ItemRegistry to load to
     * @param columns the names of the profile entries to include
     */
    public VizsterDBLoader(ItemRegistry registry, String columns[])  {
        m_registry = registry;
        m_graph = registry.getGraph();
        m_columns = columns;
        m_liveQueries = new Vector();
        try {
            setNeighborQuery(nQuery);
            setEdgeQuery(eQuery);
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    } //
    
    public void clear() {
        while ( !m_liveQueries.isEmpty() ) {
            Thread t = (Thread)m_liveQueries.remove(m_liveQueries.size()-1);
            t.stop();
        }
        m_cache.clear();
    }
    
    public String[] getColumns() {
        return m_columns;
    } //
    
    public void loadNeighbors(final Node n) {
        Thread t = new Thread("VizsterDBLoader-"+n.getAttribute("uid")) {
            public void run() {
                PreparedStatement ns, es;
                try {
	                ns = prepare(m_neighborQuery);
	                es = prepare(m_edgeQuery);
                } catch ( SQLException sqle ) {
                    sqle.printStackTrace();
                    return;
                }
                prepareQuery(ns, n);
                loadNodes(ns, n);
                prepareQuery(es, n);
                loadEdges(es, n);
                m_liveQueries.remove(this);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        m_liveQueries.add(t);
        t.start();
    } //
    
    public void loadNetwork(final Node n, final int hops) {
        Thread t = new Thread() {
            public void run() {
                PreparedStatement ns, es;
                try {
	                ns = prepare(m_neighborQuery);
	                es = prepare(m_edgeQuery);
                } catch ( SQLException sqle ) {
                    sqle.printStackTrace();
                    return;
                }
                
		        ArrayList queue = new ArrayList();
		        ArrayList queue2 = new ArrayList();
		        queue.add(n);
		        for ( int i=0; i<hops; i++ ) {
		            while ( !queue.isEmpty() ) {
		                // query for neighbors and their edges
		                Node qn = (Node)queue.remove(queue.size()-1);
		                prepareQuery(ns, qn);
		                loadNodes(ns, qn);
		                prepareQuery(es, qn);
		                loadEdges(es, qn);
		                // load neighbors for next round of querying
		                if ( i < hops-1 ) {
		                    Iterator iter = qn.getNeighbors();
		                    while ( iter.hasNext() ) {
		                        queue2.add(iter.next());
		                    }
		                }
		            }
		            ArrayList tmp = queue;
		            queue = queue2;
		            queue2 = tmp;
		        }
		        m_liveQueries.remove(this);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        m_liveQueries.add(t);
        t.start();
    } //
    
    private void loadNodes(PreparedStatement s, Node src) {
        try {
            ResultSet rs = s.executeQuery();
            while ( rs.next() )
                loadNode(rs, src);
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    } //
    
    public Node loadNode(ResultSet rs, Node src) throws SQLException {
        String uid = null;
    	Node node = null;
        for ( int i=0; i<m_columns.length; i++ ) {
            String value = rs.getString(m_columns[i]);
            if ( i == 0 ) {
            	uid = value;
                Node n = (Node)m_cache.get(value);
                if ( n != null ) return n;
                node = new DefaultNode();
            }
            if ( value != null ) {
                value = value.replaceAll("\r","");
                node.setAttribute(m_columns[i], value);
            }
        }
        
        // hard override on photo lookup
        int len = uid.length();
        node.setAttribute("photo",PHOTOBASE+uid.charAt(len-1)+uid.charAt(len-2)+"/"+uid+".jpg");
        
        foundNode(src, node, null);
        return node;
    } //
    
    protected void foundNode(Node src, Node n, Edge e) {
        boolean inCache = false;
        String key = n.getAttribute(m_keyField);
        synchronized ( m_cache ) {
	        if ( m_cache.containsKey(key) ) {
	            // switch n reference to original loaded version 
	            n = (Node)m_cache.get(key);
	            inCache = true;
	        } else
	            m_cache.put(key, n);
        }
        
        if (e == null && src != null )
            e = new DefaultEdge(src, n, m_graph.isDirected());
        
        synchronized ( m_registry ) {
            m_graph.addNode(n);
            if ( src != null )
                m_graph.addEdge(e);
        }
        
        if ( m_listener != null && !inCache )
            m_listener.entityLoaded(null,n);
    } //
    
    private void loadEdges(PreparedStatement s, Node src) {
        try {
            ResultSet rs = s.executeQuery();
            while ( rs.next() )
                loadEdge(rs);
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    } //
    
    public Edge loadEdge(ResultSet rs) throws SQLException {
        int id1 = rs.getInt("uid1");
        int id2 = rs.getInt("uid2");
        if ( id2 < id1 ) {
            // swap to make sure lesser node is on the left 
            int tmp = id2;
            id2 = id1;
            id1 = tmp;
        }
        Node source = (Node)m_cache.get(String.valueOf(id1));
        Node target = (Node)m_cache.get(String.valueOf(id2));
        if ( source == null || target == null ) return null;
        Edge e = new DefaultEdge(source, target, m_graph.isDirected());
        boolean add = false;
        synchronized ( m_registry ) {
            add = m_graph.addEdge(e);
        }
        if ( add )
            m_listener.entityLoaded(null,e);
        return e;
    } //
    
    
    /**
     * Returns the node associated with the given user id value, loading it
     * from the database as necessary
     * @param uid the user id of the profile to retrieve
     * @return a Node representing the requested profile
     * @throws SQLException if an error occurs while talking to the database
     */
    public Node getProfileNode(String uid) throws SQLException {
        Node node = null;
        String sid = String.valueOf(uid);
        
        // return the node if it's already in the cache
        if ( (node=(Node)m_cache.get(sid)) != null )
            return node;
        
        // otherwise load the node from the database
        Statement s = getConnection().createStatement();
        ResultSet rs = s.executeQuery(sQuery+uid); rs.first();
        node = loadNode(rs,null);
        return node;
    } //
    
    protected void prepareQuery(PreparedStatement s, Node n) {
        try {
            s.clearParameters();
            int id = Integer.parseInt(n.getAttribute(m_keyField));
            s.setInt(1, id);
        } catch ( SQLException e ) { e.printStackTrace(); }
    } //
    
    public void setMaximumCacheSize(int size) {
        m_maxSize = size;
    } //
    
    public int getMaximumCacheSize() {
        return m_maxSize;
    } //
    
    public void addGraphLoaderListener(GraphLoaderListener l) {
        m_listener = GraphLoaderMulticaster.add(m_listener, l);
    } //
    
    public void removeGraphLoaderListener(GraphLoaderListener l) {
        m_listener = GraphLoaderMulticaster.remove(m_listener, l);
    } //
    
    public void touch(Entity e) {
        m_cache.get(e.getAttribute(m_keyField));
    } //
    
    public boolean evict(Entity eldest) {
        boolean b = m_cache.size()>m_maxSize;
        if ( b && m_listener != null )
            m_listener.entityUnloaded(null, eldest);
        if ( b ) {
            m_graph.removeNode((Node)eldest); 
        }
        return b;
    } //
    
    public void connect(String driver, String url, String user, String password)
    throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
    {
        Class.forName(driver).newInstance();
        m_db = DriverManager.getConnection(url, user, password);
        if ( m_neighborQuery != null )
            m_ns = prepare(m_neighborQuery);
        if ( m_edgeQuery != null )
            m_es = prepare(m_edgeQuery);
        isConnected = true;
    } //
    
    public Connection getConnection() {
        return m_db;
    } //
    
    private PreparedStatement prepare(String query) throws SQLException {
        if ( query == null )
            throw new IllegalArgumentException("Input query must be non-null");
        if ( m_db == null )
            throw new IllegalStateException("Connection to database not yet"
                    + " established! Make sure connect() is called first.");
        
        return m_db.prepareStatement(query);
    }
    
    public void setNeighborQuery(String query) throws SQLException {
        if ( m_db != null )
            m_ns = prepare(query);
        m_neighborQuery = query;
    } //
    
    public String getNeighborQuery() {
        return m_neighborQuery;
    } //
    
    public void setEdgeQuery(String query) throws SQLException {
        if ( m_db != null )
            m_es = prepare(query);
        m_edgeQuery = query;
    } //
    
    public String getEdgeQuery() {
        return m_edgeQuery;
    } //

    public boolean isConnected() {
        return isConnected;
    } //
    
} // end of class VizsterDatabaseLoader
