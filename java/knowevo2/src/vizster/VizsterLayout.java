package vizster;

import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefusex.force.DragForce;
import edu.berkeley.guir.prefusex.force.ForceSimulator;
import edu.berkeley.guir.prefusex.force.NBodyForce;
import edu.berkeley.guir.prefusex.force.SpringForce;
import edu.berkeley.guir.prefusex.layout.ForceDirectedLayout;

/**
 * VizsterLayout
 *  
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class VizsterLayout extends ForceDirectedLayout {

//    private float normal = 2E-5f;
//    private float slack1 = 2E-6f;
//    private float slack2 = 2E-7f;
    
    private float NO_TENSION = 0f;
    private float SINGLETON  = 8E-5f;
    private float ORBIT      = 1E-5f;
    private float EXTRACOMM  = 1E-6f;
    private float NORMAL     = 2E-5f;
    
    public VizsterLayout() {
        super(false,false);
        
        // initialize the force simulator
        ForceSimulator fsim = new ForceSimulator();
        fsim.addForce(new NBodyForce(-2.3f, -1f, 0.9f));
        fsim.addForce(new SpringForce(2E-5f, 150f));
        fsim.addForce(new DragForce(-0.005f));
        setForceSimulator(fsim);
        
        this.setMaxTimeStep(25L);
    } //
    
    protected float getSpringLength(EdgeItem e) {
        NodeItem n1 = (NodeItem)e.getFirstNode();
        NodeItem n2 = (NodeItem)e.getSecondNode();
        int minE = Math.min(n1.getEdgeCount(),n2.getEdgeCount());
        double doi = Math.max(n1.getDOI(), n2.getDOI());
        float len = lengthFunc(minE);
        return ( minE == 1 ? 50.f : (doi==0? 200.f: len));
    } //
    
    protected float lengthFunc(int numE) {
        numE = (numE > 10 ? 10 : numE-1);
        return 50.f + (((float)numE)/100.f)*150.f;
    } //
    
    protected float jonoCoeffFunc(EdgeItem e) {
        String type = e.getAttribute("type");
        if ( type.equals("R") ) {
            return 1E-4f;
        } else if ( type.equals("PC") ) {
            return 5E-5f;
        } else if ( type.equals("SF") ) {
            return 1E-6f;
        }
        return 5E-6f;
    } //
    
    protected float getSpringCoefficient(EdgeItem e) {
        NodeItem n1 = (NodeItem)e.getFirstNode();
        NodeItem n2 = (NodeItem)e.getSecondNode();
        int ec1 = n1.getEdgeCount();
        int ec2 = n2.getEdgeCount();
//        int maxE = Math.max(ec1,ec2);
        int minE = Math.min(ec1,ec2);
        
        // get DOI values, this should be set by the FisheyeGraphFilter
        // use them to determine if nodes are expanded foci
        double doi1 = n1.getDOI();
        double doi2 = n2.getDOI();
        double doi = Math.max(n1.getDOI(), n2.getDOI());
        
        if ( doi1 == 0 && doi2 == 0 ) {
            // no tension at all, two fixed nodes
            return NO_TENSION;
        } else if ( minE == 1 && doi == 0 ) {
            // singleton node, use strong tension
            return SINGLETON;
        } else if ( doi == 0 ) {
            // loosen tension based on edge count of non-focus
            int ec = (doi1 == 0 ? ec2 : ec1);
            float alpha = calcAlpha(ec);
            return alpha*ORBIT;
        } else {
            // two non-focus nodes, weight by the lesser edge count
            // use a lesser baseline tension for extra-community edges
            Boolean b = (Boolean)e.getVizAttribute("extraCommunity");
            boolean v = (b == null ? false : b.booleanValue() );
            float alpha = calcAlpha(minE);
            return alpha*(v?EXTRACOMM:NORMAL);
        }
    } //
    
    protected float getMassValue(NodeItem n) {
        return (float)n.getSize();
    } //
    
    private float calcAlpha(int ec) {
        float ainv = (float) Math.max(1,0.5*Math.log(ec));
        return 1.0f/ainv;
    } //
    
} // end of class VizsterLayout
