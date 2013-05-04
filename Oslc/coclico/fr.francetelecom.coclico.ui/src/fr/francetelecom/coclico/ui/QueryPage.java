package fr.francetelecom.coclico.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import fr.francetelecom.coclico.CorePlugin;
import fr.francetelecom.coclico.core.Client;
import fr.francetelecom.coclico.core.RepositoryConnector;

@SuppressWarnings("restriction")
public class QueryPage extends AbstractRepositoryQueryPage2 {
	private Text summaryText;

	private Combo projectCombo;
	
	private Combo statusCombo;
	private Combo priorityCombo;
	
	public QueryPage(TaskRepository repository, IRepositoryQuery query) {
		super("Coclico", repository, query);
		setTitle("Coclico Search");
		setDescription("Specify search parameters.");
	}
	@Override protected void createPageContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE); 	label.setText("Summary:");
		summaryText = new Text(composite, SWT.BORDER);	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(summaryText);
		
		label = new Label(composite, SWT.NONE);			label.setText("Project:");
		projectCombo = new Combo(composite, SWT.NONE);
		
		/*label = new Label(composite, SWT.NONE); 		label.setText("Status:");
		statusText = new Text(composite, SWT.BORDER);	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(statusText);
		label = new Label(composite, SWT.NONE); 		label.setText("Priority:");
		priorityText = new Text(composite, SWT.BORDER);	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(priorityText);
		*/
		label = new Label(composite, SWT.NONE);			label.setText("Status:");
		statusCombo = new Combo(composite, SWT.NONE);
		statusCombo.add("Open");
		statusCombo.add("Closed");
		label = new Label(composite, SWT.NONE);			label.setText("Priority:");
		priorityCombo = new Combo(composite, SWT.NONE);
		for (int i=1;i<=5;i++) priorityCombo.add(""+i);
		
	}
	/*@Override protected void doRefresh() {
		XmlConfiguration configuration = getClient().getConfiguration();
		projectCombo.removeAll();
		for (String project : configuration.getProjects())
			projectCombo.add(project);
	}
	@Override protected boolean hasRepositoryConfiguration() {
		return getClient().hasConfiguration();
	}
	@Override protected boolean restoreState(IRepositoryQuery query) {
		String summary = query.getAttribute(CorePlugin.QUERY_KEY_SUMMARY);
		if (summary != null) summaryText.setText(summary);
		String project = query.getAttribute(CorePlugin.QUERY_KEY_PROJECT);
		if (project != null) projectCombo.setText(project);
		return true;
	}*/
	private Client getClient() {
		return ((RepositoryConnector) getConnector()).getClient(getTaskRepository());
	}
	@Override protected boolean hasRepositoryConfiguration() {
		//return getClient().hasConfiguration();
		return true;
	}
	@Override protected void doRefresh() {}
	@Override protected boolean restoreState(IRepositoryQuery query) {
		return true;
	}
	@Override public void applyTo(IRepositoryQuery query) {
		if (getQueryTitle() != null)
			query.setSummary(getQueryTitle());
		query.setAttribute(CorePlugin.QUERY_KEY_SUMMARY, summaryText.getText());
		query.setAttribute(CorePlugin.QUERY_KEY_PROJECT, projectCombo.getText());
		
		query.setAttribute(CorePlugin.QUERY_KEY_PRIORITY, priorityCombo.getText());
		query.setAttribute(CorePlugin.QUERY_KEY_STATUS, statusCombo.getText());
		//query.setAttribute(CorePlugin.QUERY_KEY_ASSIGNED_TO, statusCombo.getText());
		
	}

}
