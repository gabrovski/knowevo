package vizster.ui;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import vizster.Vizster;
//import vizster.util.ColorMapAction;
import vizster.action.DebugInfoAction;
import vizster.action.ExitAction;
import vizster.action.FitToViewAction;
import vizster.action.ForceConfigAction;
import vizster.action.LoadNetworkAction;
import vizster.action.ProfileSearchAction;
import vizster.action.SaveVisibleNetworkAction;
import vizster.action.ShowImagesAction;
import vizster.action.StaticLayoutAction;
import vizster.action.ToggleAnimationAction;
import vizster.action.ToggleLinkHighlighting;
import vizster.action.TogglePassHighlightThroughFocus;
import edu.berkeley.guir.prefuse.util.display.ExportDisplayAction;

/**
 * The menubar for the Vizster application
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> vizster(AT)jheer.org
 */
@SuppressWarnings("serial")
public class VizsterMenuBar extends JMenuBar {

    public static final String DBUG = "Toggle Debug Display";
    public static final String LOAD = "Open Network File...";
    public static final String CONN = "Connect to Network Database...";
    public static final String SAVE = "Save Visible Network to File...";
    public static final String EXPT = "Export Display Image...";
    public static final String EXIT = "Exit";
    
    public static final String GOTO = "Go To Profile...";
    public static final String SRCH = "Search for Profile...";
    public static final String FSIM = "Configure Force Simulator...";
    
    public static final String VIEW = "Fit To View";
    public static final String IMGS = "Show Images";
    public static final String ANIM = "Animation";
    public static final String LINK = "Highlight Links";
    public static final String PASS = "Pass Highlight Through Expanded Nodes";
    public static final String LAYT = "Re-Compute Layout";
    
    public static final String GMAP = "Grayscale";
    public static final String HMAP = "Hot";
    public static final String CMAP = "Cool";
    
    private Vizster vizster;
    
    public VizsterMenuBar(Vizster vizster) {
        this.vizster = vizster;
        initUI();
    } //
    
    private void initUI() {
        JMenu fileM = new JMenu("File");
        JMenu laytM = new JMenu("Options");
        JMenu toolM = new JMenu("Tools");
//        JMenu cmapM = new JMenu("ColorMaps");
        
        JMenuItem dbugI = new JMenuItem(DBUG);
        JMenuItem loadI = new JMenuItem(LOAD);
        JMenuItem connI = new JMenuItem(CONN);
        JMenuItem saveI = new JMenuItem(SAVE);
        JMenuItem exptI = new JMenuItem(EXPT);
        JMenuItem exitI = new JMenuItem(EXIT);
        JMenuItem gotoI = new JMenuItem(GOTO);
        JMenuItem srchI = new JMenuItem(SRCH);
        JMenuItem fsimI = new JMenuItem(FSIM);
        JMenuItem viewI = new JMenuItem(VIEW);
        JMenuItem animI = new JCheckBoxMenuItem(ANIM,true);
        JMenuItem imgsI = new JCheckBoxMenuItem(IMGS,true);
        JMenuItem linkI = new JCheckBoxMenuItem(LINK,true);
        JMenuItem passI = new JCheckBoxMenuItem(PASS,false);
        JMenuItem laytI = new JMenuItem(LAYT);
//        JMenuItem gmapI = new JCheckBoxMenuItem(GMAP);
//        JMenuItem hmapI = new JCheckBoxMenuItem(HMAP);
//        JMenuItem cmapI = new JCheckBoxMenuItem(CMAP);
        
//        ButtonGroup buttG = new ButtonGroup();
//        buttG.add(gmapI); gmapI.setSelected(true);
//        buttG.add(hmapI);
//        buttG.add(cmapI);
        
        imgsI.setSelected(true);
        
        dbugI.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
        loadI.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        saveI.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        exptI.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        gotoI.setAccelerator(KeyStroke.getKeyStroke("ctrl G"));
        fsimI.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));
        animI.setAccelerator(KeyStroke.getKeyStroke("ctrl K"));
        laytI.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
//        gmapI.setAccelerator(KeyStroke.getKeyStroke("ctrl 1"));
//        hmapI.setAccelerator(KeyStroke.getKeyStroke("ctrl 2"));
//        cmapI.setAccelerator(KeyStroke.getKeyStroke("ctrl 3"));
        
        exitI.setActionCommand(EXIT);
        loadI.setActionCommand(LOAD);
        connI.setActionCommand(CONN);
        saveI.setActionCommand(SAVE);
        exptI.setActionCommand(EXPT);
        gotoI.setActionCommand(GOTO);
        srchI.setActionCommand(SRCH);
        fsimI.setActionCommand(FSIM);
        viewI.setActionCommand(VIEW);
        imgsI.setActionCommand(IMGS);
        animI.setActionCommand(ANIM);
        linkI.setActionCommand(LINK);
        passI.setActionCommand(PASS);
        laytI.setActionCommand(LAYT);
//        gmapI.setActionCommand(GMAP);
//        hmapI.setActionCommand(HMAP);
//        cmapI.setActionCommand(CMAP);
        
        dbugI.addActionListener(new DebugInfoAction(vizster));
        
        Action loadAction = new LoadNetworkAction(vizster);
        loadI.addActionListener(loadAction);
        connI.addActionListener(loadAction);
        
        saveI.addActionListener(new SaveVisibleNetworkAction(vizster));
        exptI.addActionListener(new ExportDisplayAction(vizster.getDisplay()));
        exitI.addActionListener(new ExitAction());
        srchI.addActionListener(new ProfileSearchAction(vizster));        
        fsimI.addActionListener(new ForceConfigAction(vizster));
        
        viewI.addActionListener(new FitToViewAction(vizster));
        imgsI.addActionListener(new ShowImagesAction(vizster));
        animI.addActionListener(new ToggleAnimationAction(vizster));
        linkI.addActionListener(new ToggleLinkHighlighting(vizster));
        passI.addActionListener(new TogglePassHighlightThroughFocus(vizster));
        laytI.addActionListener(new StaticLayoutAction(vizster));
        
//        ColorMapAction cmapA = new ColorMapAction(vizster);
//        gmapI.addActionListener(cmapA);
//        hmapI.addActionListener(cmapA);
//        cmapI.addActionListener(cmapA);
        
        fileM.add(dbugI);
        fileM.add(loadI);
        fileM.add(connI);
        fileM.add(saveI);
        fileM.add(exptI);
        fileM.add(exitI);
        laytM.add(viewI);
        laytM.add(imgsI);
        laytM.add(animI);
        laytM.add(linkI);
        laytM.add(passI);
//        laytM.add(laytI);
        toolM.add(gotoI);
        toolM.add(srchI);
        toolM.add(fsimI);
//        cmapM.add(gmapI);
//        cmapM.add(hmapI);
//        cmapM.add(cmapI);
        
        add(fileM);
        add(laytM);
        add(toolM);
//        add(cmapM);
    } //
    
} // end of class VizsterMenuBar
