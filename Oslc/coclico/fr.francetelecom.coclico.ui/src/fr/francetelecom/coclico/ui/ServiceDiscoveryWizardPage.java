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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;


import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ServiceDiscoveryWizardPage extends WizardPage {
		//private static final String TITLE_SERVICE_DISCOVERY = Messages.OslcServiceDiscoveryWizardPage_Serivce_Discovery;
		private TreeViewer v;
		private ServiceDescriptor selectedServiceDescriptor;
		private final TaskRepository repository;
		private final ServiceDiscoveryProvider provider;
		private List<ServiceProvider> rootProviders;
		protected ServiceDiscoveryWizardPage(IConnector connector, TaskRepository repository) {
			super(Messages.MSG_OslcServiceDiscoveryWizardPage_Service_Discovery, Messages.MSG_OslcServiceDiscoveryWizardPage_Service_Discovery, TasksUiImages.BANNER_REPOSITORY);
			this.repository = repository;
			this.provider = new ServiceDiscoveryProvider(connector, repository, null);
			setMessage(Messages.MSG_OslcServiceDiscoveryWizardPage_Browse_Available_Services_Below);
		}
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, true));
			GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

			v = new TreeViewer(composite, SWT.VIRTUAL | SWT.BORDER);
			v.setUseHashlookup(true);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(v.getTree());

			v.setLabelProvider(new ServiceLabelProvider());
			v.setContentProvider(provider);
			v.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					TreeSelection selection = (TreeSelection) v.getSelection();
					Object o = selection.getFirstElement();
					if (o instanceof ServiceDiscoveryProvider.ServiceProviderCatalogWrapper) {
						Object provObj = ((ServiceDiscoveryProvider.ServiceProviderCatalogWrapper) o).getServiceObject();
						if (provObj instanceof ServiceProviderCatalog)	setSelectedServiceDescriptor(null);
						else if (provObj instanceof ServiceProvider)	setSelectedServiceDescriptor(null);
						else if (provObj instanceof ServiceDescriptor)	setSelectedServiceDescriptor((ServiceDescriptor) provObj);
					} else {
						// TODO: disable OK button
					}
				}
			});
			if (rootProviders != null && !rootProviders.isEmpty())
				v.setInput(rootProviders);
			else v.setInput(new ServiceProviderCatalog(repository.getRepositoryLabel(), repository.getUrl()));
			setControl(composite);
		}
		private void setSelectedServiceDescriptor(ServiceDescriptor selectedServiceDescriptor) {
			this.selectedServiceDescriptor = selectedServiceDescriptor;
			setPageComplete(selectedServiceDescriptor != null);
		}
		public ServiceDescriptor getSelectedServiceProvider() { return selectedServiceDescriptor; }
		public void setRootProviders(List<ServiceProvider> providers) { this.rootProviders = providers; }
}
