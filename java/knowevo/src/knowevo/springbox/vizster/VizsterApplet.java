/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package knowevo.springbox.vizster;


import javax.swing.*;


/**
 *
 * @author gabrovski
 */
public class VizsterApplet extends JApplet {
   
  private VizsterDrawer frame;
  
  public VizsterApplet() {
      System.out.println("dwadwa");
    frame = new VizsterDrawer();
  }
  
  public void init () {
      frame.setVisible(true);
  }

  public VizsterDrawer getVizsterFrame() {
      return frame;
  }
}
