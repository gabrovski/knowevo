package vizbook.web;

import java.util.ArrayDeque;

import vizbook.web.demo.LogDemoServlet;

/**
 * Every thread spawned by a servlet that needs to output should implement the run method of this class. 
 * See {@link LogDemoServlet}
 * 
 */
public abstract class WebLoggingTask extends Thread {
	private static enum Status {
		NotStarted, Running, Done, Stopped, Error
	}
	
	private Status status = Status.NotStarted;
	
	private ArrayDeque<String> messageQueue = new ArrayDeque<String>();	
	
	public final synchronized String getLog() {
		StringBuilder ret = new StringBuilder();
		while(!messageQueue.isEmpty()) {
			String entry = messageQueue.removeLast();
			ret.append("<br/>").append(entry);
		}		
		return ret.toString();		
	}
	
	public final synchronized void logError(String s) {
		log(String.format("<b>%s</b>", s));
	}
	
	public final synchronized void log(String s) {		
		messageQueue.addLast(s);
	}
	
	public final void run() {
		status = Status.Running;
		task();
		status = Status.Done;
	}
	
	public abstract void task();
	
	public boolean isDone() {
		return status == Status.Done;
	}	
}
