package vizster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import prefusex.community.CommunitySet;
import prefusex.lucene.TextSearchFocusSet;
import prefusex.lucene.TextSearchPanel;
import vizster.action.AuraFilter;
import vizster.action.HighlightAction;
import vizster.action.HighlightSettingAction;
import vizster.action.InvertToggleAction;
import vizster.action.linkage.LinkageFilter;
import vizster.action.linkage.ReleaseFixedAction;
import vizster.action.linkage.VizsterCircleLayout;
import vizster.controls.ExpansionControl;
import vizster.controls.FocusRequester;
import vizster.controls.HighlightControl;
import vizster.controls.HighlightHoldControl;
import vizster.controls.LinkageControl;
import vizster.controls.ZoomStepControl;
import vizster.render.VizsterRendererFactory;
import vizster.ui.CommunityConstructor;
import vizster.ui.CommunityEdgeLabeler;
import vizster.ui.CommunityFilter;
import vizster.ui.CommunityLayout;
import vizster.ui.CommunityPanel;
import vizster.ui.DecoratorItem;
import vizster.ui.Legend;
import vizster.ui.ProfilePanel;
import vizster.ui.VizsterBrowsingColorFunction;
import vizster.ui.VizsterFontFunction;
import vizster.ui.VizsterMenuBar;
import vizster.ui.VizsterSizeFunction;
import vizster.ui.VizsterXRayColorFunction;
import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.action.AbstractAction;
import edu.berkeley.guir.prefuse.action.Action;
import edu.berkeley.guir.prefuse.action.ActionMap;
import edu.berkeley.guir.prefuse.action.ActionSwitch;
import edu.berkeley.guir.prefuse.action.RepaintAction;
import edu.berkeley.guir.prefuse.action.animate.LocationAnimator;
import edu.berkeley.guir.prefuse.action.animate.PolarLocationAnimator;
import edu.berkeley.guir.prefuse.action.animate.SizeAnimator;
import edu.berkeley.guir.prefuse.action.assignment.Layout;
import edu.berkeley.guir.prefuse.action.filter.FisheyeGraphFilter;
import edu.berkeley.guir.prefuse.action.filter.GarbageCollector;
import edu.berkeley.guir.prefuse.activity.ActionList;
import edu.berkeley.guir.prefuse.activity.SlowInSlowOutPacer;
import edu.berkeley.guir.prefuse.event.FocusEvent;
import edu.berkeley.guir.prefuse.event.FocusListener;
import edu.berkeley.guir.prefuse.focus.DefaultFocusSet;
import edu.berkeley.guir.prefuse.focus.FocusSet;
import edu.berkeley.guir.prefuse.graph.DefaultGraph;
import edu.berkeley.guir.prefuse.graph.Entity;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.GraphLib;
import edu.berkeley.guir.prefuse.graph.Node;
import edu.berkeley.guir.prefuse.util.display.DisplayLib;
import edu.berkeley.guir.prefusex.controls.DragControl;
import edu.berkeley.guir.prefusex.controls.FocusControl;
import edu.berkeley.guir.prefusex.controls.PanControl;
import edu.berkeley.guir.prefusex.controls.ZoomControl;
import edu.berkeley.guir.prefusex.force.ForceSimulator;
import edu.berkeley.guir.prefusex.layout.ForceDirectedLayout;
import edu.berkeley.guir.prefusex.layout.FruchtermanReingoldLayout;

