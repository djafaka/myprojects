package fr.francetelecom.coclico.core;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.apache.commons.httpclient.*;

public abstract class RequestHandler<T> {
		private final String requestName;
		public String getRequestName() { return requestName; }
		public RequestHandler(String requestName) { this.requestName = requestName; }
		public abstract T run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException, IOException;
}
