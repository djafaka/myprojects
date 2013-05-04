package fr.francetelecom.coclico.core;
import fr.francetelecom.coclico.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
//import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
//import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
//import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.osgi.framework.debug.Debug;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
//import org.jdom.filter.ElementFilter;
//import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public abstract class AbstractClient {
		private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
		protected final AbstractWebLocation location;
		protected final HttpClient httpClient;
		protected final ServiceDescriptor configuration;
		public AbstractClient(AbstractWebLocation location, ServiceDescriptor data) {
			this.location = location;
			this.httpClient = createHttpClient();
			this.configuration = data;
			configureHttpCredentials(location);
		}
		/** Return your unique connector identifier i.e. com.mycompany.myconnector */
		public String getUserAgent() { return "fr.francetelecom.coclico"; }
		protected HttpClient createHttpClient() {
			HttpClient httpClient = new HttpClient();
			httpClient.setHttpConnectionManager(WebUtil.getConnectionManager());
			httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			// See: https://jazz.net/jazz/web/projects/Rational%20Team%20Concert#action=com.ibm.team.workitem.viewWorkItem&id=85127\
			// Added to support fix session cookie issue when talking to tomcat
			httpClient.getParams().setParameter("http.protocol.single-cookie-header", true); //$NON-NLS-1$
			WebUtil.configureHttpClient(httpClient, getUserAgent());
			return httpClient;
		}
		protected void configureHttpCredentials(AbstractWebLocation location) {
			AuthScope authScope = new AuthScope(WebUtil.getHost(location.getUrl()), WebUtil.getPort(location.getUrl()), null, AuthScope.ANY_SCHEME);
			AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
			if (credentialsValid(credentials)) {
				Credentials creds = WebUtil.getHttpClientCredentials(credentials, WebUtil.getHost(location.getUrl()));
				httpClient.getState().setCredentials(authScope, creds);
				this.httpClient.getParams().setAuthenticationPreemptive(true);
			} else httpClient.getState().clearCredentials();
		}
		protected boolean credentialsValid(AuthenticationCredentials credentials) {
			return credentials != null && credentials.getUserName().length() > 0;
		}
		/** Exposed at connector level via IConnector.getAvailableServices() */
		public List<ServiceProvider> getAvailableServices(String url, IProgressMonitor monitor) throws CoreException {
			RequestHandler<List<ServiceProvider>> handler = new RequestHandler<List<ServiceProvider>>("Requesting Available Services") { //$NON-NLS-1$
				@Override public List<ServiceProvider> run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException {
					try {
						final List<ServiceProvider> result = new ArrayList<ServiceProvider>();
						parseServices(method.getResponseBodyAsStream(), result, monitor);
						return result;
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network error occurred retrieving available services: " + e.getMessage(), e)); //$NON-NLS-1$
					}
				}
			};
			return executeMethod(createGetMethod(url), handler, monitor);
		}
		/*protected Document getDocumentFromMethod(HttpMethodBase method) throws CoreException {
			try { return getDocumentFromStream(method.getResponseBodyAsStream()); }
			catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network error obtaining response from server: " + e.getMessage(), e)); //$NON-NLS-1$
			}
		}*/
		protected JSONObject getJSONObjectFromMethod(HttpMethodBase method) throws CoreException {
			try { return getJSONObjectFromStream(method.getResponseBodyAsStream()); }
			catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network error obtaining response from server: " + e.getMessage(), e));
			}
		}
		/*protected Document getDocumentFromStream(InputStream inStream) throws CoreException {
			SAXBuilder builder = new SAXBuilder();
			builder.setExpandEntities(false);
			try { return builder.build(inStream); }
			catch (JDOMException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error parsing response: " + e.getMessage(), e)); //$NON-NLS-1$
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network error parsing response: " + e.getMessage(), e)); //$NON-NLS-1$
			}
		}*/
		protected JSONObject getJSONObjectFromStream(InputStream inStream) throws CoreException {
			try {
				JSONTokener tokener=new JSONTokener(inStream);
				return new JSONObject(tokener);
			} catch (JSONException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error while parsing response: " + e.getMessage(), e));
			}
		}
		private /*static*/ String getJSONString(JSONObject o,String key) {
			return o==null || o==JSONObject.NULL ? null : o.optString(key); 
		}
		private /*static*/ String getJSONString(JSONObject o,String key1,String key2) {
			return o==null || o==JSONObject.NULL ? null : getJSONString(o.optJSONObject(key1),key2); 
		}
		public void parseServices(InputStream inStream, Collection<ServiceProvider> providers, IProgressMonitor monitor) throws CoreException {
			/* ServiceProviderCatalog =====================================================
				Property			Occurs	RO		Type	Representation Range	Description
			dcterms:title  				0-1	true	Literal  	n/a  		n/a  	Title of the service provider catalog  
			dcterms:description 		0-1	true	Literal  	n/a  		n/a		Description of the service provider catalog  
			dcterms:publisher			0-1	true Local Resource	Inline  oslc:Publisher  Describes the software product that provides the implementation.  
			oslc:domain					0+	true	Resource	Reference	n/a		URI of the OSLC domain specification that may be implemented by referenced services  
			oslc:serviceProvider		0+	true	Resource Either oslc:ServiceProvider  		A service offered by the service provider.  
			oslc:serviceProviderCatalog	0+	true	Resource Either oslc:ServiceProviderCatalog Additional service provider catalog.  
			oslc:oauthConfiguration		0+	true Local Resource	Inline  oslc:OAuthConfiguration Defines the three OAuth URIs required for a client to act as an OAuth consumer.
			=============================================================================*/
			/* ServiceProvider ============================================================
				Property		Occurs	RO		Type	Representation Range	Description
			dcterms:title		  	0-1	True  Literal		n/a			n/a		Title of the service provider  
			dcterms:description		0-1	True  Literal		n/a			n/a		Description of the service provider  
			dcterms:publisher		0-1	True Local Resource	Inline oslc:Publisher  Describes the software product that provides the implementation.  
			oslc:service			0+	True Local Resource	Inline oslc:Service Describes a service offered by the service provider.  
			oslc:details			0+	True 	Resource	Reference	any		A URL that may be used to retrieve a web page to determine additional details about the service provider.  
			oslc:prefixDefinition	0+	True Local Resource	Inline oslc:PrefixDefinition  Defines a namespace prefix for use in JSON representations and in forming OSLC Query Syntax strings.  
			oslc:oauthConfiguration	0-1	True Local Resource	Inline oslc:OAuthConfiguration Defines the three OAuth URIs required for a client to act as an OAuth consumer.  
			=============================================================================*/
			/* OAuthConfiguration =========================================================
				Property		Occurs	RO		Type	Representation Range	Description
			oslc:oauthRequestTokenURI  exactly-one  True  Resource  Reference  n/a  URI for obtaining OAuth request token  
			oslc:authorizationURI  exactly-one  True  Resource  Reference  n/a  URI for obtaining OAuth authorization  
			oslc:oauthAccessTokenURI  exactly-one  True  Resource  Reference  n/a  URI for obtaining OAuth access token  
			=============================================================================*/
			//Document doc = getDocumentFromStream(inStream);
			JSONObject obj=getJSONObjectFromStream(inStream);
			/*Iterator<?> itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_SERVICE_PROVIDER_CATALOG));
			while (itr.hasNext()) {
				Element element = (Element) itr.next();
				if (element != doc.getRootElement()) {
					Attribute attrAbout = element.getAttribute(IConstants.ATTRIBUTE_ABOUT, IConstants.NAMESPACE_RDF);
					String title = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC).getText();
					if (attrAbout != null && attrAbout.getValue().length() > 0)
						providers.add(new ServiceProviderCatalog(title, attrAbout.getValue()));
				}
			}*/
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_SERVICE_PROVIDER));
			while (itr.hasNext()) {
				Element element = (Element) itr.next();
				String title = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC).getText();
				Element service = element.getChild(IConstants.ELEMENT_SERVICES, IConstants.NAMESPACE_OSLC_DISCOVERY_1_0);
				if (service != null) {
					String resource = service.getAttributeValue(IConstants.ATTRIBUTE_RESOURCE, IConstants.NAMESPACE_RDF);
					providers.add(new ServiceProvider(title, resource));
				}
			}*/
			JSONArray a=obj.optJSONArray(IConstants.OSLC_ServiceProviderCatalog);
			if (a!=null) for (int i=0;i<a.length();i++) {
				JSONObject o=a.optJSONObject(i);
				if (o!=null) {
					JSONObject element;
					element=o.optJSONObject(IConstants.OSLC_ServiceProviderCatalog);
					if (element!=null) {
						String about=getJSONString(element,IConstants.RDF_Type,IConstants.RDF_About);
						String title=getJSONString(element,IConstants.DC_Title);
						if (about != null && about.length() > 0)
							providers.add(new ServiceProviderCatalog(title, about));
						continue;
					}
					element=o.optJSONObject(IConstants.OSLC_ServiceProvider);
					if (element!=null) {
						String about=getJSONString(element,IConstants.RDF_About);
						String title=getJSONString(element,IConstants.DC_Title);
						if (about != null && about.length() > 0) {
							providers.add(new ServiceProviderCatalog(title, about));
							//providers.add(new ServiceProvider(title, about));
							/*
							RequestHandler<List<ServiceProvider>> handler = new RequestHandler<List<ServiceProvider>>("Retrieving Service Descriptor") { //$NON-NLS-1$
								@Override public List<ServiceProvider> run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException, IOException {
									InputStream stream=method.getResponseBodyAsStream();
									try {
										JSONObject obj=getJSONObjectFromStream(stream);
										obj=obj.optJSONObject("oslc_disc:ServiceProviderCatalog");//IConstants.OSLC_ServiceProviderCatalog);
										List<ServiceProvider> list=null;
										if (obj!=null) {
											list=new ArrayList<ServiceProvider>();
											JSONArray a=obj.optJSONArray("oslc:serviceProvider");
											if (a!=null) for (int i=0;i<a.length();i++) {
												obj=a.optJSONObject(0);
												obj=obj.optJSONObject("oslc:service");
												String title=obj.optString(IConstants.DC_Title);
												String description=obj.optString(IConstants.DC_Description);
												String url=obj.optJSONObject(IConstants.RDF_Type).optString(IConstants.RDF_Resource);
												list.add(new ServiceProvider(title, url));
											}
										}
										return list;
									} catch (Exception e) { return null; }
								}
							};
							List<ServiceProvider> list=executeMethod(createGetMethod(about), handler, monitor);
							if (list!=null) //for (int i=0;i<list.size();i++)
								providers.addAll(list);
							//if (provider!=null) providers.add(provider);
							else providers.add(new ServiceProvider(title, about));
							*/
						}
						continue;
					}
				}
			}
