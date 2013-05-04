package fr.francetelecom.coclico.core;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

public class Client extends AbstractClient {
		public Client(AbstractWebLocation location, ServiceDescriptor data) { super(location,data); }
		@Override protected AbstractChangeRequest createChangeRequest(String id, String title) {
			return new AbstractChangeRequest(id,title) {};
		}
		@Override public TaskData getTaskData(final String encodedTaskId, TaskAttributeMapper mapper, IProgressMonitor monitor) throws CoreException {
			return null;
		}
		@Override public RepositoryResponse putTaskData(TaskData taskData, Set<TaskAttribute> oldValues, IProgressMonitor monitor) throws CoreException {
			return this.postTaskData(taskData, monitor);
		}
}
