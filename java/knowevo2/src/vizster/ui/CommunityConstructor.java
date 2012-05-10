package vizster.ui;

import prefusex.community.CommunitySet;
import edu.berkeley.guir.prefuse.FocusManager;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.action.AbstractAction;

public class CommunityConstructor extends AbstractAction {

	public static final int INIT  = -1;
	public static final int CLEAR = -2;
	
	private VizsterBrowsingColorFunction m_color = null;
	private Object m_key;
	private int    m_idx = -1;
	
	public CommunityConstructor(Object communityKey, VizsterBrowsingColorFunction color) {
		m_key = communityKey;
		m_color = color;
	} //
	
	public void setIndex(int idx) {
		m_idx = idx;
	} //
	
	public void run(ItemRegistry registry, double frac) {
		FocusManager fman = registry.getFocusManager();
        CommunitySet comm = (CommunitySet)fman.getFocusSet(m_key);
        if ( m_idx == INIT ) {
        	comm.init(registry);
        	m_color.updateCommunityMap(comm);
        } else if ( m_idx == CLEAR ) {
        	comm.clear();
        	m_color.updateCommunityMap(comm);
        } else {
        	comm.reconstruct(m_idx);
        }
	} //

} // end of class CommunityConstructor
