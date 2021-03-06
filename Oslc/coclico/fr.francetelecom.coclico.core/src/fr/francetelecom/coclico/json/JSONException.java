
package fr.francetelecom.coclico.json;
// The JSONException is thrown by the JSON.org classes when things are amiss.
public class JSONException extends Exception {
	private static final long serialVersionUID = 0;
	private Throwable cause; 
	public JSONException(String message) { super(message); }
	public JSONException(Throwable cause) { super(cause.getMessage()); this.cause = cause; }
	public Throwable getCause() { return this.cause; }
}
