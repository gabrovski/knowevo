package knowevo.myvizster;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.graph.external.DatabaseLoader;
import edu.berkeley.guir.prefuse.graph.external.ExternalNode;
import edu.berkeley.guir.prefuse.graph.external.ExternalTreeNode;
import edu.berkeley.guir.prefuse.graph.external.GraphLoader;

/**
 * Loads friendster data from an external database using prefuse's
 * preliminary external data management classes.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
public class VizsterDatabaseLoader extends DatabaseLoader {

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
    
    public static final String eQuery
        = "select * from graph where uid1 = ?";

    /**
     * The default set of profile entries to load
     */
    public static final String[] DEFAULT_COLUMNS =
        {"uid", "name", "location", "age", "photourl"};
    
    /**
     * A set of all the available friendster profile attributes
     */
    public static final String[] ALL_COLUMNS =
        {"uid", "name", "location", "age", "gender", "status", 
         "interested_in", "preference", "location", "hometown",
         "occupation", "interests", "music", "books", "tvshows",
         "movies", "membersince", "lastlogin", "lastmod", "about",
         "want_to_meet", "photourl"};
    
    /**
     * Creates a new loader to load items from a database retrieving a default
     * set of profile entries for each loaded profile.
     * @param registry the ItemRegistry to load to
     */
    public VizsterDatabaseLoader(ItemRegistry registry) {
        this(registry, DEFAULT_COLUMNS);
    } //
    
    /**
     * Creates a new loader to load items from a database retrieving the given
     * set of profile entries for each loaded profile.
     * @param registry the ItemRegistry to load to
     * @param columns the names of the profile entries to include
     */
    public VizsterDatabaseLoader(ItemRegistry registry, String columns[])  {
        super(registry, columns);
        try {
            setNeighborQuery(nQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        node = loadNode(GraphLoader.LOAD_NEIGHBORS,rs,null);
        return node;
    } //
    
    protected void prepareNeighborQuery(PreparedStatement s, ExternalNode n) {
        try {
            s.clearParameters();
            int id = Integer.parseInt(n.getAttribute(m_keyField));
            s.setInt(1, id);
        } catch ( SQLException e ) { e.printStackTrace(); }
    } //
    
    protected void prepareChildrenQuery(PreparedStatement s, ExternalTreeNode n) {
        throw new UnsupportedOperationException(
                "This loader doesn't handle tree structures");
    } //
    
    protected void prepareParentQuery(PreparedStatement s, ExternalTreeNode n) {
        throw new UnsupportedOperationException(
                "This loader doesn't handle tree structures");
    } //

} // end of class VizsterDatabaseLoader
