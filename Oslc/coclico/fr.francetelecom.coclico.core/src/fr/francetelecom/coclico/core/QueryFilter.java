package fr.francetelecom.coclico.core;

import java.util.regex.Pattern;

import fr.francetelecom.coclico.CorePlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

public class QueryFilter {
	private Pattern summaryPattern;
	private Pattern projectPattern;
	public QueryFilter(IRepositoryQuery query) {
		String expectedSummary = query.getAttribute(CorePlugin.QUERY_KEY_SUMMARY);
		if (expectedSummary != null && expectedSummary.length() > 0)
			summaryPattern = Pattern.compile(expectedSummary);
		String expectedProject = query.getAttribute(CorePlugin.QUERY_KEY_PROJECT);
		if (expectedProject != null && expectedProject.length() > 0)
			projectPattern = Pattern.compile(expectedProject);
	}
	public boolean accepts(TaskData taskData) {
		if (!match(summaryPattern, taskData.getRoot().getAttribute(TaskAttribute.SUMMARY)))
			return false;
		if (!match(projectPattern, taskData.getRoot().getAttribute(TaskAttribute.PRODUCT)))
			return false;
		return true;
	}
	private boolean match(Pattern pattern, TaskAttribute attribute) {
		if (pattern != null) {
			return attribute != null && pattern.matcher(attribute.getValue()).find();
		}
		return true;
	}

}
