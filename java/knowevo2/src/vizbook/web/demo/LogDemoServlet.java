package vizbook.web.demo;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import vizbook.web.WebLoggingTask;

@SuppressWarnings("serial")
@WebServlet("/LogDemo")
public class LogDemoServlet extends HttpServlet {
	
	private final String TASK_NAME = "WebLoggingTask";
	
    public LogDemoServlet() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);		
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {    	
    	updateWebLog(request.getSession(), response.getWriter());    	
	}    
    
    //TODO: move this to a base class
    private void updateWebLog(HttpSession session, Writer writer) throws IOException {    	
    	
    	if(session.getAttribute(TASK_NAME) == null) {
    		WebLoggingTask task = new RandomLogger();
    		session.setAttribute(TASK_NAME, task);
    		task.start();
    	}
    	
    	WebLoggingTask task = (WebLoggingTask) session.getAttribute(TASK_NAME);
    	
    	if(!task.isDone())
    		writer.write(task.getLog());
    	else
    		writer.write("Done!");    	
    }
}