/**
 * An application for visual exploration of the friendster social networking
 * service.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class Vizster extends JFrame {
	
	/**
     * A set of all the available friendster profile attributes
     */
    public static final String[] ALL_COLUMNS =
        {"uid", "name", "nfriends", "age", "gender", "status", 
         "interested_in", "preference", "location", "hometown",
         "occupation", "interests", "music", "books", "tvshows",
         "movies", "membersince", "lastlogin", "lastmod", "about",
         "want_to_meet", "photourl"};

    // default starting friendster user id
    public static final String DEFAULT_START_UID = "186297";
    public static final String ID_FIELD = "uid";
    
    // keys for additional focus sets
    public static final String CLICK_KEY  = "clicked";
    public static final String MOUSE_KEY  = "moused";
    public static final String SEARCH_KEY = "search";
    public static final String HIGHLIGHT_KEY = "highlight";
    public static final String COMMUNITY_KEY = "community";
    
    // names of additional item classes
    public static final String AURA_CLASS = "aura";
    
    // modes the interface can be in
    public static final int BROWSE_MODE = 0;
    public static final int COMPARE_MODE = 1;
    
    // prefuse architecture components
    private ItemRegistry registry;
    private ActionList redraw, forces, altForces, altAnimate, filter, magnify;
    private ActionList aura, community, highlight, linkLayout, unfix, border;    
    private boolean xrayMode = false;
    private VizsterRendererFactory renderers;
    private ActionMap actionMap;
    
    // control if layout remains animated
    private boolean animate = true;
    
    // ui components
    private VizsterDisplay display;
    private ProfilePanel profilePanel;
    private JPanel container;
    private TextSearchPanel searchPanel;
    private CommunityPanel communityPanel;
    
    // number of login attempts before application exits
    private int loginRetries = 5;
    
    public static Vizster main(String file) {
    	VizsterLib.setLookAndFeel();
        //String startUID = argv.length > 0 ? argv[0] : DEFAULT_START_UID;
        String startUID = DEFAULT_START_UID;
        if(file == null || file.isEmpty()) {
        	//TODO: If file is null, load with sample file
        	file = null;
        }
        return new Vizster(startUID, file);
    }
    
    /**
     * Launches the Vizster application
     * @param argv this app takes one optional argument - a friendster user id
     *  to show upon launch.
     */
    public static void main(String[] argv) {
    	String file = argv.length > 0 ? argv[0] : null;
    	main(file);
    } //
    
    /**
     * Construct a new Vizster application instance.
     */
    public Vizster() {
        this(DEFAULT_START_UID, null);
    } //
    
    /**
     * Construct a new Vizster application instance.
     * @param startUID the user id to show first
     */
    public Vizster(String startUID) {
        this(startUID, null);
    } //
    
    /**
     * Construct a new Vizster application instance.
     * @param startUID the user id to show first
     * @param datafile the data file to use, if null, 
     *   a connection dialog for a database will be provided 
     */
    public Vizster(String startUID, String datafile) {
        super("Vizster");       
        
        // create the registry
        registry = new ItemRegistry(new DefaultGraph());
        registry.setItemComparator(new VizsterItemComparator());
        registry.addItemClass(AURA_CLASS, DecoratorItem.class);
        
        // initialize focus handling
        // -We already get a default focus set, use it for double-clicked nodes
        // -Add another set for nodes that are single-clicked, to show profiles
        // -Add another set for nodes moused-over
        // -Add another set for controlling highlight status
        // -Add another set for sets of automatically-determined communities
        // -Add another set for keyword search hits
        FocusManager fmanager = registry.getFocusManager();
        fmanager.putFocusSet(CLICK_KEY, new DefaultFocusSet());
        fmanager.putFocusSet(MOUSE_KEY, new DefaultFocusSet());
        fmanager.putFocusSet(HIGHLIGHT_KEY, new DefaultFocusSet());
        fmanager.putFocusSet(COMMUNITY_KEY, new CommunitySet(true));
        final TextSearchFocusSet searchSet = new TextSearchFocusSet();
        fmanager.putFocusSet(SEARCH_KEY, searchSet);
        
        // initialize user interface components
        // set up the primary display
        display = new VizsterDisplay(this);
        display.setSize(700,650);
        // create the panel which shows friendster profile data
        profilePanel = new ProfilePanel(this);
        // create the search panel
        searchPanel = new TextSearchPanel(ALL_COLUMNS,
                registry, searchSet, fmanager.getFocusSet(CLICK_KEY));
        // create the community explorer panel
        communityPanel = new CommunityPanel(this);
        
        // initialize the prefuse renderers and action lists
        initPrefuse();
        
        // initialize the display's control listeners
        display.addControlListener(new ExpansionControl(2));
        display.addControlListener(new FocusControl(1, CLICK_KEY));
        display.addControlListener(new HighlightControl(highlight, MOUSE_KEY));
        display.addControlListener(new HighlightHoldControl(highlight,
                HIGHLIGHT_KEY, this, HighlightHoldControl.CLICK_AND_HOLD_MODE));
        //display.addControlListener(new HighlightFreezeControl(highlight, HIGHLIGHT_KEY));
        display.addControlListener(new LinkageControl(this));
        display.addControlListener(new DragControl(redraw, true));
        display.addControlListener(new PanControl(true));
        //display.addControlListener(new MagnifyControl(this, MagnifyControl.CLICK_AND_HOLD_MODE));
        display.addMouseListener(new FocusRequester());
        
        // add a zoom control that works everywhere
        ZoomControl zc = new ZoomControl(true);
        zc.setMaxScale(10.0);
        zc.setMinScale(0.1);
        display.addMouseListener(zc);
        display.addMouseMotionListener(zc);
        
        // add click-only zoom controls
        ZoomStepControl zsc = new ZoomStepControl(this);
        display.addMouseListener(zsc);
        display.addMouseMotionListener(zsc);
        
        // set up the JFrame
        setJMenuBar(new VizsterMenuBar(this));
        initUI(); pack();
        setVisible(true);
        
        // wait until graphics are available
        while ( display.getGraphics() == null );
        
        // load the network data
        loadGraph(datafile, startUID);
    } //
    
    public void loadGraph(String datafile, String startUID) {
        // stop any running actions
        forces.cancel();
        
        // load graph
        try {
	        Graph g = datafile == null ? new DefaultGraph() : VizsterLib.loadGraph(datafile);	        
	        registry.setGraph(g);
        } catch ( Exception e ) {
            e.printStackTrace();
            VizsterLib.defaultError(this, "Couldn't load input graph." + e.getMessage());
            return;
        }
        
        // retrieve the initial profile and set as focus
        Node r = getInitialNode(startUID);
        registry.getDefaultFocusSet().set(r);
        registry.getFocusManager().getFocusSet(CLICK_KEY).set(r);
        centerDisplay();
        
        filter.runNow();
        if ( animate ) {
            forces.runNow();
        } else {
            runStaticLayout();
        }
    } //
    
    private Node getInitialNode(String uid) {
        Node r = null;      
        if ( uid == null ) {
            r = GraphLib.getMostConnectedNodes(registry.getGraph())[0]; 
        } else {
            Node[] matches = GraphLib.search(registry.getGraph(), ID_FIELD, uid);
            if ( matches.length > 0 ) {
                r = matches[0];
            } else {
                r = GraphLib.getMostConnectedNodes(registry.getGraph())[0];
            }
        }        
        return r;
    } //
    
    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE); //TODO: Make this work in applets
        
        // recenter the display upon resizing
        display.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                centerDisplay();
            } //
        });
        
        JScrollPane scroller = new JScrollPane(profilePanel);
        scroller.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension pd = profilePanel.getPreferredSize();
        scroller.setPreferredSize(new Dimension(300,pd.height));
        
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        communityPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(communityPanel);
        container.add(Box.createHorizontalGlue());
        container.add(searchPanel);
        container.setBackground(Color.WHITE);
        
        JPanel main = new JPanel(new BorderLayout());
        main.add(display, BorderLayout.CENTER);
        main.add(container, BorderLayout.SOUTH);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                false, main, scroller);
        split.setDividerSize(10);
        split.setResizeWeight(1.0);
        split.setOneTouchExpandable(true);
        
        getContentPane().add(split);
    } //
    
    public void centerDisplay() {
        Point2D centroid = DisplayLib.getCentroid(registry,
                registry.getDefaultFocusSet().iterator());
        centerDisplay(centroid);
    } //
    
    public void centerDisplay(Point2D p) {
        display.animatePanToAbs(p, 2000);
    } //
    
    public void resetDisplay() {
        Rectangle2D b = DisplayLib.getNodeBounds(registry,50);
        DisplayLib.fitViewToBounds(display, b);
    } //
    
    public void unzoom() {
        int x = display.getWidth()/2;
        int y = display.getHeight()/2;
        display.animateZoom(new Point2D.Float(x,y),0,2000);
    } //
    
    // ========================================================================
    // == PREFUSE INITIALIZATION ==============================================
    
    private void initPrefuse() {
        // initialize action map
        actionMap = new ActionMap();
        
        // initialize renderers
        renderers = new VizsterRendererFactory(this);
        registry.setRendererFactory(renderers);
        
        // ====================================================================
        // == set up actions ==================================================
        
        // filters
        FisheyeGraphFilter feyeFilter = new FisheyeGraphFilter(-1,true,false);
        AuraFilter         auraFilter = new AuraFilter();
        GarbageCollector   gcFilter   = new GarbageCollector(new String[]
          {ItemRegistry.DEFAULT_NODE_CLASS, ItemRegistry.DEFAULT_EDGE_CLASS});
        feyeFilter.setEdgesInteractive(false);
        
        // layout routines
        Layout frLayout = new FruchtermanReingoldLayout(400);
        Layout fdLayout = new VizsterLayout();
        
        // colors and highlights
        VizsterBrowsingColorFunction vcolor = new VizsterBrowsingColorFunction();
        VizsterXRayColorFunction xcolor = new VizsterXRayColorFunction();
        HighlightAction         hilite = new HighlightAction(2);
        
        // community actions
        CommunityFilter      commFilter    = new CommunityFilter(COMMUNITY_KEY);
        CommunityEdgeLabeler commLabeler   = new CommunityEdgeLabeler(COMMUNITY_KEY);
        CommunityConstructor commConstruct = new CommunityConstructor(COMMUNITY_KEY, vcolor);
        
        // initialize basic recoloring-drawing action lists
        ActionSwitch colorSwitch = new ActionSwitch(
                new Action[] {vcolor, xcolor}, 0);
        
        // ====================================================================
        // == set up actions lists ============================================
        
        // initialize basic redraw action
        redraw = new ActionList(registry);
        redraw.add(new CommunityLayout(COMMUNITY_KEY));
        redraw.add(colorSwitch);
        redraw.add(actionMap.put("fonts", new VizsterFontFunction()));
        redraw.add(new RepaintAction());
        
        // initialize lists for linkage mode
        ActionList linkageList = new ActionList(registry);
        linkageList.add(new HighlightSettingAction(-1));
        linkageList.add(new LinkageFilter(false));
        
        // initialize list for community filtering
        community = new ActionList(registry);
        community.add(commConstruct);
        community.add(commFilter);
        community.add(commLabeler);
        
        // experimental link view layout / animation
        linkLayout = new ActionList(registry);
        linkLayout.add(new VizsterCircleLayout());
        
        ActionList linkAnimate = new ActionList(registry,800);
        linkAnimate.setPacingFunction(new SlowInSlowOutPacer());
        linkAnimate.add(new LocationAnimator());
        linkAnimate.alwaysRunAfter(linkLayout);
        
        unfix = new ActionList(registry);
        unfix.add(new ReleaseFixedAction());
        
        ActionList unfixAnimate = new ActionList(registry,800);
        unfixAnimate.add(new LocationAnimator());
        unfixAnimate.alwaysRunAfter(unfix);
        // -----
        
        // initialize the filter action list
        filter = new ActionList(registry);
        filter.add(feyeFilter);
        filter.add(linkageList);
        filter.add(gcFilter);
        filter.add(commFilter);
        filter.add(commLabeler);
        filter.add(auraFilter);
        filter.add(colorSwitch);
        
        // initialize action list for filtering search auras
        aura = new ActionList(registry);
        aura.add(auraFilter);
        aura.add(hilite);
        aura.add(colorSwitch);
        
        // initialize action list for highlighting
        highlight = new ActionList(registry);
        highlight.add(hilite);
        
        // intialize lists for magnification
        magnify = new ActionList(registry);
        magnify.add(actionMap.put("size", new VizsterSizeFunction()));
        
        ActionList magnifyAnimate = new ActionList(registry, 1000);
        magnifyAnimate.add(new SizeAnimator());
        magnifyAnimate.alwaysRunAfter(magnify);
        // -----
        
        // initialize lists for outline flashing
        border = new ActionList(registry,350);
        border.add(new InvertToggleAction());
        
        // initilaize the layout, including communities
        forces = new ActionList(registry,-1,20);
        forces.add(new AbstractAction() {
            public void run(ItemRegistry registry, double frac) {                
                Iterator<?> iter = registry.getDefaultFocusSet().iterator();
                while ( iter.hasNext() ) {
                    Entity e = (Entity) iter.next();
                    if (e instanceof Node) {
                        NodeItem item = registry.getNodeItem((Node)e);
                        if ( item != null ) item.setFixed(true);
                    }
                }
            } //
        });
        forces.add(fdLayout);
        forces.add(redraw);
        
        // initialize action list for an alternate, static layout
        altForces = new ActionList(registry, 0);
        altForces.add(frLayout);
        altForces.add(colorSwitch);
        
        // animator between static configurations
        altAnimate = new ActionList(registry, 2000, 20);
        altAnimate.setPacingFunction(new SlowInSlowOutPacer());
        altAnimate.add(new PolarLocationAnimator());
        altAnimate.add(redraw);
        
        // put actions into action map
        actionMap.put("filter", feyeFilter);
        actionMap.put("linkSwitch", linkageList);
        actionMap.put("browseColors", vcolor);
        actionMap.put("compareColors", xcolor);
        actionMap.put("colorSwitch", colorSwitch);
        actionMap.put("dynamicForces", fdLayout);
        actionMap.put("staticForces", frLayout);
        actionMap.put("highlight", hilite);
        actionMap.put("commConstruct", commConstruct);
        
        // ====================================================================
        // == initialize focus listeners ======================================
        
        final FocusSet defaultSet = registry.getDefaultFocusSet();
        defaultSet.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                // unfix previous center items
                Entity[] removed = e.getRemovedFoci();
                for ( int i=0; i < removed.length; i++) {
                    NodeItem n = registry.getNodeItem((Node)removed[i]);
                    if ( n != null ) { n.setFixed(false); n.setWasFixed(false); }
                }
                
                // set current focus as referrer for newly visible nodes
                Node added = (Node)e.getFirstAdded();
                NodeItem addedItem = registry.getNodeItem(added);
                if ( addedItem != null ) {
                    setReferrer(addedItem);
                }

                setLinkageMode(false);
                filter.runNow(); // refilter
                setAnimate(true);
                
                if ( addedItem != null ) {
                    centerDisplay(addedItem.getLocation()); // center display on the new focus
                }
                    
                TimerTask task = new TimerTask() {
                    public void run() {
                        searchPanel.searchUpdate();
                    } //
                };
                VizsterLib.getTimer().schedule(task,500);
            } //
        });
        
        FocusManager fmanager = registry.getFocusManager();
        FocusSet clickedSet = fmanager.getFocusSet(CLICK_KEY);
        clickedSet.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                // update profile panel to show new focus
                Node f = (Node)e.getFirstAdded();
                if ( f != null )
                    profilePanel.updatePanel(f);
                NodeItem item = registry.getNodeItem(f);
                if ( item != null )
                    setReferrer(item);
            } //
        });
        
        FocusSet searcher = fmanager.getFocusSet(SEARCH_KEY);
        searcher.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                if (e.getAddedFoci().length>0 || e.getRemovedFoci().length>0)
                    aura.runNow();
            } //
        });
    } //
    
    // ========================================================================
    // == ACCESSOR METHODS ====================================================
    
    public int getLoginRetries() {
        return loginRetries;
    } //
    
    public ItemRegistry getItemRegistry() {
        return registry;
    } //
    
    public Display getDisplay() {
        return display;
    } //    
    
    public ForceSimulator getForceSimulator() {
        return ((VizsterLayout)actionMap.get("dynamicForces")).getForceSimulator();
    } //
    
    public void setMode(int mode) {
        if ( mode != BROWSE_MODE && mode != COMPARE_MODE )
            return;
        boolean b = (mode == BROWSE_MODE);
        xrayMode = !b;
        Color bg = b ? Color.WHITE : Color.BLACK;
        Color fg = b ? Color.BLACK : Color.WHITE;
        this.setBackground(bg);
        this.setForeground(fg);
        display.setBackground(bg);
        display.setForeground(fg);
        searchPanel.setBackground(bg);
        searchPanel.setForeground(fg);
        communityPanel.setBackground(bg);
        communityPanel.setForeground(fg);
        container.setBackground(bg);
        container.setForeground(fg);
        ActionSwitch as = (ActionSwitch)actionMap.get("colorSwitch");
        as.setSwitchValue(b?0:1);
        
        if ( !b ) {
            VizsterXRayColorFunction cf = (VizsterXRayColorFunction)
            	actionMap.get("compareColors");
            Legend l = new Legend(cf.getLabels(), cf.getColorMap());
            display.setLegend(l);
        } else {
            display.setLegend(null);
        }
    } //
    
    public boolean isXRayMode() {
        return xrayMode;
    } //
    
    public VizsterXRayColorFunction getComparisonColorFunction() {
        return (VizsterXRayColorFunction)actionMap.get("compareColors");
    } //
    
    public VizsterBrowsingColorFunction getBrowsingColorFunction() {
        return (VizsterBrowsingColorFunction)actionMap.get("browseColors");
    } //
    
    public boolean isAnimate() {
        return animate;
    } //
    
    public void redraw() {
        //redraw.runNow();
    } //
    
    public void setAnimate(boolean b) {
        if ( b ) {
            forces.runNow();
        } else {
            forces.cancel();
        }
        animate = b;
    } //
    
    public void runStaticLayout() {
        altAnimate.runAfter(altForces);
        altForces.runNow();
    } //
    
    public void runFilter() {
        filter.runNow();
    } //
    
    public void constructCommunities(int idx) {
    	((CommunityConstructor)actionMap.get("commConstruct")).setIndex(idx);
        community.runNow();
    } //
    
    public void setReferrer(NodeItem item) {
        ((ForceDirectedLayout)actionMap.get("dynamicForces"))
    		.setReferrer(item);
    } //
    
    public void setMagnify(boolean state) {
        VizsterSizeFunction sizeFunc = (VizsterSizeFunction)actionMap.get("size");
        sizeFunc.setMagnify(state);
        magnify.runNow();
        if ( state )
            border.runNow();
    } //
    
    public void setLinkageMode(boolean state) {
        Action linkage = (Action)actionMap.get("linkSwitch");
        boolean enabled = linkage.isEnabled();
        if ( state != enabled ) {
            highlight.setEnabled(!state);
            linkage.setEnabled(state);
            filter.runNow();
            if ( !state ) {
                highlight.runNow();
            }
        }
    } //
    
    public void search(String query) {
        searchPanel.setQuery(query);
    } //
    
    public void setShowImages(boolean s) {
        ((VizsterRendererFactory)registry.getRendererFactory()).setDrawImages(s);
    } //
    
    public void setLinkHighlighting(boolean s) {
        ((HighlightAction)actionMap.get("highlight")).setShowEdges(s);
    } //
    
    public void setPassHighlightTroughFocus(boolean s) {
        ((HighlightAction)actionMap.get("highlight")).setSkipFoci(s);
    } //
    
    public void setHighlightHops(int h) {
        ((HighlightAction)actionMap.get("highlight")).setHops(h);
    } //
    
} // end of class Vizster
