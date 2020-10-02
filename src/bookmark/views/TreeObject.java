package bookmark.views;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

import bookmark.constant.Constant;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content
 * (like Task List, for example).
 */
public class TreeObject implements IAdaptable, Serializable {
	private static final long serialVersionUID = -4275221961856278045L;

	private TreeParent parent;
	private String name;
	private int flag;
	private String projectName;

	public TreeObject(String name) {
		this.name = name;
		this.flag = Constant.CHILD;
		this.projectName = "";
	}

	public TreeObject(String name, String projectName) {
		this.name = name;
		this.flag = Constant.CHILD;
		this.projectName = projectName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (!getProjectName().isEmpty()) {
			sb.append(getProjectName());
			sb.append(" > ");
		}
		sb.append(getName());
		return sb.toString();
	}

	/**
	 * Override equals method to use name to compare two TreeObject
	 */
	public boolean equals(Object object) {
		if ((object instanceof TreeObject) && ((TreeObject) object).getName() == this.getName()) {
			return true;
		} else {
			return false;
		}
	}

	public Object getAdapter(Class key) {
		return null;
	}
}