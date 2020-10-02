package bookmark.views;

public class Mark {

	private TreeObject node;
	private Boolean mode;

	public TreeObject getNode() {
		return node;
	}

	public void setNode(TreeObject node) {
		this.node = node;
	}

	public Boolean getMode() {
		return mode;
	}

	public void setMode(Boolean mode) {
		this.mode = mode;
	}

	public void resetMark() {
		this.node = null;
		this.mode = null;
	}

}