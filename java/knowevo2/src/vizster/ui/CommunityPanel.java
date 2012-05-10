package vizster.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefusex.community.CommunitySet;
import vizster.Vizster;
import edu.berkeley.guir.prefuse.event.FocusEvent;
import edu.berkeley.guir.prefuse.event.FocusListener;
import edu.berkeley.guir.prefuse.focus.FocusSet;


/**
 * CommunityPanel
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
@SuppressWarnings("serial")
public class CommunityPanel extends JPanel
	implements ChangeListener, ActionListener
{

    private static final String ENABLED  = "Disable";
    private static final String DISABLED = "Enable";
    private static final String UPDATE   = "Update";
    
    private JLabel  commLabel;
    private JSlider commSlider;
    private JButton enableButton;
    private JButton updateButton;
    private Vizster vizster;
    
    public CommunityPanel(Vizster vizster) {
        this.vizster = vizster;
        
        commLabel = new JLabel("community >>");
        
        commSlider = new JSlider();
        commSlider.setValue(0);
        commSlider.setPreferredSize(new Dimension(200,25));
        commSlider.setMaximumSize(new Dimension(200,25));
        commSlider.addChangeListener(this);
        commSlider.setEnabled(false);
        
        enableButton = new JButton(DISABLED);
        enableButton.addActionListener(this);
        
        updateButton = new JButton(UPDATE);
        updateButton.addActionListener(this);
        updateButton.setVisible(false);
        
        final CommunitySet comm = (CommunitySet)vizster.getItemRegistry()
        	.getFocusManager().getFocusSet(Vizster.COMMUNITY_KEY);
        comm.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                commSlider.setModel(comm.getRange()); 
            } //
        });
        
        FocusSet expanded = vizster.getItemRegistry().getDefaultFocusSet();
        expanded.addFocusListener(new FocusListener() {
            public void focusChanged(FocusEvent e) {
                if ( enableButton.getText() == ENABLED ) {
                    updateButton.setVisible(true);
                }
            } //
        });
        
        this.add(commLabel);
        this.add(commSlider);
        this.add(enableButton);
        this.add(updateButton);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
    } //
    
    public void setForeground(Color c) {
        super.setForeground(c);
        if ( commLabel != null ) commLabel.setForeground(c);
        //if ( commSlider != null ) commSlider.setForeground(c);
        //if ( enableButton != null ) enableButton.setForeground(c);
    } //
    
    public void setBackground(Color c) {
        super.setBackground(c);
        if ( commLabel != null ) commLabel.setBackground(c);
        if ( commSlider != null ) commSlider.setBackground(c);
        if ( enableButton != null ) enableButton.setBackground(c);
    } //
    
    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent arg0) {
        JSlider slider = (JSlider)arg0.getSource();
        vizster.constructCommunities(slider.getValue());
    } //

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == enableButton ) {
	        boolean enabled = (enableButton.getText() == ENABLED);
	        enableButton.setText(enabled ? DISABLED : ENABLED);
	        commSlider.setEnabled(!enabled);
	        if ( !enabled ) {
	        	vizster.constructCommunities(CommunityConstructor.INIT);
	        } else {
	        	vizster.constructCommunities(CommunityConstructor.CLEAR);
	            updateButton.setVisible(false);
	        }
        } else if ( evt.getSource() == updateButton ) {
            vizster.constructCommunities(CommunityConstructor.INIT);
            updateButton.setVisible(false);
        }
        vizster.runFilter();
    } //
    
    
} // end of class CommunityPanel
