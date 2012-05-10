package vizbook.web.facebook;

import com.google.code.facebookapi.ProfileField;

public class NodeAtrribute {
	
	private ProfileField field;
	private String attributeName;
	private boolean clean;
	
	public String getNodeAttributeEntry(String value) {
		return null;
	}
	
	public void setClean(boolean clean) {
		this.clean = clean;
	}
	
	public boolean isClean() {
		return clean;
	}
	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getAttributeName() {
		return attributeName;
	}

	public void setField(ProfileField field) {
		this.field = field;
	}

	public ProfileField getField() {
		return field;
	}

}
