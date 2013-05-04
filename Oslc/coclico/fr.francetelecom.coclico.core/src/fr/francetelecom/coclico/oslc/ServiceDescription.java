
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.net.*;
import java.util.*;

public class ServiceDescription extends Obj {
		private boolean readed=false;
		private URI creation;	// Location for creation of change Requests with a POST HTTP request
		private URI querybase;
		public ServiceDescription(URI uri) { super(uri); }
		public void readDescription()throws Exception {
			if (readed) return; readed=true;
			JSONObject o=getJSONObject();
			/*"service":{
				"domain":{ "rdf:ressource":"http://open-services.net/ns/cm#" },
				"creationFactory":{
					"dcterms:title":"Location for creation of change Requests with a POST HTTP request",
					"oslc:label":"New Tracker items Creation",
					"oslc:creation":{
						"rdf:ressource":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101"
					}
				},
				"queryCapability":{
					"dcterms:title":"GET-Based Tracker items query",
					"oslc:label":"Tracker items query",
					"oslc:queryBase":{
						"rdf:ressource":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101"
					}
				},
				"selectionDialog":{ "Dialog":{
						"dcterms:title":"Change Requests Selection Dialog",
						"oslc:label":"Tracker items selection UI",
						"oslc:dialog":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101/ui/selection",
						"oslc:hintWidth":"800px", "oslc:hintHeight":"600px"
				} },
				"creationDialog":{ "Dialog":{
						"dcterms:title":"Change Requests Creation Dialog",
						"oslc:label":"New Tracker items Celection UI",
						"oslc:dialog":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101/ui/creation",
						"oslc:hintWidth":"800px", "oslc:hintHeight":"600px"
				} }
			}*/
			o=o.getJSONObject("service");
			String s;
			s=Oslc.getString(o,"creationFactory",Oslc.OSLC_Creation,Oslc.RDF_Resource);	if (s!=null) this.creation=new URI(s);
			s=Oslc.getString(o,"queryCapability",Oslc.OSLC_QueryBase,Oslc.RDF_Resource);	if (s!=null) this.querybase=new URI(s);
		}
		public ArrayList<CMResult> ListResults() throws Exception {
			readDescription();
			/*"oslc_cm:totalCount":1, "oslc_cm:results":[{...}]*/
			ArrayList<CMResult> results=new ArrayList<CMResult>();
			JSONObject o=Oslc.getJSONObject(this.querybase);
			int n=o.getInt(Oslc.CM_TotalCount); if (n==0) return results;
			JSONArray a=o.getJSONArray(Oslc.CM_Results);
			if (a==null || a.length()<n) throw new Exception("Invalid result count");
			for (int i=0;i<n;i++) {
				JSONObject obj=a.getJSONObject(i);
				if (obj!=null && obj!=JSONObject.NULL) {
					results.add(new CMResult(obj));
					if (--n==0) break;
				}
			}
			return results;
		}
}
