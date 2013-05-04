
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;
import java.net.*;

public class CM extends Obj {
	private ServiceDescription details;
	public ServiceDescription getServiceDescription() { return details; }
	//private URI details;	public URI getDetailsURI() { return details; } 
	public CM(JSONObject obj) throws Exception {
		//super(obj);
		super(Oslc.getString(obj,Oslc.RDF_Type, Oslc.RDF_Resource),obj.getString(Oslc.DC_Title),obj.getString(Oslc.DC_Description));
		/*	"dcterms:title":"bugs",
			"dc:descrption":"bugs tracker",
			"rdf:type":{ "rdf:ressource":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/oslc-cm-service/7/tracker/101" },
			"oslc:domain":{ "rdf:ressource":"http://open-services.net/ns/cm#" },
			"oslc:details":"http://fgdebtrk2.rd.francetelecom.fr/tracker/index.php?group_id=7&atid=101"
		*/
		if (Oslc.getString(obj, Oslc.OSLC_Domain, Oslc.RDF_Resource)==Oslc.NS_CM)
			throw new Exception("Invalid domain resource for CM service");
		//this.details=new URI(obj.getString(OSLC_Details));
		this.details=new ServiceDescription(new URI(obj.getString(Oslc.OSLC_Details)));
	}
}
