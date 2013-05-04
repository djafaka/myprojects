package fr.francetelecom.coclico.core;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServiceDescriptor implements Serializable { // Repository configuration
		private static final long serialVersionUID = -5981264972265788764L;
		private final Set<CreationDialogDescriptor> creationDialogs;
			public Set<CreationDialogDescriptor> getCreationDialogs() { return Collections.unmodifiableSet(creationDialogs); }
			public void addCreationDialog(CreationDialogDescriptor descriptor) {
				creationDialogs.add(descriptor); if (descriptor.isDefault()) defaultDialog = descriptor;
			}
		private final Set<ServiceFactory> serviceFactories;
			public Set<ServiceFactory> getFactories() { return Collections.unmodifiableSet(serviceFactories); }
			public void addServiceFactory(ServiceFactory factory) { serviceFactories.add(factory); }
		private final Set<SelectionDialogDescriptor> selectionDialogs;
			public Set<SelectionDialogDescriptor> getSelectionDialogs() { return Collections.unmodifiableSet(selectionDialogs); }
			public void addSelectionDialog(SelectionDialogDescriptor dialog) { 	selectionDialogs.add(dialog); }
			public SelectionDialogDescriptor getDefaultSelectionDialog() {
				for (SelectionDialogDescriptor dialog : selectionDialogs) if (dialog.isDefault()) return dialog;
				return null;
			}
		private ServiceContributor contributor;
		private ServiceHome home;		public ServiceHome getHome() { return home; }
										public void setHome(ServiceHome home) { this.home = home; }
		private String aboutUrl;		public String getAboutUrl() { return aboutUrl; }
										public void setAboutUrl(String url) { this.aboutUrl = url; }
		private String simpleQueryUrl;	public String getSimpleQueryUrl() { return simpleQueryUrl; }
										public void setSimpleQueryUrl(String simpleQueryUrl) { this.simpleQueryUrl = simpleQueryUrl; }
		private ServiceFactory defaultFactory;	public ServiceFactory getDefaultFactory() {
													if (defaultFactory == null && !serviceFactories.isEmpty()) return serviceFactories.iterator().next();
													return defaultFactory;
												}
		public void setDefaultFactory(ServiceFactory factory) { this.defaultFactory = factory; }
		private CreationDialogDescriptor defaultDialog;	public CreationDialogDescriptor getDefaultCreationDialog() { return defaultDialog; }
														public void setDefaultCreationDialog(CreationDialogDescriptor defaultDialog) { this.defaultDialog = defaultDialog; }
														
		private String title;			public String getTitle() { return title != null ? title : "Service available"; }
										public void setTitle(String title) { this.title = title; }
		private String description;		public String getDescription() { return description != null ? description : "Service available"; }
										public void setDescription(String description) { this.description = description; }
		public ServiceDescriptor(String aboutUrl) {
			this.aboutUrl = aboutUrl;
			this.creationDialogs = new CopyOnWriteArraySet<CreationDialogDescriptor>();
			this.serviceFactories = new CopyOnWriteArraySet<ServiceFactory>();
			this.selectionDialogs = new CopyOnWriteArraySet<SelectionDialogDescriptor>();
		}
		public void clear() {
			this.creationDialogs.clear(); this.serviceFactories.clear(); this.selectionDialogs.clear();
			this.contributor = null; this.title = null; this.description = null; this.defaultFactory = null; this.simpleQueryUrl = null;
		}
		public void setContributor(ServiceContributor contributor) { this.contributor = contributor; }
		public ServiceContributor getContributor() { return this.contributor; }
		@Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result + ((aboutUrl == null) ? 0 : aboutUrl.hashCode()); return result; }
		@Override public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof ServiceDescriptor)) return false;
			ServiceDescriptor other = (ServiceDescriptor) obj;
			if (aboutUrl == null) {
				if (other.aboutUrl != null) return false;
			} else if (!aboutUrl.equals(other.aboutUrl)) return false;
			return true;
		}
}
