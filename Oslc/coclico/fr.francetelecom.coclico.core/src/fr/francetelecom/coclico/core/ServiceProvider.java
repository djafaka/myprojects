package fr.francetelecom.coclico.core;

public class ServiceProvider { // Catalog
		private final String name;		public String getName() { return name; }
		private final String url;		public String getUrl() { return url; }
		public ServiceProvider(String name, String url) { this.name = name; this.url = url; }
}
