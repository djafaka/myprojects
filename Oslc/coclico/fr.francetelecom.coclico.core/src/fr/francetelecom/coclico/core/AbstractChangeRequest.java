package fr.francetelecom.coclico.core;
public abstract class AbstractChangeRequest {
	/*
url			"rdf:about":"http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101/bug/1",
id			"dcterms:identifier":"1",
title		"dcterms:title":"Empty code",
description	"dcterms:description":"Empty code: need some code",
			"oslc_cm:status":"Open",
			"helios_bt:priority":"3",
creator		"dcterms:creator":"Remy Jean-Pierre",
			"helios_bt:assigned_to":"Nobody",
modified	"dcterms:modified":"1312187175",
			"dcterms:created":"1312187175"
	*/
		protected final String id;	public String getIdentifier() { return id; }
		protected String title;		public String getTitle() { return title; }
									public void setTitle(String title) { this.title = title; }
		private String type;		public String getType() { return type; }				
									public void setType(String type) { 	this.type = type; }
		private String description;	public String getDescription() { return description; }
									public void setDescription(String description) { this.description = description; }
		private String subject;		public String getSubject() { return subject; }			
									public void setSubject(String subject) { this.subject = subject; }
		private String creator;		public String getCreator() { return creator; }			
									public void setCreator(String creator) { this.creator = creator; }
		private String modified;	public String getModified() { return modified; }		
									public void setModified(String modified) { this.modified = modified; } // must conform to RFC3339
		private String url;			public String getUrl() { return url; }
									public void setUrl(String url) { this.url = url; }
		public AbstractChangeRequest(String identifier, String title) {
			this.id = identifier; this.title = title;
		}
		private String status;		public String getStatus() { return status; }		public void setStatus(String status) { this.status = status; }
		private String priority;	public String getPriority() { return priority; }	public void setPriority(String priority) { this.priority = priority; }
		private String assigned;	public String getAssignedTo() { return assigned; }	public void setAssignedTo(String assigned) { this.assigned = assigned; }
		private String created;		public String getCreated() { return created; }		public void setCreated(String created) { this.created = created; } // must conform to RFC3339
}
