package fr.francetelecom.coclico.ui;

import fr.francetelecom.coclico.CorePlugin;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;

public class TaskEditorPage extends AbstractTaskEditorPage {

	public TaskEditorPage(TaskEditor editor) {
		super(editor, "TaskEditorPage", "Coclico", CorePlugin.CONNECTOR_KIND);
	}

}
