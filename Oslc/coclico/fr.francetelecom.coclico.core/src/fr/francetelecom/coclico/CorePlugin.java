package fr.francetelecom.coclico;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** plug-in */
public class CorePlugin extends AbstractUIPlugin {

	public static final String ID_PLUGIN = "fr.francetelecom.coclico"; //$NON-NLS-1$
	public static final String CONNECTOR_KIND = ID_PLUGIN; //"FusionForge Coclico Connector";

	public static final String REPOSITORY_URI = ID_PLUGIN + ".path";
	public static final String QUERY_KEY_SUMMARY = ID_PLUGIN + ".summary";
	public static final String QUERY_KEY_PROJECT = ID_PLUGIN + ".project";
	public static final String QUERY_KEY_STATUS = ID_PLUGIN + ".status";
	public static final String QUERY_KEY_PRIORITY = ID_PLUGIN + ".priority";
	
	private static CorePlugin plugin;
	
	public CorePlugin() {}
	public void start(BundleContext context) throws Exception { super.start(context); plugin = this; }
	public void stop(BundleContext context) throws Exception { plugin = null; super.stop(context); }
	public static CorePlugin getDefault() { return plugin; }

}
