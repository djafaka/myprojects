
package fr.francetelecom.coclico.core;
import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.osgi.util.NLS;

import fr.francetelecom.coclico.CorePlugin;

public class TaskDataHandler extends AbstractTaskDataHandler {
	private final RepositoryConnector connector;
	public TaskDataHandler(RepositoryConnector connector) {
		this.connector = connector;
	}
	@Override public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
		return new TaskAttributeMapper(repository);
	}
	public static Date fromTime(String time) {
		Date d=new Date(); long t=Long.decode(time); d.setTime(t*1000); return d;
	}
	@Override public boolean initializeTaskData(TaskRepository repository, TaskData data, ITaskMapping initializationData, IProgressMonitor monitor) throws CoreException {
		data.getRoot().createAttribute(TaskAttribute.SUMMARY).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).setLabel("Summary:");
		data.getRoot().createAttribute(TaskAttribute.DESCRIPTION).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setLabel("Description:");
		data.getRoot().createAttribute(TaskAttribute.DATE_CREATION).getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_DATETIME).setLabel("Created:");
		data.getRoot().createAttribute(TaskAttribute.DATE_MODIFICATION).getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_DATETIME).setLabel("Modified:");
		data.getRoot().createAttribute(TaskAttribute.PRIORITY).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Priority:");
		data.getRoot().createAttribute(TaskAttribute.STATUS).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Status:");
		data.getRoot().createAttribute(TaskAttribute.USER_REPORTER).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_PERSON).setLabel("Submitted by:");
		data.getRoot().createAttribute(TaskAttribute.USER_ASSIGNED).getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_PERSON).setLabel("Assigned to:");
		TaskAttribute attribute;
		if (!data.isNew()) {
			attribute = data.getRoot().createAttribute(TaskAttribute.TASK_URL);
			attribute.getMetaData().setReadOnly(true).setKind(TaskAttribute.KIND_DEFAULT).setType(TaskAttribute.TYPE_URL).setLabel("Location:");
		}
		return true;
	}
	public TaskData readTaskData(TaskRepository repository, AbstractChangeRequest change, IProgressMonitor monitor) throws CoreException {
		try {
			//TaskData data = parseDocument(repository, file, document.getDocumentElement(), monitor);
			String taskId = change.getIdentifier();
			TaskData data = new TaskData(getAttributeMapper(repository), repository.getConnectorKind(), repository.getRepositoryUrl(), taskId);
			initializeTaskData(repository, data, null, monitor);
			TaskAttribute attr;
			attr = data.getRoot().getAttribute(TaskAttribute.SUMMARY);				attr.setValue(change.getTitle());
			attr = data.getRoot().getAttribute(TaskAttribute.DESCRIPTION);			attr.setValue(change.getDescription());
			attr = data.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);	data.getAttributeMapper().setDateValue(attr, fromTime(change.getModified()));
			attr = data.getRoot().getAttribute(TaskAttribute.STATUS);				attr.setValue(change.getStatus());
			attr = data.getRoot().getAttribute(TaskAttribute.PRIORITY);				attr.setValue(change.getPriority());
			attr = data.getRoot().getAttribute(TaskAttribute.USER_REPORTER);		attr.setValue(change.getCreator());
			attr = data.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED);		attr.setValue(change.getAssignedTo());
			attr = data.getRoot().getAttribute(TaskAttribute.DATE_CREATION);		data.getAttributeMapper().setDateValue(attr, fromTime(change.getCreated()));
			//attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);		attribute.setValue(getValue(input, "project"));
			attr = data.getRoot().getAttribute(TaskAttribute.TASK_URL);	if (attr!=null) attr.setValue(change.getUrl());
			return data;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.ID_PLUGIN, NLS.bind("Error parsing task {0}", change.getUrl()), e));
		}
	}
	@Override public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
			Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
		Client client = connector.getClient(repository);
		//String taskId = getTaskId(taskData, client);
		//File file = client.getTask(taskId, monitor);
		return client.postTaskData(taskData, monitor);
	}
}
/*
	// Utility method
	private void SetType(TaskData data,boolean readonly,String attribute,String type,String label) {
		TaskAttribute attr = data.getRoot().createAttribute(attribute);
		attr.getMetaData().setReadOnly(false).setType(type).setLabel(label);
	}
	// Utility method
	private void SetKind(TaskData data,boolean readonly,String attribute,String type,String label) {
		TaskAttribute attr = data.getRoot().createAttribute(attribute);
		attr.getMetaData().setReadOnly(false).setType(type).setLabel(label);
	}*/
