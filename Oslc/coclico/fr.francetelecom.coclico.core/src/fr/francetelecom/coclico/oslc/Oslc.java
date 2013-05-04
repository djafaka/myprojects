
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.io.*;
import java.net.*;

public abstract class Oslc { // Basic oslc object & static methods
	public static String	NS_Core							="http://open-services.net/ns/core#",
							NS_CoreServiceProviderCatalog	=NS_Core+"ServiceProviderCatalog",
							NS_CoreServiceProvider			=NS_Core+"ServiceProvider",
							NS_CoreService					=NS_Core+"Service",
							NS_CM							="http://open-services.net/ns/cm#";
	public static String 	DC_Title="dcterms:title", DC_Description="dcterms:description",
							DC_Publisher="dcterms:publisher", DC_Identifier="dcterms:identifier",
							DC_Creator="dcterms:creator",
							DC_Modified="dcterms:modified", DC_Created="dcterms:created";
	public static String 	RDF_Type="rdf:type", RDF_Resource="rdf:ressource", RDF_About="rdf:about";
	public static String	OSLC_Service="oslc:Service", OSLC_Domain="oslc:domain",
							OSLC_ServiceProviderCatalog="oslc:ServiceProviderCatalog",
							OSLC_ServiceProvider="oslc:ServiceProvider",
							OSLC_Details="oslc:details",
							OSLC_Creation="oslc:creation", OSLC_QueryBase="oslc:queryBase";
	public static String 	CM_TotalCount="oslc_cm:totalCount", CM_Results="oslc_cm:results",
							CM_Status="oslc_cm:status";
	public static String 	BT_Priority="helios_bt:priority", BT_AssignedTo="helios_bt:assigned_to";
	public static String getString(JSONObject o,String key,String skey) {
		o=o.optJSONObject(key); return o==null||o==JSONObject.NULL?null:o.optString(skey);
	}
	public static String getString(JSONObject o,String key,String key2,String skey) {
		o=o.optJSONObject(key); if (o!=null && o!=JSONObject.NULL) o=o.optJSONObject(key2);
		return o==null||o==JSONObject.NULL?null:o.optString(skey);
	}
	public static Object getJSON(URI uri) throws Exception {
		URL url=uri.toURL();
		URLConnection c = url.openConnection();
		c.addRequestProperty("Accept","application/JSON");
		InputStream is = c.getInputStream(); //url.openStream();
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		StringBuilder sb=new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) sb.append(line);
		br.close();
		return new JSONObject(sb.toString());
	}
	public static JSONObject getJSONObject(URI uri) throws Exception {
		Object o=getJSON(uri);
		if (o==null || o instanceof JSONObject) return (JSONObject)o;
		throw new Exception("Not a JSON object");
	}
}

