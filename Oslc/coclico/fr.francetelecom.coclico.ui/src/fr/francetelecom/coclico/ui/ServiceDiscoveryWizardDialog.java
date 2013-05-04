package fr.francetelecom.coclico.ui;

/*
import fr.francetelecom.coclico.core.Client.IConstants;
import fr.francetelecom.coclico.core.Client.IConnector;
import fr.francetelecom.coclico.core.Client.CreationDialogDescriptor;
import fr.francetelecom.coclico.core.Client.SelectionDialogDescriptor;
import fr.francetelecom.coclico.core.Client.ServiceDescriptor;
import fr.francetelecom.coclico.core.Client.ServiceProvider;
import fr.francetelecom.coclico.core.Client.ServiceProviderCatalog;
*/


import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.swt.widgets.Shell;

public class ServiceDiscoveryWizardDialog extends WizardDialog {
	public ServiceDiscoveryWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
}
