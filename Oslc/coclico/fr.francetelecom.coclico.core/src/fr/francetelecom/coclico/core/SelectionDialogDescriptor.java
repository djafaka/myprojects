package fr.francetelecom.coclico.core;
import java.io.Serializable;


public class SelectionDialogDescriptor implements Serializable {
		private static final long serialVersionUID = -7728392262241197918L;
		private boolean isDefault = false;	public boolean isDefault() { return isDefault; }
											public void setDefault(boolean b) { this.isDefault = b; }
		private String hintWidth;			public String getHintWidth() { return hintWidth; }
											public void setHintWidth(String hintWidth) { this.hintWidth = hintWidth; }
		private String hintHeight;			public String getHintHeight() { return hintHeight; }
											public void setHintHeight(String hintHeight) { this.hintHeight = hintHeight; }
		private final String title;			public String getTitle() { return title; }
		private final String url;			public String getUrl() { return url; }
		private String label;				public String getLabel() { 	return label; }		public void setLabel(String label) { this.label = label; }
		public SelectionDialogDescriptor(String title, String url) { this.title = title; this.url = url; }
		@Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result + ((url == null) ? 0 : url.hashCode()); return result; }
		@Override public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj!=null && obj instanceof SelectionDialogDescriptor)
				return url == null ? ((SelectionDialogDescriptor) obj).url==null : url.equals(((SelectionDialogDescriptor) obj).url);
			return false;
		}
}
