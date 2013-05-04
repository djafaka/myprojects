package fr.francetelecom.coclico.core;
import java.io.Serializable;

public class CreationDialogDescriptor implements Serializable {
		private static final long serialVersionUID = 5159045583444273413L;
		private String title;		public String getTitle() { return title; }
									public void setTitle(String title) { this.title = title; }
		private String relativeUrl;	public String getUrl() { return relativeUrl; }
									public void setUrl(String url) { this.relativeUrl = url; }
		private boolean isDefault;	public boolean isDefault() { return isDefault; }	
									public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
		public CreationDialogDescriptor(String title, String relativeurl) { this.title = title; this.relativeUrl = relativeurl; }
}
