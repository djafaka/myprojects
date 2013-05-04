package fr.francetelecom.coclico.ui;

import fr.francetelecom.coclico.CorePlugin;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;

import org.eclipse.mylyn.commons.core.StatusHandler;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public /*abstract*/ class RepositorySettingsPage extends AbstractRepositorySettingsPage {
		private ServiceDescriptor descriptor;	public ServiceDescriptor getProvider() { return descriptor; }
		protected Text baseText;				protected String getBaseUrl() { return baseText.getText(); }
		public RepositorySettingsPage(String name, String desc, TaskRepository taskRepository) {
			super(name, desc, taskRepository);
			setNeedsAnonymousLogin(false);
			setNeedsEncoding(false);
			setNeedsTimeZone(false);
			setNeedsHttpAuth(false);
		}
		public void setServiceDescriptor(ServiceDescriptor descriptor) {
			this.descriptor = descriptor;
		}
		@Override protected void createSettingControls(Composite parent) {
			Label baseUrlLabel = new Label(parent, SWT.NONE);
			baseUrlLabel.setText("Base URL:"); //$NON-NLS-1$
			baseText = new Text(parent, SWT.BORDER);
			if (repository != null) {
				String base = repository.getProperty(IConstants.OSLC_BASEURL);
				if (base != null) baseText.setText(base);
			}
			baseText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					serverUrlCombo.setText(baseText.getText());
					if (getWizard() != null) getWizard().getContainer().updateButtons();
				}
			});
			GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).span(2, 1).grab(true, false).applyTo(baseText);
			super.createSettingControls(parent);
			if (serverUrlCombo.getText().length() == 0)
				serverUrlCombo.setText(Messages.MSG_OslcRepositorySettingsPage_Enter_Base_Url_Above);
			serverUrlCombo.setEnabled(false);
		}
		@Override public /*abstract*/ String getConnectorKind() { return CorePlugin.CONNECTOR_KIND; }
		@Override protected Validator getValidator(TaskRepository repository) {
			return new OslcValidator(createTaskRepository(), baseText.getText());
		}
		@Override protected boolean isValidUrl(String url) {
			if (url.startsWith(URL_PREFIX_HTTPS) || url.startsWith(URL_PREFIX_HTTP)) {
				try { new URL(url); return true; } catch (MalformedURLException e) { }
			}
			return false;
		}
		@Override public void applyTo(TaskRepository repository) {
			repository.setProperty(IConstants.OSLC_BASEURL, baseText.getText());
			super.applyTo(repository);
		};
		@Override protected void applyValidatorResult(Validator validator) {
			OslcValidator cqValidator = (OslcValidator) validator;
			if (!cqValidator.getProviders().isEmpty()) {
				if (repository == null) repository = createTaskRepository();
				ServiceDiscoveryWizard oslcWizard = new ServiceDiscoveryWizard(((IConnector) connector),
						cqValidator.getRepository(), cqValidator.getProviders());
				ServiceDiscoveryWizardDialog dialog = new ServiceDiscoveryWizardDialog(getShell(), oslcWizard);
				dialog.setBlockOnOpen(true);
				dialog.create();
				int result = dialog.open();
				if (result == Window.OK && oslcWizard.getSelectedServiceDescriptor() != null) {
					setUrl(oslcWizard.getSelectedServiceDescriptor().getAboutUrl());
					setServiceDescriptor(oslcWizard.getSelectedServiceDescriptor());
				} else cqValidator.setStatus(Status.CANCEL_STATUS);
			}
			super.applyValidatorResult(validator);
		}
		private class OslcValidator extends Validator {
			final TaskRepository repository;	public TaskRepository getRepository() { return this.repository; }
			private final String baseUrl;		private String getBaseUrl() { return baseUrl; }
			private List<ServiceProvider> providers = new ArrayList<ServiceProvider>();
				public List<ServiceProvider> getProviders() { return providers; }
				private void setProviders(List<ServiceProvider> providers) { this.providers = providers; }
			public OslcValidator(TaskRepository repository, String baseUrl) {
				this.repository = repository; this.baseUrl = baseUrl;
			}
			@Override public void run(IProgressMonitor monitor) throws CoreException {
				try {
					new URL(getBaseUrl());
					// TODO: if only one ServiceProviderCatalog/ServiceProvider found, use it
					List<ServiceProvider> serviceProviders = ((IConnector) connector).getAvailableServices(repository, getBaseUrl(), monitor);
					setProviders(serviceProviders);
				} catch (MalformedURLException ex) {
					throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, IStatus.OK, INVALID_REPOSITORY_URL, null));
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error during service discovery", e)); //$NON-NLS-1$
					throw e;
				}
			}
		}
		@Override protected void createAdditionalControls(Composite parent) {} // ignore
}
