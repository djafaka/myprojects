package fr.francetelecom.coclico.ui;

import fr.francetelecom.coclico.core.*;
/*
import fr.francetelecom.coclico.core.Client.IConstants;
import fr.francetelecom.coclico.core.Client.IConnector;
import fr.francetelecom.coclico.core.Client.CreationDialogDescriptor;
import fr.francetelecom.coclico.core.Client.SelectionDialogDescriptor;
import fr.francetelecom.coclico.core.Client.ServiceDescriptor;
import fr.francetelecom.coclico.core.Client.ServiceProvider;
import fr.francetelecom.coclico.core.Client.ServiceProviderCatalog;
*/

import java.util.List;

import org.eclipse.jface.wizard.Wizard;



//import org.eclipse.mylyn.internal.oslc.ui.OslcServiceDiscoveryProvider.ServiceProviderCatalogWrapper;

import org.eclipse.mylyn.tasks.core.TaskRepository;

public class ServiceDiscoveryWizard extends Wizard {
		private final IConnector connector;
		private final TaskRepository repository;
		private ServiceDiscoveryWizardPage page;
		private final List<ServiceProvider> providers;
		public ServiceDiscoveryWizard(IConnector connector, TaskRepository repository,List<ServiceProvider> providers) {
			setNeedsProgressMonitor(true);
			this.connector = connector; this.repository = repository; this.providers = providers;
		}
		@Override public boolean performFinish() { return true; }
		@Override public void addPages() {
			page = new ServiceDiscoveryWizardPage(connector, repository);
			page.setRootProviders(providers);
			addPage(page);
		}
		@Override public boolean canFinish() { return (page.getSelectedServiceProvider() != null); }
		public ServiceDescriptor getSelectedServiceDescriptor() { return page.getSelectedServiceProvider(); }
}