else {
	obj=obj.optJSONObject("oslc_disc:ServiceProviderCatalog");//IConstants.OSLC_ServiceProviderCatalog);
	if (obj!=null) {
		a=obj.optJSONArray("oslc:serviceProvider");
		if (a!=null) for (int i=0;i<a.length();i++) {
			obj=a.optJSONObject(i);
			obj=obj.optJSONObject("oslc:service");
			String title=obj.optString(IConstants.DC_Title);
			String description=obj.optString(IConstants.DC_Description);
			String url=obj.optJSONObject(IConstants.RDF_Type).optString(IConstants.RDF_Resource);
			providers.add(new ServiceProvider(title, url));
		}
	}
}
		}
		/** Retrieve a service descriptor for the given service provider.
		 * Exposed at connector level by IOslcConnector.getServiceDescriptor()
		 * @throws CoreException */
		public ServiceDescriptor getServiceDescriptor(ServiceProvider provider, IProgressMonitor monitor) throws CoreException {
			ServiceDescriptor configuration = new ServiceDescriptor(provider.getUrl());
			downloadServiceDescriptor(configuration, monitor);
			return configuration;
		}
		/** Populate the provided configuration with new data from the remote repository. */
		protected void downloadServiceDescriptor(final ServiceDescriptor config, IProgressMonitor monitor) throws CoreException {
			RequestHandler<ServiceDescriptor> handler = new RequestHandler<ServiceDescriptor>("Retrieving Service Descriptor") { //$NON-NLS-1$
				@Override public ServiceDescriptor run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException, IOException {
					config.clear();
					parseServiceDescriptor(method.getResponseBodyAsStream(), config, monitor);
					return config;
				}
			};
			executeMethod(createGetMethod(config.getAboutUrl()), handler, monitor);
		}
		public void parseServiceDescriptor(InputStream inStream, ServiceDescriptor config, IProgressMonitor monitor) throws CoreException {
			/* Service ====================================================================
				Property		Occurs	RO		Type	Representation Range	Description
			oslc:domain			 1		True  Resource  	Reference  n/a				Namespace URI of the OSLC domain specification that is implemented by this service.  
			oslc:creationFactory 0+		True  Local Resource  n/a  oslc:CreationFactory Enables clients to create new resources  
			oslc:queryCapability 0+		True  Local Resource  n/a  oslc:QueryCapability Enables clients query across a collection of resources  
			oslc:selectionDialog 0+		True  Local Resource  n/a  oslc:Dialog  		Enables clients to select a resource via UI  
			oslc:creationDialog  0+		True  Local Resource  n/a  oslc:Dialog  		Enables clients to create a resource via UI  
			=============================================================================*/
			/* CreationFactory ============================================================
				Property		Occurs	RO		Type	Representation Range	Description
			dcterms:title  		1		True	Literal		n/a			n/a		Title string that could be used for display  
			oslc:label			0-1		True	String		n/a  		n/a		Very short label for use in menu items  
			oslc:creation		1		True	Resource	Reference	n/a		To create a new resource via the factory, post it to this URI  
			oslc:resourceShape  0+		True	Resource Reference  oslc:ResourceShape  A Creation Factory MAY provide Resource Shapes that describe shapes of resources that may be created.  
			oslc:resourceType	0+		True	Resource	Reference	n/a		The expected resource type URI of the resource that will be created using this creation factory. These would be the URIs found in the result resource's rdf:type property.  
			oslc:usage			0+		True	Resource	Resource	n/a		An identifier URI for the domain specified usage of this creation factory. If a service provides multiple creation factories, it may designate the primary or default one that should be used with a property value of http://open-services.net/ns/core#default
			=============================================================================*/
			/* QueryCapability ============================================================
				Property		Occurs	RO		Type	Representation Range	Description
			dcterms:title		1		True  Literal		n/a  		n/a 	Title string that could be used for display  
			oslc:label			0-1		True  String		n/a  		n/a 	Very short label for use in menu items  
			oslc:queryBase		1		True  Resource		Reference	n/a 	The base URI to use for queries. Queries are invoked via HTTP GET on a query URI formed by appending a key=value pair to the base URI, as described in Query Capabilities section.  
			oslc:resourceShape	0-1		True  Resource		Reference	n/a 	The Query Capability SHOULD provide a Resource Shape that describes the query base URI.  
			oslc:resourceType	0+		True  Resource		Reference	n/a 	The expected resource type URI that will be returned with this query capability. These would be the URIs found in the result resource's rdf:type property.  
			oslc:usage			0+		True  Resource		Reference	n/a		An identifier URI for the domain specified usage of this query capability. If a service provides multiple query capabilities, it may designate the primary or default one that should be used with a property value of http://open-services/ns/core#default  
			=============================================================================*/
			/* Dialog =====================================================================
				Property		Occurs	RO		Type	Representation Range	Description
			dcterms:title		1		True  	Literal		n/a			n/a		Title string that could be used for display  
			oslc:label			0-1		True	String		n/a			n/a		Very short label for use in menu items  
			oslc:dialog			1		True	Resource	Reference	n/a		The URI of the dialog  
			oslc:hintWidth		0-1		True	String		n/a			n/a		Values MUST be expressed in relative length units as defined in the W3C Cascading Style Sheets Specification (CSS 2.1) Em and ex units are interpreted relative to the default system font (at 100% size).  
			oslc:hintHeight		0-1		True	String		n/a			n/a		Values MUST be expressed in relative length units as defined in the W3C Cascading Style Sheets Specification (CSS 2.1) Em and ex units are interpreted relative to the default system font (at 100% size).  
			oslc:resourceType	0+		True	Resource	Reference	n/a		The expected resource type URI for the resources that will be returned when using this dialog. These would be the URIs found in the result resource's rdf:type property.  
			oslc:usage  zero-or-many  True  Resource  Reference  n/a  An identifier URI for the domain specified usage of this dialog. If a service provides multiple selection or creation dialogs, it may designate the primary or default one that should be used with a property value of http://open-services/ns/core#default  
			=============================================================================*/
			//Document doc = getDocumentFromStream(inStream);
			JSONObject obj=getJSONObjectFromStream(inStream);
/*obj=obj.optJSONObject("oslc_disc:ServiceProviderCatalog");//IConstants.OSLC_ServiceProviderCatalog);
if (obj!=null) {
	JSONArray a=obj.optJSONArray("oslc:serviceProvider");
	obj=a.optJSONObject(0);
	obj=obj.optJSONObject("oslc:service");
	String url=obj.getString(key);
	String title=obj.optString(IConstants.DC_Title);
	String description=obj.getString(IConstants.DC_Description);
	String url=obj.optJSONObject(IConstants.RDF_Type).optString(IConstants.RDF_Resource);
	return;
}*/
			obj=obj.optJSONObject("service");
			/*Iterator<?> itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC));
			if (itr.hasNext()) {
				Element element = (Element) itr.next();
				config.setTitle(element.getText());
			}*/
			config.setTitle(obj.optString(IConstants.DC_Title));
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_DESCRIPTION, IConstants.NAMESPACE_DC));
			if (itr.hasNext()) {
				Element element = (Element) itr.next();
				config.setDescription(element.getText());
			}*/
			config.setDescription(obj.optString(IConstants.DC_Description));
			//itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_CREATIONDIALOG, IConstants.NAMESPACE_OSLC_CM_1_0));
			/*while (itr.hasNext()) {
				boolean isDefault = false;
				Element element = (Element) itr.next();
				String label = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC).getText();
				String url = element.getChild(IConstants.ELEMENT_URL, IConstants.NAMESPACE_OSLC_CM_1_0).getText();
				Attribute attrDefault = element.getAttribute(IConstants.ATTRIBUTE_DEFAULT, IConstants.NAMESPACE_OSLC_CM_1_0);
				if (attrDefault != null && attrDefault.getValue().equals("true")) isDefault = true; //$NON-NLS-1$
				CreationDialogDescriptor recordType = new CreationDialogDescriptor(label, url);
				config.addCreationDialog(recordType);
				if (isDefault) config.setDefaultCreationDialog(recordType);
			}*/
			JSONObject o=obj.optJSONObject("creationDialog");
			if (o!=null) {
				o=o.optJSONObject("Dialog");
				if (o!=null) {
					boolean isDefault=false;
					String title=o.optString(IConstants.DC_Title);
					//String label=o.optString(IConstants.OSLC_label);
					String url=o.optString(IConstants.OSLC_dialog);
					//String w=o.optString(IConstants.OSLC_hintWidth);
					//String h=o.optString(IConstants.OSLC_hintHeight);
					CreationDialogDescriptor recordType = new CreationDialogDescriptor(title, url);
					config.addCreationDialog(recordType);
					if (isDefault) config.setDefaultCreationDialog(recordType);
				}
			}
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_SIMPLEQUERY));
			if (itr.hasNext()) {
				Element element = (Element) itr.next();
				String url = element.getChild(IConstants.ELEMENT_URL, IConstants.NAMESPACE_OSLC_CM_1_0).getText();
				if (url != null) config.setSimpleQueryUrl(url);
			}*/
			o=obj.optJSONObject("queryCapability");
			if (o!=null) {
				String url=getJSONString(o,IConstants.OSLC_queryBase,IConstants.RDF_Resource);
				if (url!=null) config.setSimpleQueryUrl(url);
			}
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_FACTORY));
			while (itr.hasNext()) {
				boolean isDefault = false;
				Element element = (Element) itr.next();
				String title = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC).getText();
				String url = element.getChild(IConstants.ELEMENT_URL, IConstants.NAMESPACE_OSLC_CM_1_0).getText();
				Attribute attrDefault = element.getAttribute(IConstants.ATTRIBUTE_DEFAULT,IConstants.NAMESPACE_OSLC_CM_1_0);
				if (attrDefault != null && attrDefault.getValue().equals("true")) isDefault = true; //$NON-NLS-1$
				ServiceFactory factory = new ServiceFactory(title, url);
				if (isDefault) config.setDefaultFactory(factory);
				config.addServiceFactory(factory);
			}*/
			o=obj.optJSONObject("creationFactory");
			if (o!=null) {
				boolean isDefault = false;
				String title=o.optString(IConstants.DC_Title);
				//String label=o.optString(IConstants.OSLC_label);
				String url = getJSONString(o,IConstants.OSLC_creation,IConstants.RDF_Resource);
				ServiceFactory factory = new ServiceFactory(title, url);
				if (isDefault) config.setDefaultFactory(factory);
				config.addServiceFactory(factory);
			}
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_HOME));
			if (itr.hasNext()) {
				Element element = (Element) itr.next();
				Element childTitle = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC);
				Element childUrl = element.getChild(IConstants.ELEMENT_URL,IConstants.NAMESPACE_OSLC_CM_1_0);
				if (childTitle != null && childTitle.getText().length() > 0 && childUrl != null && childUrl.getText().length() > 0) {
					ServiceHome home = new ServiceHome(childTitle.getText(), childUrl.getText());
					config.setHome(home);
				}
			}*/
			/*itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_SELECTIONDIALOG));
			if (itr.hasNext()) {
				Element element = (Element) itr.next();
				Element childTitle = element.getChild(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC);
				Element childUrl = element.getChild(IConstants.ELEMENT_URL, IConstants.NAMESPACE_OSLC_CM_1_0);
				if (childTitle != null && childTitle.getText().length() > 0 && childUrl != null && childUrl.getText().length() > 0) {
					SelectionDialogDescriptor selection = new SelectionDialogDescriptor(childTitle.getText(), childUrl.getText());
					String isDefault = element.getAttributeValue(IConstants.ATTRIBUTE_DEFAULT, IConstants.NAMESPACE_OSLC_CM_1_0);
					if (isDefault != null) selection.setDefault(isDefault.equals("true")); //$NON-NLS-1$
					String hintHeight = element.getAttributeValue(IConstants.ATTRIBUTE_HINTHEIGHT, IConstants.NAMESPACE_OSLC_CM_1_0);
					if (hintHeight != null) selection.setHintHeight(hintHeight);
					String hintWidth = element.getAttributeValue(IConstants.ATTRIBUTE_HINTWIDTH, IConstants.NAMESPACE_OSLC_CM_1_0);
					if (hintWidth != null) selection.setHintWidth(hintWidth);
					String label = element.getChildText(IConstants.ELEMENT_LABEL, IConstants.NAMESPACE_OSLC_CM_1_0);
					if (label != null) selection.setLabel(label);
					config.addSelectionDialog(selection);
				}
			}*/
			o=obj.optJSONObject("selectionDialog");
			if (o!=null) {
				o=o.optJSONObject("Dialog");
				if (o!=null) {
					boolean isDefault=false;
					String title=o.optString(IConstants.DC_Title);
					String url=o.optString(IConstants.OSLC_dialog);
					if (url!=null && url.length()>0) {
						SelectionDialogDescriptor selection = new SelectionDialogDescriptor(title,url);
						String label=o.optString(IConstants.OSLC_label);	if (label!=null) selection.setLabel(label);
						String w=o.optString(IConstants.OSLC_hintWidth);	if (w!=null) selection.setHintWidth(w);
						String h=o.optString(IConstants.OSLC_hintHeight);	if (h!=null) selection.setHintHeight(h);
						if (isDefault) selection.setDefault(isDefault); //$NON-NLS-1$
					}
				}
			}
		}
		protected ServiceDescriptor getConfiguration(IProgressMonitor monitor) throws CoreException {
			monitor = Policy.monitorFor(monitor);
			if (configuration.getFactories().isEmpty()) updateRepositoryConfiguration(new SubProgressMonitor(monitor, 1));
			return configuration;
		}
		@SuppressWarnings("deprecation")
		public Collection<AbstractChangeRequest> performQuery(String query, IProgressMonitor monitor) throws CoreException {
			try { query = URLEncoder.encode(query, "UTF-8"); } //$NON-NLS-1$
			catch (UnsupportedEncodingException e) { query = URLEncoder.encode(query); }
//			final String requestUrl = getConfiguration(monitor).getSimpleQueryUrl() + "?oslc_cm.query=" + query; //$NON-NLS-1$
// TODO			
final String requestUrl = "http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101"; 
			RequestHandler<Collection<AbstractChangeRequest>> handler = new RequestHandler<Collection<AbstractChangeRequest>>("Performing Query") { //$NON-NLS-1$
				@Override public Collection<AbstractChangeRequest> run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException, IOException {
					Collection<AbstractChangeRequest> result = new ArrayList<AbstractChangeRequest>();
					parseQueryResponse(method.getResponseBodyAsStream(), result, monitor);
					return result;
				}
			};
			return executeMethod(createGetMethod(requestUrl), handler, monitor);
		}
		// TODO: Handle pagination
		public void parseQueryResponse(InputStream inStream, Collection<AbstractChangeRequest> requests, IProgressMonitor monitor) throws CoreException {
			/* CM/ChangeRequest ===========================================================
				Property		Occurs	RO		Type	Representation Range	Description
				OSLC Core: Common Properties ----------------------------------------------
			oslc:shortTitle		0-1		--		Literal		n/a			n/a		Short name identifying a resource, often used as an abbreviated identifier for presentation to end-users. SHOULD include only content that is valid inside an XHTML <span> element.  
			dcterms:description	0-1		--		Literal		n/a			n/a		Descriptive text (reference: Dublin Core) about resource represented as rich text in XHTML content. SHOULD include only content that is valid and suitable inside an XHTML <div> element.  
			dcterms:title		1		--		Literal		n/a			n/a		Title (reference: Dublin Core) or often a single line summary of the resource represented as rich text in XHTML content. SHOULD include only content that is valid and suitable inside an XHTML <div> element.  
			dcterms:identifier	1		True	String		n/a			n/a		A unique identifier for a resource. Assigned by the service provider when a resource is created. Not intended for end-user display.  
			dcterms:subject		0+		False	String		n/a			n/a		Tag or keyword for a resource. Each occurrence of a dcterms:subject property denotes an additional tag for the resource.  
			dcterms:creator		0+		--		Either Resource or Local Resource  Either Reference or Inline  any  Creator or creators of resource (reference: Dublin Core). It is likely that the target resource will be a foaf:Person but that is not necessarily the case.  
			dcterms:contributor	0+		--		Either Resource or Local Resource  Either Reference or Inline  any  The person(s) who are responsible for the work needed to complete the change request (reference: Dublin Core). It is likely that the target resource will be a foaf:Person but that is not necessarily the case.  
			dcterms:created		0-1		True	DateTime	n/a			n/a		Timestamp of resource creation (reference: Dublin Core).  
			dcterms:modified	0-1		True	DateTime	n/a			n/a		Timestamp last latest resource modification (reference: Dublin Core).  
			rdf:type			0+		--		Resource	Reference	n/a		The resource type URIs. One of at least has the value of http://open-services.net/ns/cm#ChangeRequest  
			oslc:serviceProvider 0+		--		Resource	Reference  oslc:ServiceProvider  The scope of a resource is a URI for the resource's OSLC Service Provider.  
			oslc:instanceShape	0-1		--		Resource  Reference  oslc:ResourceShape  Resource Shape that provides hints as to resource property value-types and allowed values.  
			oslc:discussedBy	0-1		--		Resource  Either  oslc:Discussion  A series of notes and comments about this change request.
				OSLC CM: Start of additional properties  ------------------------------------
			dcterms:type		0+		--		String		n/a			n/a		A short string representation for the type, example 'Defect'.  
			oslc_cm:closeDate	0-1		true	DateTime	n/a			n/a		The date at which no further activity or work is intended to be conducted.  
			oslc_cm:status		0-1		--		String		n/a			n/a		Used to indicate the status of the change request based on values defined by the service provider. Most often a read-only property. Some possible values may include: 'Submitted', 'Done', 'InProgress', etc.
				 State predicate properties: This grouping of properties define a set of computed state predicates, see section on State Predicates for more information. The only restriction on valid state predicate combinations is that if oslc_cm:inprogress is true, then oslc_cm:fixed and oslc_cm:closed must also be false
			oslc_cm:closed		0-1		True	Boolean		n/a			n/a		Whether or not the Change Request is completely done, no further fixes or fix verification is needed.  
			oslc_cm:inprogress	0-1		True	Boolean		n/a			n/a		Whether or not the Change Request in a state indicating that active work is occurring. If oslc_cm:inprogress is true, then oslc_cm:fixed and oslc_cm:closed must also be false  
			oslc_cm:fixed		0-1		True	Boolean		n/a			n/a		Whether or not the Change Request has been fixed.  
			oslc_cm:approved	0-1		True	Boolean		n/a			n/a		Whether or not the Change Request has been approved.  
			oslc_cm:reviewed	0-1		True	Boolean		n/a			n/a		Whether or not the Change Request has been reviewed.  
			oslc_cm:verified	0-1		True	Boolean		n/a			n/a		Whether or not the resolution or fix of the Change Request has been verified.
				Relationship properties: This grouping of properties are used to identify relationships between resources managed by other OSLC Service Providers
			oslc_cm:relatedChangeRequest	0+	False  Resource  Reference  any  This relationship is loosely coupled and has no specific meaning. It is likely that the target resource will be an oslc_cm:ChangeRequest but that is not necessarily the case.  
			oslc_cm:affectsPlanItem			0+	False  Resource  Reference  any  Change request affects a plan item. It is likely that the target resource will be an oslc_cm:ChangeRequest but that is not necessarily the case.  
			oslc_cm:affectedByDefect		0+	False  Resource  Reference  any  Change request is affected by a reported defect. It is likely that the target resource will be an oslc_cm:ChangeRequest but that is not necessarily the case.  
			oslc_cm:tracksRequirement		0+	False  Resource  Reference  any  Tracks the associated Requirement or Requirement ChangeSet resources. It is likely that the target resource will be an oslc_rm:Requirement but that is not necessarily the case.  
			oslc_cm:implementsRequirement	0+	False  Resource  Reference  any  Implements associated Requirement. It is likely that the target resource will be an oslc_rm:Requirement but that is not necessarily the case.  
			oslc_cm:affectsRequirement		0+	False  Resource  Reference  any  Change request affecting a Requirement. It is likely that the target resource will be an oslc_rm:Requirement but that is not necessarily the case.  
			oslc_cm:testedByTestCase		0+	False  Resource  Reference  any  Test case by which this change request is tested. It is likely that the target resource will be an oslc_qm:TestCase but that is not necessarily the case.  
			oslc_cm:affectsTestResult		0+	False  Resource  Reference  any  Associated QM resource that is affected by this Change Request. It is likely that the target resource will be an oslc_qm:TestResult but that is not necessarily the case.  
			oslc_cm:blocksTestExecutionRecord 0+ False  Resource  Reference  any  Associated QM resource that is blocked by this Change Request. It is likely that the target resource will be an oslc_qm:TestExecutionRecord but that is not necessarily the case.  
			oslc_cm:relatedTestExecutionRecord 0+ False  Resource  Reference  any  Related to a QM test execution resource. It is likely that the target resource will be an oslc_qm:TestExecutionRecord but that is not necessarily the case.  
			oslc_cm:relatedTestCase			0+	False  Resource  Reference  any  Related QM test case resource. It is likely that the target resource will be an oslc_qm:TestCase but that is not necessarily the case.  
			oslc_cm:relatedTestPlan			0+	False  Resource  Reference  any  Related QM test plan resource. It is likely that the target resource will be an oslc_qm:TestPlan but that is not necessarily the case.  
			oslc_cm:relatedTestScript		0+	False  Resource  Reference  any  Related QM test script resource. It is likely that the target resource will be an oslc_qm:TestScript but that is not necessarily the case.  
			oslc_cm:tracksChangeSet			0+	False  Resource  Reference  any  Tracks SCM change set resource. It is likely that the target resource will be an oslc_scm:ChangeSet but that is not necessarily the case.  
			=============================================================================*/
			/*Document doc = getDocumentFromStream(inStream);
			Iterator<?> itr = doc.getDescendants(new ElementFilter(IConstants.ELEMENT_CHANGEREQUEST, IConstants.NAMESPACE_OSLC_CM_1_0));
			while (itr.hasNext()) {
				Element element = (Element) itr.next();
				String title = element.getChildText(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC);
				String id = element.getChildText(IConstants.ELEMENT_IDENTIFIER, IConstants.NAMESPACE_DC);
				if (title != null && id != null) {
					AbstractChangeRequest request = createChangeRequest(id, title);
					request.setType(element.getChildText(IConstants.ELEMENT_TYPE, IConstants.NAMESPACE_DC));
					request.setDescription(element.getChildText(IConstants.ELEMENT_DESCRIPTION,	IConstants.NAMESPACE_DC));
					request.setSubject(element.getChildText(IConstants.ELEMENT_SUBJECT,	IConstants.NAMESPACE_DC));
					request.setCreator(element.getChildText(IConstants.ELEMENT_CREATOR,	IConstants.NAMESPACE_DC));
					request.setModified(element.getChildText(IConstants.ELEMENT_MODIFIED,IConstants.NAMESPACE_DC));
					requests.add(request);
				}
			}*/
			JSONObject obj=getJSONObjectFromStream(inStream);
			try {
				JSONArray a=obj.getJSONArray(IConstants.CM_Results);
				if (a!=null) for (int i=0;i<a.length();i++) {
					JSONObject o=a.optJSONObject(i);
					if (o!=null) {
						String title = o.optString(IConstants.DC_Title);
						String id = o.optString(IConstants.DC_Identifier);
						if (title != null && id != null) {
							AbstractChangeRequest request = createChangeRequest(id, title);
							request.setUrl(o.optString(IConstants.RDF_About));
							//request.setType(element.getChildText(IConstants.ELEMENT_TYPE, IConstants.NAMESPACE_DC));
							request.setDescription(o.optString(IConstants.DC_Description));
							//request.setSubject(element.getChildText(IConstants.ELEMENT_SUBJECT,	IConstants.NAMESPACE_DC));
							request.setCreator(o.optString(IConstants.DC_Creator));
							request.setModified(o.optString(IConstants.DC_Modified));
							request.setCreated(o.optString(IConstants.DC_Created));
							request.setStatus(o.optString(IConstants.CM_Status));
							request.setPriority(o.optString(IConstants.BT_Priority));
							request.setAssignedTo(o.optString(IConstants.BT_AssignedTo));
							requests.add(request);
						}
					}
				}
			} catch (JSONException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error while parsing response: " + e.getMessage(), e));
			}
		}
		protected abstract AbstractChangeRequest createChangeRequest(String id, String title);
		/** Updates this clients 'repository configuration'.
		 * If old types were in use (locally cached) and still exist they are re-read from repository. */
		public void updateRepositoryConfiguration(IProgressMonitor monitor) throws CoreException {
			configuration.clear();
			downloadServiceDescriptor(configuration, monitor);
		}
		public abstract TaskData getTaskData(final String encodedTaskId, TaskAttributeMapper mapper, IProgressMonitor monitor) throws CoreException;
		public abstract RepositoryResponse putTaskData(TaskData taskData, Set<TaskAttribute> oldValues, IProgressMonitor monitor) throws CoreException;
		public RepositoryResponse postTaskData(TaskData taskData, IProgressMonitor monitor) throws CoreException {
TaskAttribute attribute;
Element root = new Element(IConstants.ELEMENT_CHANGEREQUEST, IConstants.NAMESPACE_OSLC_CM_1_0);
final Document doc = new Document(root);
attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);// IConstants.ELEMENT_TITLE);
if (attribute != null) {
	Element e = new Element(IConstants.ELEMENT_TITLE, IConstants.NAMESPACE_DC);
	e.setText(attribute.getValue()); root.addContent(e);
}
attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION);
if (attribute != null) {
	Element e = new Element(IConstants.ELEMENT_DESCRIPTION, IConstants.NAMESPACE_DC);
	e.setText(attribute.getValue()); root.addContent(e);
}

			/*TaskAttribute attribute = taskData.getRoot().getMappedAttribute(IConstants.ELEMENT_TITLE);
			if (attribute != null) {
				// TODO: Store namespace on attribute
				Element e = new Element(attribute.getId(), IConstants.NAMESPACE_DC);
				e.setText(attribute.getValue()); root.addContent(e);
			}*/
			/*attribute = taskData.getRoot().getMappedAttribute(IConstants.ELEMENT_DESCRIPTION);
			if (attribute != null) {
				Element e = new Element(attribute.getId(), IConstants.NAMESPACE_DC);
				e.setText(attribute.getValue()); root.addContent(e);
			}*/
			/*attribute = taskData.getRoot().getMappedAttribute(IConstants.ELEMENT_TYPE);
			if (attribute != null) {
				Element e = new Element(attribute.getId(), IConstants.NAMESPACE_DC);
				e.setText(attribute.getValue()); root.addContent(e);
			}*/
			/*attribute = taskData.getRoot().getMappedAttribute(IConstants.ELEMENT_SUBJECT);
			if (attribute != null) {
				Element e = new Element(attribute.getId(), IConstants.NAMESPACE_DC);
				e.setText(attribute.getValue()); root.addContent(e);
			}*/
			// For RTC Tests
			/*attribute = taskData.getRoot().getMappedAttribute("filedAgainst"); //$NON-NLS-1$
			if (attribute != null) {
				Element e = new Element(attribute.getId(), IConstants.NAMESPACE_RTC_CM_1_0);
				e.setText(attribute.getValue()); root.addContent(e);
			}*/
			//JSONStringer out;
