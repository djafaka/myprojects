
package fr.francetelecom.coclico.oslc;
import fr.francetelecom.coclico.json.*;

public class CMResult extends Obj {
		private String id;			public String getIdentifier() { return id; } 
		private String status;		public String getStatus() { return status; }
		private String creator;		public String getCreator() { return creator; }
		private String assignedto;	public String getAssignedTo() { return assignedto; }
		private String priority;	public String getPriority() { return priority; }
		private String modified;	public String getModified() { return modified; }
		private String created;		public String getCreated() { return created; }
		public CMResult(JSONObject obj) throws Exception {
			super(obj.getString(Oslc.RDF_About),obj.getString(Oslc.DC_Title),obj.getString(Oslc.DC_Description));
			id=obj.getString(Oslc.DC_Identifier);
			status=obj.getString(Oslc.CM_Status);
			priority=obj.getString(Oslc.BT_Priority);
			assignedto=obj.getString(Oslc.BT_AssignedTo);
			modified=obj.getString(Oslc.DC_Modified);
			created=obj.getString(Oslc.DC_Created);
		}
}

