package fr.francetelecom.coclico.core;
import java.io.Serializable;

public class ServiceContributor implements Serializable {
		private static final long serialVersionUID = -3975425402750114209L;
		private String title;				public String getTitle() { return title; }				public void setTitle(String title) { this.title = title; }
		private final String identifier;	public String getIdentifier() { return identifier; }
		private String icon;				public String getIcon() { return icon; }				public void setIcon(String icon) { this.icon = icon; }
		public ServiceContributor(String identifier) { this.identifier = identifier; }
}
