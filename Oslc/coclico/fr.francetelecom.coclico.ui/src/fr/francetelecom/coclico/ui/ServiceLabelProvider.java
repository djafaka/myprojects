package fr.francetelecom.coclico.ui;

import fr.francetelecom.coclico.core.*;

import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

@SuppressWarnings("restriction")
public class ServiceLabelProvider extends LabelProvider {
		@Override public String getText(Object element) {
			if (element instanceof ServiceProvider)					return ((ServiceProvider) element).getName();
			else if (element instanceof CreationDialogDescriptor)	return ((CreationDialogDescriptor) element).getTitle();
			else if (element instanceof ServiceDescriptor) 			return ((ServiceDescriptor) element).getDescription();
			else if (element instanceof ServiceDiscoveryProvider.ServiceProviderCatalogWrapper)
																	return this.getText(((ServiceDiscoveryProvider.ServiceProviderCatalogWrapper) element).getServiceObject());
			else 													return Messages.MSG_OslcServiceLabelProvider_Loading;
		}
		@Override public Image getImage(Object element) {
			if (element instanceof ServiceProviderCatalog) 			return CommonImages.getImage(TasksUiImages.REPOSITORIES_VIEW);
			else if (element instanceof ServiceProvider) 			return CommonImages.getImage(TasksUiImages.REPOSITORY);
			else if (element instanceof ServiceDiscoveryProvider.ServiceProviderCatalogWrapper)
																	return this.getImage(((ServiceDiscoveryProvider.ServiceProviderCatalogWrapper) element).getServiceObject());
			return null;
		}
}
