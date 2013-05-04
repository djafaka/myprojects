
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.util.*;

public class ServiceProvider extends Obj {
		private ArrayList<CM> services=null;
		//public ServiceProvider(URI uri) { super(uri); }
		//public ServiceProvider(URI uri,String title,String description) { super(uri,title,description); }
		public ServiceProvider(JSONObject obj) throws Exception {
			super(obj.getString(Oslc.RDF_About),obj.getString(Oslc.DC_Title),obj.getString(Oslc.DC_Description));
			if (Oslc.getString(obj, Oslc.RDF_Type, Oslc.RDF_Resource)!=Oslc.NS_CoreServiceProvider)
				throw new Exception("Invalid resource type");
			/*
			"dcterms:title":"Project: Site Admin",
			"dcterms:description":"FusionForge project Site Admin as an OSLC-CM ServiceProvider",
			"rdf:type":{ "rdf:ressource":"http://open-services.net/ns/core#ServiceProvider" },
			"rdf:about":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-cm-services/1",
			"dcterms:publisher":{
				"dcterms:title":"FusionForge OSLC V2 plugin",
				"dcterms:identifier":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc"
			},
			"oslc:Service":{ "oslc:domain":"http://open-services.net/ns/core#Service" }
			*/
		}
		public List<CM> getServices() throws Exception {
			services=new ArrayList<CM>();
			/*"oslc_disc:ServiceProviderCatalog":{
				"oslc:serviceProvider":[ {
						"oslc:service":{
							"dcterms:title":"bugs",
							"dc:descrption":"bugs tracker",
							"rdf:type":{ "rdf:ressource":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-cm-service/7/tracker/101" },
							"oslc:domain":{ "rdf:ressource":"http://open-services.net/ns/cm#" },
							"oslc:details":"http://fgdebtrk2.rd.francetelecom.fr/tracker/index.php?group_id=7&atid=101"
						}
				} ]
			}*/
			JSONObject o=getJSONObject();
			o=o.optJSONObject("oslc_disc:ServiceProviderCatalog");
			if (o!=null) {
				JSONArray a=o.optJSONArray("oslc:serviceProvider");
				if (a!=null) for (int i=0;i<a.length();i++) {
					JSONObject obj=a.optJSONObject(i);
					obj=obj.optJSONObject("oslc:service");
					//if (obj!=null) services.add(new Service(obj));
					if (obj!=null && Oslc.getString(obj, Oslc.OSLC_Domain, Oslc.RDF_Resource)==Oslc.NS_CM)
						services.add(new CM(obj));
				}
			}
			return services;
		}
}
