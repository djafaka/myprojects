package fr.francetelecom.coclico.core;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
//import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
//import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.eclipse.osgi.util.NLS;

import fr.francetelecom.coclico.CorePlugin;


public class RepositoryConnector extends org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector implements IConnector {
	public enum TaskStatus { OPEN, CLOSED, DELETED; // ASSIGNED, CLOSED, NEW, REOPENED;
		public static TaskStatus fromStatus(String status) {
			if (status == null) return null;
			if (status.equals("Open")) return OPEN;
			if (status.equals("Closed")) return CLOSED;
			if (status.equals("Deleted")) return DELETED;
			return null;
		}
		public String toStatusString() { switch (this) {
			case OPEN:		return "Open"; //$NON-NLS-1$
			case CLOSED:	return "Closed"; //$NON-NLS-1$
			case DELETED:	return "Deleted"; //$NON-NLS-1$
			default:		return ""; //$NON-NLS-1$
			}
		}
		@Override public String toString() { switch (this) {
			case OPEN:		return "Open"; //$NON-NLS-1$
			case CLOSED:	return "Closed"; //$NON-NLS-1$
			case DELETED:	return "Deleted"; //$NON-NLS-1$
			default:		return ""; //$NON-NLS-1$
			}
		}
	}
	public enum TaskPriorityLevel { BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL;
		public static TaskPriorityLevel fromPriority(String priority) {
			if (priority == null) return null;
			if (priority.equals("blocker")) return BLOCKER;
			if (priority.equals("critical")) return CRITICAL;
			if (priority.equals("major")) return MAJOR;
			if (priority.equals("minor")) return MINOR;
			if (priority.equals("trivial")) return TRIVIAL;
			return null;
		}
		public PriorityLevel toPriorityLevel() { switch (this) {
			case BLOCKER:	return PriorityLevel.P1;
			case CRITICAL:	return PriorityLevel.P2;
			case MAJOR:		return PriorityLevel.P3;
			case MINOR:		return PriorityLevel.P4;
			case TRIVIAL:	return PriorityLevel.P5;
			default:		return null;
			}
		}
		@Override public String toString() { switch (this) {
			case BLOCKER:	return "blocker"; //$NON-NLS-1$
			case CRITICAL:	return "critical"; //$NON-NLS-1$
			case MAJOR:		return "major"; //$NON-NLS-1$
			case MINOR:		return "minor"; //$NON-NLS-1$
			case TRIVIAL:	return "trivial"; //$NON-NLS-1$
			default:		return null;
			}
		}
	}
		private static Map<TaskRepository, Client> clientByRepository = new HashMap<TaskRepository, Client>();
		private TaskDataHandler taskDataHandler;				
		public synchronized Client getClient(TaskRepository repository) {
			//File location = new File(repository.getProperty(XmlCorePlugin.REPOSITORY_KEY_PATH));
			Client client = clientByRepository.get(repository);
			if (client == null) {// || !client.getLocation().equals(location)) {
				client = new Client(new org.eclipse.mylyn.commons.net.WebLocation(repository.getUrl()),new ServiceDescriptor(null));
				clientByRepository.put(repository, client);
			}
			return client;
		}
		public RepositoryConnector() {
			taskDataHandler=new TaskDataHandler(this);
		}
		@Override public AbstractTaskDataHandler getTaskDataHandler() { return taskDataHandler; }
		/** Returns true, if the connector provides a wizard for creating new tasks. */
		@Override public boolean canCreateNewTask(TaskRepository repository) {
			return true;
		}
		/** Returns true, if the connector supports retrieval of tasks based on String keys. */
		@Override public boolean canCreateTaskFromKey(TaskRepository repository) {
			return true;
		}
		/** Returns the unique kind of the repository, e.g. "bugzilla". */
		@Override public String getConnectorKind() { return IConstants.ID_PLUGIN; }
		/** The connector's summary i.e. "JIRA (supports 3.3.1 and later)" */
		@Override public String getLabel() { return "Coclico"; }
		/** Can return null if URLs are not used to identify tasks. */
		@Override public String getRepositoryUrlFromTaskUrl(String taskUrl) {
			return null;
		}
		@Override public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) throws CoreException {
			TaskAttributeMapper mapper=new TaskAttributeMapper(repository);
			TaskData data = getClient(repository).getTaskData(taskId, mapper, monitor);
			return data;
		}
		@Override public String getTaskIdFromTaskUrl(String url) {
			return null;
		}
		@Override public String getTaskUrl(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override public boolean hasTaskChanged(TaskRepository repository, ITask task, TaskData taskData) {
			TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);
			if (attribute != null) {
				Date dataDate = taskData.getAttributeMapper().getDateValue(attribute);
				if (dataDate != null) {
					Date taskModificationDate = task.getModificationDate();
					if (taskModificationDate != null) return !taskModificationDate.equals(dataDate);
				}
			}
			return true;
		}
		/**
		 * Runs query on repository, results are passed to collector.
		 * If a repository does not return the full task data for a result, TaskData#isPartial() will return true.
		 * Implementors must complete executing query before returning from this method.
		 * @param repository	task repository to run query against
		 * @param query			query to run
		 * @param collector		callback for returning results
		 * @param session		provides additional information for running the query, may be null
		 * @param monitor		for reporting progress
		 * @return Status#OK_STATUS in case of success, an error status otherwise
		 * @throws OperationCanceledException if the query was canceled
		 */
		@Override public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector, ISynchronizationSession session, IProgressMonitor monitor) {
			try {
				Client client=getClient(repository);
				Collection<AbstractChangeRequest> result=client.performQuery("", monitor);
				//TaskDataHandler handler=new TaskDataHandler(this);				
				for (AbstractChangeRequest change : result) {
						TaskData taskData = taskDataHandler.readTaskData(repository, change, monitor);
						// set to true if repository does not return full task details 
						//taskData.setPartial(true);
						//if (filter.accepts(taskData))
							collector.accept(taskData);
				}
			} catch (CoreException e) {
				return new Status(IStatus.ERROR, CorePlugin.ID_PLUGIN, NLS.bind("Query failed: ''{0}''", e.getMessage()), e);
			}
			return Status.OK_STATUS;
		}
		/** Updates the local repository configuration cache (e.g. products and components).
		 * Connectors are encouraged to implement {@link #updateRepositoryConfiguration(TaskRepository, ITask, IProgressMonitor)} in addition this method.
		 * @param repository	the repository to update configuration for
		 * @see #isRepositoryConfigurationStale(TaskRepository, IProgressMonitor)
		 */
		@Override public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
			getClient(repository).updateRepositoryConfiguration(monitor);
		}
		@Override public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
			// TODO Auto-generated method stub
		}
		@Override public List<ServiceProvider> getAvailableServices(TaskRepository repository, String url, IProgressMonitor monitor) throws CoreException {
			return getClient(repository).getAvailableServices(url, monitor);
		}
		@Override public ServiceDescriptor getServiceDescriptor(TaskRepository repository, ServiceProvider selectedProvider, IProgressMonitor monitor) throws CoreException {
			return getClient(repository).getServiceDescriptor(selectedProvider, monitor);
		}
}