String title=null,description=null;
attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);// IConstants.ELEMENT_TITLE);
if (attribute != null) title=attribute.getValue();
attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION);
if (attribute != null) description=attribute.getValue();
			String sout;
			XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
			sout=out.outputString(doc);		

sout="<oslc_cm:ChangeRequest xmlns:oslc_cm=\"http://open-services.net/xmlns/cm/1.0/\">"
+"<dc:title xmlns:dc=\"http://purl.org/dc/terms/\">"+title+"</dc:title>"
+"<dc:description xmlns:dc=\"http://purl.org/dc/terms/\">"+description+"</dc:description>"
+"<title>"+title+"</title>"
+"<dcterms:title xmlns:dc=\"http://purl.org/dc/terms/\">"+title+"</dcterms:title>"
+"</oslc_cm:ChangeRequest>";


			/*try {
				JSONObject root=new JSONObject();
				TaskAttribute attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY);// IConstants.ELEMENT_TITLE);
				if (attribute != null) //root.put(attribute.getId(),attribute.getValue());
					root.put(IConstants.ELEMENT_TITLE,attribute.getValue());
				attribute = taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION);
				if (attribute != null) //root.put(attribute.getId(),attribute.getValue());
					root.put(IConstants.ELEMENT_DESCRIPTION,attribute.getValue());
				//out=new JSONStringer(); out.value(root);
				sout=root.toString();
			} catch (JSONException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error creating new change request: " + e.getMessage(), e));
			}*/
