
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.net.*;

public class Obj { 
		private URI uri;				public URI getURI() { return uri; }
		protected String title;			public String getTitle() { return title; }
		protected String description;	public String getDescription() { return description; }
		public Obj(URI uri) { this(uri,null,null); }
		public Obj(URI uri,String title, String description) {
			this.uri=uri; this.title=title; this.description=description;
		}
		public Obj(String uri,String title, String description) throws Exception {
			this.uri=new URI(uri); this.title=title; this.description=description;
		}
		/*public Obj(JSONObject obj) throws Exception {
			String s; JSONObject o;
			s=o.optString("rdf:about"); if (s!=null) this.uri=new URI(s);
			this.title=obj.optString("dcterms:title");
			this.description=obj.optString("dcterms:description");
		}*/
		public Object getJSON() throws Exception { return Oslc.getJSON(uri); }
		public JSONObject getJSONObject() throws Exception { return Oslc.getJSONObject(uri); }
}
