package fr.francetelecom.coclico.core;
import java.io.Serializable;

public class ServiceHome implements Serializable {
		private static final long serialVersionUID = -723213938552650293L;
		private final String title;			public String getTitle() { return title; }
		private final String url;			public String getUrl() { return url; }
		public ServiceHome(String title, String url) { this.title = title; this.url = url; }
}
