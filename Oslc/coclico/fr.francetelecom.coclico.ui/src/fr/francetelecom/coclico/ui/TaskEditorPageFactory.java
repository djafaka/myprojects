package fr.francetelecom.coclico.ui;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import fr.francetelecom.coclico.CorePlugin;


@SuppressWarnings("restriction")
public class TaskEditorPageFactory extends AbstractTaskEditorPageFactory {
	@Override public boolean canCreatePageFor(TaskEditorInput input) {
		ITask task=input.getTask();
		return task.getConnectorKind().equals(CorePlugin.CONNECTOR_KIND)
			|| TasksUiUtil.isOutgoingNewTask(task, CorePlugin.CONNECTOR_KIND);
	}
	@Override public IFormPage createPage(TaskEditor parentEditor) {
		return new TaskEditorPage(parentEditor);
	}
	@Override public Image getPageImage() {
		return CommonImages.getImage(TasksUiImages.REPOSITORY_SMALL);
	}
	@Override public String getPageText() { return "Coclico"; }

}
