package vizster;

import javax.swing.JApplet;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class VizsterApplet extends JApplet {	
	//TODO: http://www.reddit.com/r/java/comments/d9tun/how_to_get_a_javaswing_program_to_work_in_a/
	
	JFrame vizster = Vizster.main("");

	@Override
	public void init() {		
		super.init();
	}

	@Override
	public void start() {		
		super.start();		
		vizster.setVisible(true);
	}

	@Override
	public void stop() {		
		super.stop();
	}
}
