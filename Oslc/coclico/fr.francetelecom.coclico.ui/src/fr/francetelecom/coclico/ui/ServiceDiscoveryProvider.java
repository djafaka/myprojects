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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.mylyn.commons.core.StatusHandler;

import org.eclipse.mylyn.tasks.core.TaskRepository;



import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class ServiceDiscoveryProvider implements ITreeContentProvider {
	private final IConnector connector;
	private final TaskRepository repository;
	private ServiceProvider selectedPovider;	public ServiceProvider getOSLServiceProvider() { return selectedPovider; }
	private DeferredTreeContentManager manager;
		public Object[] getChildren(Object parentElement) { return manager.getChildren(parentElement); }
		public Object getParent(Object element) { return null; }
		public boolean hasChildren(Object element) { return manager.mayHaveChildren(element); }
	ServiceDiscoveryProvider(IConnector connector, TaskRepository repository, String base) {
		this.connector = connector;
		this.repository = repository;
	}
	public void dispose() {} // ignore
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			List<ServiceProvider> rootProviders = (List<ServiceProvider>) inputElement;
			Object[] result = new Object[rootProviders.size()];
			for (int x = 0; x < rootProviders.size(); x++)
				result[x] = new ServiceProviderCatalogWrapper(rootProviders.get(x));
			return result;
		} else {
			Object[] result = { inputElement };
			return result;
		}
	}
	@SuppressWarnings("deprecation")
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof AbstractTreeViewer) {
			manager = new DeferredTreeContentManager(null, (AbstractTreeViewer) viewer);
		}
	}
	protected class ServiceProviderCatalogWrapper implements IDeferredWorkbenchAdapter {
		private final Object element;
		public ServiceProviderCatalogWrapper(Object catalog) { this.element = catalog; }
		public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
			try {
				if (!monitor.isCanceled()) {
					monitor.beginTask("Loading...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
					ServiceProviderCatalogWrapper wrapper = (ServiceProviderCatalogWrapper) object;
					Object parentElement = wrapper.getServiceObject();
					if (parentElement instanceof ServiceProviderCatalog) {
						ServiceProviderCatalog remoteFolder = (ServiceProviderCatalog) parentElement;
						List<ServiceProvider> providers = connector.getAvailableServices(repository,
								remoteFolder.getUrl(), monitor);
						for (ServiceProvider oslcServiceProvider : providers) {
							collector.add(new ServiceProviderCatalogWrapper(oslcServiceProvider), monitor);
						}
					} else if (parentElement instanceof ServiceProvider) {
						selectedPovider = (ServiceProvider) parentElement;
						ServiceDescriptor serviceDescriptor = connector.getServiceDescriptor(repository,
								selectedPovider, monitor);
						collector.add(new ServiceProviderCatalogWrapper(serviceDescriptor), monitor);
//						for (OslcCreationDialogDescriptor oslcRecordType : serviceDescriptor.getCreationDialogs())
//							collector.add(new ServiceProviderCatalogWrapper(oslcRecordType), monitor);
					}
				}
			} catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error occurred during service discovery", e)); //$NON-NLS-1$
			} finally { monitor.done(); }
		}
		public ISchedulingRule getRule(Object object) { return null; }
		public boolean isContainer() {
			return (element instanceof ServiceProviderCatalog || element instanceof ServiceProvider);
		}
		public Object[] getChildren(Object o) { return null; }
		public ImageDescriptor getImageDescriptor(Object object) { return null; }
		public String getLabel(Object o) {
			if (element instanceof ServiceProvider) 		return ((ServiceProvider) element).getName();
			else if (element instanceof ServiceDescriptor)	return ((ServiceDescriptor) element).getDescription();
			return ""; //$NON-NLS-1$
		}
		public Object getParent(Object o) { return null; }
		public Object getServiceObject() { return element; }
	}
}
