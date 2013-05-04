package fr.francetelecom.coclico.ui;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class UIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "fr.francetelecom.coclico.ui"; //$NON-NLS-1$
	private static UIPlugin plugin;
	public UIPlugin() {	}
	public void start(BundleContext context) throws Exception { super.start(context); plugin = this; }
	public void stop(BundleContext context) throws Exception { plugin = null; super.stop(context); }
	public static UIPlugin getDefault() { return plugin; }
}
