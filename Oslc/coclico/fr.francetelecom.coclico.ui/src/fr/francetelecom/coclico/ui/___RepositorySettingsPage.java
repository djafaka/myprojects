package fr.francetelecom.coclico.ui;
import java.net.URI;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.widgets.Composite;
import fr.francetelecom.coclico.CorePlugin;
public class ___RepositorySettingsPage extends AbstractRepositorySettingsPage {
	public ___RepositorySettingsPage(TaskRepository taskRepository) {
		super("Coclico Repository Settings", "Settings for Coclico", taskRepository);
		setNeedsAnonymousLogin(false);
		setNeedsAdvanced(false);
		setNeedsEncoding(false);
		setNeedsProxy(true);
		setNeedsTimeZone(false);
		setNeedsHttpAuth(false);
	}
	@Override public void createControl(Composite parent) {
		super.createControl(parent);
		addRepositoryTemplatesToServerUrlCombo();// important
	}
	@Override public String getConnectorKind() { return CorePlugin.CONNECTOR_KIND; }
	@Override protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		setUserId("user");
		setPassword("pass");
		getContainer().updateButtons();
	}
	@Override protected void createAdditionalControls(Composite parent) {}
	@Override protected boolean isValidUrl(String url) {
		if (url!=null && url.length()>0)
			try { new URI(url); return true; } catch (Exception e) {}
		return false;
	}
	@Override protected Validator getValidator(TaskRepository repository) {
		return null;
	}
}
