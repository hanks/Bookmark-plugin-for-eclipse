package bookmark.views;

import java.util.ArrayList;

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
public class TreeParent extends TreeObject {

	/**
	 *
	 */
	private static final long serialVersionUID = -1850997564183666463L;
	private ArrayList<TreeObject> children;

	public TreeParent(String name) {
		super(name);
		this.flag = Constant.PARENT;
		children = new ArrayList<TreeObject>();
	}

	public void addChild(TreeObject child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(TreeObject child) {
		children.remove(child);
		child.setParent(null);
	}

	/**
	 *
	 * @return TreeObject list or TreeObject[] when no children
	 */
	public TreeObject[] getChildren() {
		return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Add child to specified target node
	 *
	 * Use recursion way to add child, if child is leaf, to find his parent and
	 * add to its parent
	 *
	 * @param obj
	 * @param path
	 */
	public boolean addChild(TreeObject target, TreeObject child) {
		TreeObject[] children = this.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].flag == Constant.PARENT) {
				// if target is folder
				if (target == children[i]) {
					// insert child
					((TreeParent) children[i]).addChild(child);
					return true;
				}

				boolean is_ok = ((TreeParent) children[i]).addChild(target, child);

				if (is_ok) {
					return true;
				}
			} else if (children[i].flag == Constant.CHILD) {
				if (children[i] == target) {
					TreeParent parent = children[i].getParent();
					parent.addChild(child);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Remove child from target node
	 *
	 * @param target
	 * @return true when remove successfully or else false
	 */
	public boolean removeSelectedChild(TreeObject target) {
		TreeObject[] children = this.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].flag == Constant.PARENT) {
				// if target is folder
				if (target == children[i]) {
					// delete child
					this.removeChild(target);
					return true;
				}

				boolean is_ok = ((TreeParent) children[i]).removeSelectedChild(target);

				if (is_ok) {
					return true;
				}
			} else if (children[i].flag == Constant.CHILD) {
				if (children[i] == target) {
					TreeParent parent = children[i].getParent();
					parent.removeChild(target);
					return true;
				}
			}
		}
		return false;
	}
}