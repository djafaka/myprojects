package fr.francetelecom.coclico.core;
import java.io.Serializable;

public class ServiceFactory implements Serializable {
		private static final long serialVersionUID = -8495019838789015468L;
		private final String url;	public String getUrl() { return url; }
		private final String title;	public String getTitle() { return title; }
		private String label;		public String getLabel() { return label; }				
		public void setLabel(String l) { this.label = l; }
		private boolean isDefault;	public boolean isDefault() { return this.isDefault; }	
		public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
		public ServiceFactory(String title, String url) { this.title = title; this.url = url; }
		@Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result + ((url == null) ? 0 : url.hashCode()); return result; }
		@Override public boolean equals(Object obj) {
			if (this == obj) return true;
			else if (obj!=null && obj instanceof ServiceFactory)
				return url==null ? ((ServiceFactory)obj).url==null : url.equals(((ServiceFactory)obj).url); 
			else return false;
		}
}