//			PostMethod method = createPostMethod(getConfiguration(monitor).getDefaultFactory().getUrl());
// TODO			
//ServiceFactory factory=getConfiguration(monitor).getDefaultFactory();
String url;
//if (factory!=null) url=factory.getUrl();
//else
	url="http://fgdebtrk2.rd.francetelecom.fr/plugins/oslc/cm/project/7/tracker/101";
PostMethod method = createPostMethod(url);
			//method.setRequestHeader("Accept", "application/x-oslc-cm-change-request+xml"); //$NON-NLS-1$ //$NON-NLS-2$
			method.setRequestHeader("Content-Type", "application/x-oslc-cm-change-request+xml"); //$NON-NLS-1$ //$NON-NLS-2$
/*			
sout=// <?xml version="1.0" encoding="UTF-8"?>
		"<rdf:RDF"
			+" xmlns:dcterms=\"http://purl.org/dc/terms/\""
			+" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
			+" xmlns:oslc_cm=\"http://open-services.net/ns/cm#\">"
			+"<rdf:type rdf:resource=\"http://open-services.net/ns/cm#ChangeRequest\"/>"
			+"<oslc_cm:ChangeRequest>"
			+"<dcterms:title>"+title+"</dcterms:title>"
		    +"<dcterms:description>"+description+"</dcterms:description>"
			+"</oslc_cm:ChangeRequest>"
		+"</rdf:RDF>";
*/
sout=	"<dcterms:title>"+title+"</dcterms:title>"
		+"<dcterms:description>"+description+"</dcterms:description>";
			//method.setRequestHeader("Content-Type", "application/rdf+xml");

