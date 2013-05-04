package fr.francetelecom.coclico.ui;

import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import fr.francetelecom.coclico.CorePlugin;
import fr.francetelecom.coclico.core.Client;
import fr.francetelecom.coclico.core.IConnector;
import fr.francetelecom.coclico.core.ServiceDescriptor;
import fr.francetelecom.coclico.core.ServiceProvider;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.NewWebTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

public class ConnectorUi extends AbstractRepositoryConnectorUi {
	public ConnectorUi() { }
	/** @return the unique type of the repository, e.g. "bugzilla" */
	@Override public String getConnectorKind() { return CorePlugin.CONNECTOR_KIND; }
	@Override public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new RepositorySettingsPage("settings","Coclico repository settings",taskRepository);
	}
	@Override public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		wizard.addPage(new QueryPage(repository, query));
		return wizard;
	}
	@Override public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		//if (RepositoryConnector.hasRichEditor(repository))
			NewTaskWizard wizard=new NewTaskWizard(repository, selection);
			return wizard;
		//else
		//	return new NewWebTaskWizard(repository, repository.getRepositoryUrl() + ITracClient.NEW_TICKET_URL, selection);
	}
	/*@Override public ITaskSearchPage getSearchPage(TaskRepository repository, IStructuredSelection selection) {
		return new TracQueryPage(repository);
	}*/
	@Override public boolean hasSearchPage() {
		return false;
	}
}
