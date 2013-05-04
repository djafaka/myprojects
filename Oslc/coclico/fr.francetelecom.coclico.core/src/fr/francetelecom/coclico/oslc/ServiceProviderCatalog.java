
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.net.*;
import java.util.*;

public class ServiceProviderCatalog extends Obj { // http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-services/
		private ArrayList<ServiceProvider> providers=null;
		public ServiceProviderCatalog(URI uri) {
			super(uri); // http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-services/
		}
		public List<ServiceProvider> getProviders() throws Exception {
			providers=new ArrayList<ServiceProvider>();
			/*"oslc:ServiceProviderCatalog":[
           		{
           			"oslc:ServiceProvider":{
           				"dcterms:title":"Project: Site Admin",
           				"dcterms:description":"FusionForge project Site Admin as an OSLC-CM ServiceProvider",
           				"rdf:type":{ "rdf:ressource":"http://open-services.net/ns/core#ServiceProvider" },
           				"rdf:about":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-cm-services/1",
           				"dcterms:publisher":{
           					"dcterms:title":"FusionForge OSLC V2 plugin",
           					"dcterms:identifier":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc"
           				},
           				"oslc:Service":{ "oslc:domain":"http://open-services.net/ns/core#Service" }
           			}
           		},*/
			JSONObject o=getJSONObject();
			JSONArray a=o.optJSONArray("oslc:ServiceProviderCatalog"); //"oslc:ServiceProviderCatalog":[]
			if (a!=null) for (int i=0;i<a.length();i++) {
				JSONObject obj=a.optJSONObject(i); // {}
				if (obj!=null) {
					obj=obj.optJSONObject("oslc:ServiceProvider"); //"oslc:ServiceProvider":{...}
					providers.add(new ServiceProvider(obj));
				}
			}
			return providers;
		}
}