/*
	@Override public boolean initializeTaskData(TaskRepository repository, TaskData data, ITaskMapping initializationData, IProgressMonitor monitor) throws CoreException {
		Client client = connector.getClient(repository);
		XmlConfiguration configuration = client.getConfiguration(monitor);
		data.getRoot().createAttribute(TaskAttribute.SUMMARY).getMetaData()
			.setReadOnly(false)
			.setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).setLabel("Summary:");
		data.getRoot().createAttribute(TaskAttribute.DESCRIPTION).getMetaData()
			.setReadOnly(false)
			.setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setLabel("Description:");
		data.getRoot().createAttribute(TaskAttribute.DATE_MODIFICATION).getMetaData()
			.setReadOnly(false)
			.setType(TaskAttribute.TYPE_DATETIME).setLabel("Modified:");
		TaskAttribute attribute;
		if (!data.isNew()) {
			attribute = data.getRoot().createAttribute(TaskAttribute.TASK_URL);
			attribute.getMetaData()
				.setReadOnly(true).setKind(TaskAttribute.KIND_DEFAULT)
				.setType(TaskAttribute.TYPE_URL).setLabel("Location:");
			File file = client.getTask(data.getTaskId(), monitor);
			try {
				attribute.setValue(file.toURI().toURL().toString());
			} catch (MalformedURLException e) {}
		}
		attribute = data.getRoot().createAttribute(TaskAttribute.PRODUCT);
		attribute.getMetaData()
			.setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
			.setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Project");
		for (String project : configuration.getProjects()) {
			attribute.putOption(project, project);
		}
		return true;
	}
	@Override public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
			Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
		Client client = connector.getClient(repository);
		String taskId = getTaskId(taskData, client);
		File file = client.getTask(taskId, monitor);
		writeTaskData(repository, file, taskData, monitor);
		if (taskData.isNew())
			return new RepositoryResponse(ResponseKind.TASK_CREATED, taskId);
		else
			return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskId);
	}
	private String getTaskId(TaskData taskData, Client client) throws CoreException {
		if (taskData.isNew()) {
			try {
				return getTaskId(File.createTempFile("task", ".xml", client.getLocation()));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CorePlugin.ID_PLUGIN, NLS.bind(
						"Failed to create task at ''{0}''", client.getLocation().getAbsolutePath()), e));
			}
		} else
			return taskData.getTaskId();
	}
	public void writeTaskData(TaskRepository repository, File file, TaskData taskData, IProgressMonitor monitor)
			throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder loader = factory.newDocumentBuilder();
			Document document;
			if (taskData.isNew()) {
				document = loader.newDocument();
				document.appendChild(document.createElement("task"));
			} else {
				document = loader.parse(file);
			}
			updateDocument(repository, file, document, document.getDocumentElement(), taskData, monitor);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(file));
		} catch (Exception e) {
			//e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.ID_PLUGIN, NLS.bind(
					"Error parsing task ''{0}''", file.getAbsolutePath()), e));
		}
	}

	public TaskData readTaskData(TaskRepository repository, File file, IProgressMonitor monitor) throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder loader = factory.newDocumentBuilder();
			Document document = loader.parse(file);
			TaskData taskData = parseDocument(repository, file, document.getDocumentElement(), monitor);
			return taskData;
		} catch (Exception e) {
			//e.printStackTrace();
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.ID_PLUGIN, NLS.bind(
					"Error parsing task {0}", file.getAbsolutePath()), e));
		}
	}

	private TaskData parseDocument(TaskRepository repository, File file, Element input, IProgressMonitor monitor)
			throws CoreException {
		String taskId = getTaskId(file);
		TaskData taskData = new TaskData(getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), taskId);
		initializeTaskData(repository, taskData, null, monitor);

		TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.SUMMARY);
		attribute.setValue(getValue(input, "summary"));

		attribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
		attribute.setValue(getValue(input, "description"));

		attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);
		taskData.getAttributeMapper().setDateValue(attribute, new Date(file.lastModified()));

		attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
		attribute.setValue(getValue(input, "project"));

		return taskData;
	}
	private void updateDocument(TaskRepository repository, File file, Document document, Element input,
			TaskData taskData, IProgressMonitor monitor) throws CoreException {
		TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.SUMMARY);
		setValue(document, input, "summary", attribute.getValue());
		attribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
		setValue(document, input, "description", attribute.getValue());
		attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
		setValue(document, input, "project", attribute.getValue());
	}
	private String getValue(Element input, String elementName) {
		NodeList nodes = input.getElementsByTagName(elementName);
		if (nodes.getLength() > 0) {
			return nodes.item(0).getTextContent();
		}
		return "";
	}
	private void setValue(Document document, Element input, String elementName, String value) {
		NodeList nodes = input.getElementsByTagName(elementName);
		if (nodes.getLength() > 0) {
			nodes.item(0).setTextContent(value);
		} else {
			Element element = document.createElement(elementName);
			element.setTextContent(value);
			input.appendChild(element);
		}
	}
	private String getTaskId(File file) {
		Matcher matcher = Client.ID_PATTERN.matcher(file.getName());
		if (matcher.find())
			return matcher.group(1);
		return file.getName();
	}
*/
