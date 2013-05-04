package fr.francetelecom.coclico.core;

import org.jdom.Namespace;

import fr.francetelecom.coclico.CorePlugin;
public interface IConstants {
		public static final String ID_PLUGIN = CorePlugin.ID_PLUGIN; //$NON-NLS-1$
		// Task Repository property keys
		public static final String OSLC_BASEURL = "oslc.baseurl"; //$NON-NLS-1$
		// Namespaces
		public static final Namespace NAMESPACE_OSLC_CM_1_0 = Namespace.getNamespace("oslc_cm", "http://open-services.net/xmlns/cm/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
		//public static final Namespace NAMESPACE_OSLC_DISCOVERY_1_0 = Namespace.getNamespace("oslc_disc", "http://open-services.net/xmlns/discovery/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
public static final Namespace NAMESPACE_DC = Namespace.getNamespace("dc", "http://purl.org/dc/terms/"); //$NON-NLS-1$ //$NON-NLS-2$
		//public static final Namespace NAMESPACE_RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); //$NON-NLS-1$ //$NON-NLS-2$
		//public static final Namespace NAMESPACE_ATOM = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
		//public static final Namespace NAMESPACE_RTC_CM_1_0 = Namespace.getNamespace("rtc_cm", "http://jazz.net/xmlns/prod/jazz/rtc/cm/1.0/"); //$NON-NLS-1$ //$NON-NLS-2$
		public static final String		NS_Core							="http://open-services.net/ns/core#",
										NS_CoreServiceProviderCatalog	=NS_Core+"ServiceProviderCatalog",
										NS_CoreServiceProvider			=NS_Core+"ServiceProvider",
										NS_CoreService					=NS_Core+"Service",
										NS_CM							="http://open-services.net/ns/cm#";
		// Content types
		//public static final String CONTENT_TYPE_CHANGE_REQUEST = "application/x-oslc-cm-change-request+xml"; //$NON-NLS-1$
		// XML element ids
		//public static final String ELEMENT_SERVICE_PROVIDER_CATALOG = "ServiceProviderCatalog"; //$NON-NLS-1$
		//public static final String ELEMENT_SERVICE_PROVIDER = "ServiceProvider"; //$NON-NLS-1$
		public static final String ELEMENT_CHANGEREQUEST = "ChangeRequest"; //$NON-NLS-1$
		public static final String ELEMENT_SERVICES = "services"; //$NON-NLS-1$
		public static final String ELEMENT_CREATIONDIALOG = "creationDialog"; //$NON-NLS-1$
		public static final String ELEMENT_SELECTIONDIALOG = "selectionDialog"; //$NON-NLS-1$
		public static final String ELEMENT_FACTORY = "factory"; //$NON-NLS-1$
		public static final String ELEMENT_HOME = "home"; //$NON-NLS-1$
		public static final String ELEMENT_TITLE = "title"; //$NON-NLS-1$
		public static final String ELEMENT_TYPE = "type"; //$NON-NLS-1$
		public static final String ELEMENT_IDENTIFIER = "identifier"; //$NON-NLS-1$
		public static final String ELEMENT_DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String ELEMENT_CREATOR = "creator"; //$NON-NLS-1$
		public static final String ELEMENT_MODIFIED = "modified"; //$NON-NLS-1$
		public static final String ELEMENT_SUBJECT = "subject"; //$NON-NLS-1$
		public static final String ELEMENT_URL = "url"; //$NON-NLS-1$
		public static final String ELEMENT_SIMPLEQUERY = "simpleQuery"; //$NON-NLS-1$
		public static final String ELEMENT_LABEL = "label"; //$NON-NLS-1$
		// XML attribute ids
		public static final String ATTRIBUTE_RESOURCE = "resource"; //$NON-NLS-1$
		public static final String ATTRIBUTE_DEFAULT = "default"; //$NON-NLS-1$
		public static final String ATTRIBUTE_HINTWIDTH = "hintWidth"; //$NON-NLS-1$
		public static final String ATTRIBUTE_HINTHEIGHT = "hintHeight"; //$NON-NLS-1$
		public static final String ATTRIBUTE_ABOUT = "about"; //$NON-NLS-1$
		// Http header keys
		public static final String HEADER_ETAG = "ETag"; //$NON-NLS-1$
		public static final String HEADER_IF_MATCH = "If-Match"; //$NON-NLS-1$
		public static final String
			DC_Title					="dcterms:title",
			DC_Description				="dcterms:description",
			DC_Publisher				="dcterms:publisher",
			DC_Identifier				="dcterms:identifier",
			DC_Creator					="dcterms:creator",
			DC_Modified					="dcterms:modified",
			DC_Created					="dcterms:created";
		public static final String
			RDF_About					="rdf:about",
			RDF_Resource				="rdf:ressource",
			RDF_Type					="rdf:type";
		public static final String
			OSLC_Service				="oslc:Service",
			OSLC_ServiceProviderCatalog	="oslc:ServiceProviderCatalog",
			OSLC_ServiceProvider		="oslc:ServiceProvider",
			OSLC_creation				="oslc:creation",
			OSLC_details				="oslc:details",
			OSLC_dialog					="oslc:dialog",
			OSLC_domain					="oslc:domain",
			OSLC_hintWidth				="oslc:hintWidth",
			OSLC_hintHeight				="oslc:hintHeight",
			OSLC_label					="oslc:label",
			OSLC_queryBase				="oslc:queryBase";
		public static final String
			CM_Results					="oslc_cm:results",
			CM_Status					="oslc_cm:status",
			CM_TotalCount				="oslc_cm:totalCount";
		public static final String
			BT_Priority					="helios_bt:priority",
			BT_AssignedTo				="helios_bt:assigned_to";
}