sout="{ \"oslc_cm:ChangeRequest\":{ \"dcterms:title\":\""+title+"\", \"dcterms:description\":\""+description+"\" } }";
sout="{"
/*
	   +"\"prefixes\" : {"
			+"\"oslc\": \"http://open-services.net/ns/core#\","
			+"\"oslc_cm\": \"http://open-services.net/ns/cm#\","
			+"\"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\","
			+"\"foaf\" : \"http://http://xmlns.com/foaf/0.1/\","
			+"\"dcterms\" : \"http://purl.org/dc/terms/\""
	   +"},"
	   //+"\"rdf:type\" : [ { \"rdf:resource\" : \"http://open-services.net/ns/bogus/blogs#Entry\" } ],"
	   +"\"rdf:type\" : [ { \"rdf:resource\" : \"http://open-services.net/ns/cm#\" } ],"
	   //"rdf:about" : "http://example.com/blogs/entry/1",
	   +"\"oslc_cm:ChangeRequest\":{"
	   		+"\"dcterms:title\":\""+title+"\","
	   		+"\"dcterms:description\":\""+description+"\""
	   	+"}"
*/
	   +"\"dcterms:title\" : \""+title+"\","
	   +"\"dcterms:description\" : \""+description+"\""
	   //"dcterms:modified" : "2002-10-10T12:00:00-05:00",
	   //"dcterms:content" : "Anything dirty or dingy or dusty. \\nAnything ragged or rotten or rusty.",
	   //"dcterms:creator" : { "foaf:name" : "Oscar T. Grouch" }
