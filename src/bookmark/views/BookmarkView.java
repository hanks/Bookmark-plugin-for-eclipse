package bookmark.views;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.IFile; 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class BookmarkView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "bookmark.views.BookmarkView";
	public static final int PARENT = 1;
	public static final int CHILD = 0;

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action action3;
	private Action action4;
	private Action action5;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		protected int flag;
		
		public TreeObject(String name) {
			this.name = name;
			this.flag = CHILD;
		}
		
		public String getName() {
			return name;
		}
		
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		
		public TreeParent getParent() {
			return parent;
		}
		
		public String toString() {
			return getName();
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
	
	class TreeParent extends TreeObject {
		private ArrayList<TreeObject> children;
		
		public TreeParent(String name) {
			super(name);
			this.flag = PARENT;
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
		public TreeObject [] getChildren() {
    		return (TreeObject [])children.toArray(new TreeObject[children.size()]);	
		}
		
		public boolean hasChildren() {
			return children.size()>0;
		}
		
		/**
		 * Return index path for object, almost to find parent index path
		 * @param obj
		 * @param path
		 */
		public boolean addChild(TreeObject target, TreeObject child) {
			TreeObject[] children = this.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i].flag == PARENT) {
					// if target is folder
					if (target == children[i]) {
						// insert child
						((TreeParent)children[i]).addChild(child);
						return true;
					}
					
					boolean can_add = ((TreeParent)children[i]).addChild(target, child);
					
					if (can_add) {
						return true;
					} 
				} else if (children[i].flag == CHILD) {
					if (children[i] == target) {
						TreeParent parent = children[i].getParent();
						parent.getParent().addChild(parent, child);
						return true;
					} 
				}
			}
			return false;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent)
			   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			// if need to change customize image
			// return new Image(null, new FileInputStream("images/file.gif"));
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BookmarkView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		//viewer.setInput(getViewSite());
		// test customize data
		TreeParent invisibleRoot = new TreeParent("");
		TreeParent root = new TreeParent("Root");
		TreeParent parent1 = new TreeParent("Parent1");
		TreeParent parent2 = new TreeParent("Parent2");
		TreeObject leaf1 = new TreeObject("leaf1");
		TreeObject leaf2 = new TreeObject("leaf2");
		parent1.addChild(leaf1);
		parent2.addChild(leaf2);
		root.addChild(parent1);
		root.addChild(parent2);
		invisibleRoot.addChild(root);
		viewer.setInput(invisibleRoot);
		
		Object obj = viewer.getInput();
		System.out.println("here: " + (TreeParent)obj);
		

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "bookmark.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// add drop feature in tree 
		DelegatingDragAdapter dragAdapter = new DelegatingDragAdapter();
        dragAdapter.addDragSourceListener(new TransferDragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				// TODO Auto-generated method stub
				System.out.println("Drag start");
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// TODO Auto-generated method stub
				System.out.println("Drag end");
			}

			@Override
			public Transfer getTransfer() {
				// TODO Auto-generated method stub
				return null;
			}
        });
        viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, 
            dragAdapter.getTransfers(), dragAdapter);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BookmarkView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		/**
		 * add actions to tool bar
		 */
		manager.add(action1);
		manager.add(action2);
		manager.add(action3);
		manager.add(action4);
		manager.add(action5);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}
	
	private void makeActions() {
		// get active file info action
		action1 = new Action() {
			public void run() {
				//showMessage("Action 1 executed");
				System.out.println("Action 1 executed");
				
				// get the path of active editor file
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
					IFile file = input.getFile();
					System.out.println("relative path: " + file.getProjectRelativePath());
					
					IPath path = ((FileEditorInput) input).getPath();
					System.out.println("absolute path: " + path);
					
					System.out.println("project name: " + file.getProject().getName());
				} else {
					System.out.println("no active editor");
				}
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		// open file in editor action
		action2 = new Action() {
			public void run() {
				//showMessage("Action 2 executed");
				System.out.println("Action 2 executed");
				//String absolutePath = "/Users/han.zhou/runtime-EclipseApplication/test/src/test.java";
				String relativePath = "src/test.java";
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = workspaceRoot.getProject("test");
				
				IFile file1 = project.getFile((new Path(relativePath))); 
				//IFile file2 = workspaceRoot.getFileForLocation(Path.fromOSString(absolutePath)); 
				
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				
				// count editor id from file type
				IEditorDescriptor desc = PlatformUI.getWorkbench().
				        getEditorRegistry().getDefaultEditor(file1.getName());
				try {
					page.openEditor(new FileEditorInput(file1), desc.getId());
					System.out.println("relative is ok");
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
//				try {
//					page.openEditor(new FileEditorInput(file2), desc.getId());
//					System.out.println("absolute is ok");
//				} catch (PartInitException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		// add directory and leaf action
		action3 = new Action() {
			public void run() {
				System.out.println("empty");
			}
		};
		action3.setText("Action 3");
		action3.setToolTipText("Action 3 tooltip");
		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		// add detect selection action
		action4 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null) {
					System.out.println("no selection");
				} else {
				    TreeObject a = (TreeObject)obj;
				    if (a.flag == 0) {
				    	System.out.println("leaf: " + a.toString());
				    } else {
				    	System.out.println("parent: " + a.toString());
				    }
				}
			}
		};
		action4.setText("Action 4");
		action4.setToolTipText("Action 4 tooltip");
		action4.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		// add book mark to selected parent
		action5 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null) {
					showMessage("Please select an bookmark folder.");
					return ;
				} else {
					TreeObject targetParent = (TreeObject)obj;
					
					//get active editor info
					String relativePath = "";
					String projectName = "";
					
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					IEditorPart editor = page.getActiveEditor();
					if (editor != null) {
						IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
						IFile file = input.getFile();
						relativePath = file.getProjectRelativePath().toOSString();
						projectName = file.getProject().getName();
					} else {
						showMessage("no active editor");
						return ;
					}
					
					// create leaf with file info
					TreeParent invisibleRoot = (TreeParent)viewer.getInput();
					TreeObject child = new TreeObject(relativePath);
					invisibleRoot.addChild(targetParent, child);
					viewer.setInput(invisibleRoot);
				}
			}
		};
		action5.setText("Action 5");
		action5.setToolTipText("Action 5 tooltip");
		action5.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		// double click action to open file
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				//showMessage("Double-click detected on "+obj.toString());
				if (obj != null) {
					TreeObject treeObject = (TreeObject)obj;
					if (treeObject.flag == 1) {
						showMessage("Please select a bookmark.");
						return ;
					}
					String relativePath = treeObject.getName();
                    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                    IProject project = workspaceRoot.getProject("test"); 
                    IFile file1 = project.getFile((new Path(relativePath)));
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage(); 
                    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file1.getName());

					try {
					    page.openEditor(new FileEditorInput(file1), desc.getId()); //使用page来打开编辑器，需要传入文件对象，编辑器id
					} catch (PartInitException e) {
					  e.printStackTrace();
					}
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Bookmark View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}