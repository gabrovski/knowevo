package vizster;

import java.util.Comparator;

import vizster.ui.DecoratorItem;

import edu.berkeley.guir.prefuse.AggregateItem;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.NodeItem;

/**
 * Comparator that sorts items based on type and focus status.
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class VizsterItemComparator implements Comparator<Object> {

    private static int MOUSE_SCORE        = (1<<10);
    private static int HIGHLIGHT1_SCORE   = (1<<9);
    private static int HIGHLIGHT2_SCORE   = (1<<8);
    private static int HIGHLIGHT3_SCORE   = (1<<7);
    private static int FOCUS_SCORE        = (1<<6);
    private static int NODE_SCORE         = (1<<5);
    // private static int AURA_SCORE         = (1<<4);
    private static int AGGREGATE_SCORE    = (1<<3);
    private static int FADED_SCORE        = (1<<2);
    private static int EDGE_SCORE         = (1<<1);
    
    protected int score(VisualItem item) {
        int score = 0;
        boolean isAggregate = item instanceof AggregateItem;
        boolean isDecorator = item instanceof DecoratorItem;
        
        if ( item.isHighlighted() && !isAggregate && !isDecorator ) {
            int val = 0;
            if ( item instanceof EdgeItem ) {
                score += HIGHLIGHT3_SCORE;
            } else if ( (val=VizsterLib.getHighlightValue(item)) == 0 ) {
               score += MOUSE_SCORE;
            } else if ( val == 1 ) {
                score += HIGHLIGHT1_SCORE;
            } else if ( val >= 2 ) {
                score += HIGHLIGHT2_SCORE;
            }
        }
//        if ( isDecorator ) {
//            VisualItem ditem = ((DecoratorItem)item).getDecorated();
//            if ( ditem.isHighlighted() ) {
//	            int val = VizsterLib.getHighlightValue(ditem);
//	            System.out.println(val);
//	            if ( val == 0 ) {
//	                score += MOUSE_SCORE-1;
//	             } else if ( val == 1 ) {
//	                 score += HIGHLIGHT1_SCORE-1;
//	             } else if ( val >= 2 ) {
//	                 score += HIGHLIGHT2_SCORE-1;
//	             }
//            } else if ( item.isFocus() ) {
//                score += FOCUS_SCORE;
//            }
//        }
        if ( item.isFocus() ) {
            score += FOCUS_SCORE;
        }
        if ( isAggregate ) {
            score += AGGREGATE_SCORE;
        } else if ( item instanceof NodeItem ) {
            if ( !item.isHighlighted() && VizsterLib.getHighlightValue(item) < 0 )
                score += FADED_SCORE;
            else
                score += NODE_SCORE;
        } else if ( item instanceof EdgeItem ) {
            score += EDGE_SCORE;
        }
        return score;
    } //
    
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if ( !(o1 instanceof VisualItem && o2 instanceof VisualItem) ) {
			throw new IllegalArgumentException();
		}
		
		VisualItem item1 = (VisualItem)o1;
		VisualItem item2 = (VisualItem)o2;
        
		int score1 = score(item1);
		int score2 = score(item2);

		if ( item1 instanceof AggregateItem && item2 instanceof AggregateItem ) {
            int s1 = ((AggregateItem)item1).getAggregateSize();
            int s2 = ((AggregateItem)item2).getAggregateSize();
            if ( s1 < s2 )
                score1 += 1;
            else if ( s2 < s1 )
                score2 += 1;
        }
		
		return (score1<score2 ? -1 : (score1==score2 ? 0 : 1));
	} //

} // end of class VizsterItemComparator