+"}";

//sout="\"dcterms:title\" : \""+title+"\","


			//method.setRequestHeader("Content-Type", "application/x-oslc-cm-change-request+xml");
			method.setRequestHeader("Content-Type", "application/json");
			//method.setRequestHeader("Content-Type", "application/JSON");
			//XMLOutputter out = new XMLOutputter(Format.getCompactFormat());
Debug.println("=================================");Debug.println(sout);Debug.println("=================================");
			//method.setRequestHeader("Accept", "application/JSON");
			method.setRequestHeader("Accept", "application/JSON");
			try {
				//method.setRequestEntity(new StringRequestEntity(out.outputString(doc), IConstants.CONTENT_TYPE_CHANGE_REQUEST, UTF_8));
				//method.setRequestEntity(new StringRequestEntity(out.toString(), "application/JSON", UTF_8));
				//method.setRequestEntity(new StringRequestEntity(sout, "application/rdf+xml" , UTF_8));
				method.setRequestEntity(new StringRequestEntity(sout, "application/json", UTF_8));
				//method.setRequestEntity(new StringRequestEntity(sout, "application/JSON", UTF_8));
			} catch (UnsupportedEncodingException e1) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Error creating new change request: " + e1.getMessage(), e1)); //$NON-NLS-1$
			}
			RequestHandler<RepositoryResponse> handler = new RequestHandler<RepositoryResponse>("Creating") { //$NON-NLS-1$
				@Override public RepositoryResponse run(HttpMethodBase method, IProgressMonitor monitor) throws CoreException {
					Header header = method.getResponseHeader("Location"); //$NON-NLS-1$
					if (header != null && header.getValue() != null) {
						String location = header.getValue();
						// TODO: delegate extraction of 'task id' to protected method and add to repository response
						return new RepositoryResponse(ResponseKind.TASK_CREATED, location);
					}
					return null;
				}
			};
			return executeMethod(method, handler, monitor);
		}
		protected GetMethod createGetMethod(String requestPath) {
			GetMethod method = new GetMethod(getRequestPath(requestPath));
			method.setFollowRedirects(true); method.setDoAuthentication(true);
			// application/xml is returned by oslc servers by default (but some may not play nice)
			//method.setRequestHeader("Accept", "application/xml"); //$NON-NLS-1$ //$NON-NLS-2$
			method.setRequestHeader("Accept", "application/JSON");
			return method;
		}
		protected PostMethod createPostMethod(String requestPath) {
			PostMethod method = new PostMethod(getRequestPath(requestPath));
			method.setFollowRedirects(false); method.setDoAuthentication(true);
	//		this.entity = getRequestEntity(method);
	//		if (pairs != null) method.setRequestBody(pairs);
	//		else if (entity != null) method.setRequestEntity(entity);
	//		else StatusHandler.log(new Status(IStatus.WARNING, IOslcCoreConstants.ID_PLUGIN, "Request body or entity missing upon post.")); 
			return method;
		}
		protected PutMethod createPutMethod(String requestPath) {
			PutMethod method = new PutMethod(getRequestPath(requestPath));
			method.setFollowRedirects(false); method.setDoAuthentication(true);
			return method;
		}
		protected <T> T executeMethod(HttpMethodBase method, RequestHandler<T> handler, IProgressMonitor monitor) throws CoreException {
			monitor = Policy.monitorFor(monitor);
			try {
				monitor.beginTask(handler.getRequestName(), IProgressMonitor.UNKNOWN);
				HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, monitor);
				int code = WebUtil.execute(httpClient, hostConfiguration, method, monitor);
Debug.println("code="+code);				
				handleReturnCode(code, method);
				return handler.run(method, monitor);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.WARNING, IConstants.ID_PLUGIN, "An unexpected network error has occurred: " + e.getMessage(), e)); //$NON-NLS-1$
			} finally {
				if (method != null) WebUtil.releaseConnection(method, monitor);
				monitor.done();
			}
		}
		public String getRequestPath(String repositoryUrl) {
			if (repositoryUrl.startsWith("./")) return WebUtil.getRequestPath(location.getUrl()) + repositoryUrl.substring(1);
			else if (repositoryUrl.startsWith("/")) return WebUtil.getRequestPath(location.getUrl()) + repositoryUrl;
			return WebUtil.getRequestPath(repositoryUrl);
		}
		protected void handleReturnCode(int code, HttpMethodBase method) throws CoreException {
			try {
				if (code == java.net.HttpURLConnection.HTTP_OK) return; // Status.OK_STATUS;
				else if (code == java.net.HttpURLConnection.HTTP_MOVED_TEMP || code == java.net.HttpURLConnection.HTTP_CREATED) // A new resource created...
					return; // Status.OK_STATUS;
				else if (code == java.net.HttpURLConnection.HTTP_UNAUTHORIZED || code == java.net.HttpURLConnection.HTTP_FORBIDDEN)
					throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Unable to log into server, ensure repository credentials are correct.")); //$NON-NLS-1$
				else if (code == java.net.HttpURLConnection.HTTP_PRECON_FAILED) // Mid-air collision
					throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Mid-air collision occurred.")); //$NON-NLS-1$
				else if (code == java.net.HttpURLConnection.HTTP_CONFLICT)
					throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "A conflict occurred.")); //$NON-NLS-1$
				else throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Unknown error occurred. Http Code: " + code + " Request: " + method.getURI() + " Response: "+ method.getResponseBodyAsString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (URIException e) {
				throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network Error: "+ e.getMessage())); //$NON-NLS-1$
			} catch (IOException e) { throw new CoreException(new Status(IStatus.ERROR, IConstants.ID_PLUGIN, "Network Error: "+ e.getMessage())); } //$NON-NLS-1$
		}
}
