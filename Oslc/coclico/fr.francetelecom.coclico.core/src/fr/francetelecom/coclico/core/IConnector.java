package fr.francetelecom.coclico.core;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
//import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;

public interface IConnector {
		public List<ServiceProvider> getAvailableServices(TaskRepository repository, String url, IProgressMonitor monitor) throws CoreException;
		public ServiceDescriptor getServiceDescriptor(TaskRepository repository, ServiceProvider selectedPovider, IProgressMonitor monitor) throws CoreException;
}
