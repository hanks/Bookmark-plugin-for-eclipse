package bookmark.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.core.resources.IFile; 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.eclipse.jdt.core.ICompilationUnit;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */
 
class TreeObject implements IAdaptable, Serializable {
	private static final long serialVersionUID = -4275221961856278045L;
	public static final int PARENT = 1;
	public static final int CHILD = 0;
	private String name;
	private TreeParent parent;
	protected int flag;
	private String projectName;
	
	public TreeObject(String name) {
		this.name = name;
		this.flag = CHILD;
		this.projectName = "";
	}
	
	public TreeObject(String name, String projectName) {
		this.name = name;
		this.flag = CHILD;
		this.projectName = projectName;
	}
	
	public String getName() {
		return name;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = -1850997564183666463L;
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
	 * Add child to specified target node
	 * 
	 * Use recursion way to add child, if child is leaf, to find his parent and add to its parent
	 * 
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
				
				boolean is_ok = ((TreeParent)children[i]).addChild(target, child);
				
				if (is_ok) {
					return true;
				} 
			} else if (children[i].flag == CHILD) {
				if (children[i] == target) {
					TreeParent parent = children[i].getParent();
					if (parent.getParent() != null) {
						parent.getParent().addChild(parent, child);	
					} else { // when it is invisibleRoot, so there is 
						     // no parent, directly to add
						parent.addChild(child);
					}
					return true;
				} 
			}
		}
		return false;
	}
	
	/**
	 * Remove child from target node
	 * @param target
	 * @return true when remove successfully or else false
	 */
	public boolean removeSelectedChild(TreeObject target) {
		TreeObject[] children = this.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].flag == PARENT) {
				// if target is folder
				if (target == children[i]) {
					// delete child
					this.removeChild(target);
					return true;
				}
				
				boolean is_ok = ((TreeParent)children[i]).removeSelectedChild(target);
				
				if (is_ok) {
					return true;
				} 
			} else if (children[i].flag == CHILD) {
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
	public static final String DATA_STORE_KEY = "bookmark_datasource";

	private TreeViewer viewer;
	
	private Action addFolderAction;
	private Action addBookmarkAction;
	private Action deleteAction;
	private Action doubleClickAction;


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
			String imageKey = ISharedImages.IMG_OBJS_BKMRK_TSK;
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
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		// get data from store or else do initialization
		TreeParent invisibleRoot = this.loadPersistantData();

		// set data source
		viewer.setInput(invisibleRoot);

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
		
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.addBookmarkAction);
		manager.add(this.addFolderAction);
		manager.add(this.deleteAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
	
	}
	
	private void makeActions() {
		
		// remove selected folder or bookmark
		this.deleteAction = new Action() {
			public void run() {
				// get invisibleRoot
				TreeParent invisibleRoot = (TreeParent)viewer.getInput();
				
				// get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null) {
					showMessage("No selection in Bookmark View.");
				} else {
					TreeObject target = (TreeObject)obj;
					// confirm dialog
					String title = "Confirm";
					String question = "Do you really want to delelte this whole node?";
					boolean answer = MessageDialog.openConfirm(null, title, question);
					if (answer) {
						invisibleRoot.removeSelectedChild(target);	
					} 
					// update data source
					viewer.setInput(invisibleRoot);
					// save to persistent
					BookmarkView.savePersistantData(invisibleRoot);
				}
			}
		};
		this.deleteAction.setText("Delete");
		this.deleteAction.setToolTipText("Delete selected folder or bookmark.");
		this.deleteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));
		
		// use user input to add parent 
		this.addFolderAction = new Action() {
			public void run() {
				String parentName;
			    // create an input dialog to get user input
			    String title = "Input";
			    String question = "Please enter folder name:";
			    String initValue = "";
			    InputDialog dlg = new InputDialog(null,
			    		title,
			    		question,
			    		initValue,
			    		null);
			    dlg.open();
			    if (dlg.getReturnCode() != Window.OK) {
			    	return ;
			    } else {
			    	parentName = dlg.getValue();
			    }
			    
    		    // new a folder
			    TreeParent newParent = new TreeParent(parentName);
			    // get invisible root
			    TreeParent invisibleRoot = (TreeParent)viewer.getInput();
			    
			    // get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null) {
					// no selection, default to add to the invisibleRoot
					invisibleRoot.addChild(newParent);
				} else {
					invisibleRoot.addChild((TreeObject)obj, newParent);
				}
				
				// add back to viewer
				viewer.setInput(invisibleRoot);
				
				// save to persistent
				BookmarkView.savePersistantData(invisibleRoot);
			}
		};
		this.addFolderAction.setText("Add folder here");
		this.addFolderAction.setToolTipText("Add folder here");
		this.addFolderAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
		
		// add book mark to selected parent
		this.addBookmarkAction = new Action() {
			public void run() {
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
					// check selection from package explorer 
					ISelectionService service = getSite().getWorkbenchWindow()
							.getSelectionService();
					IStructuredSelection package_exploer_selection = (IStructuredSelection) service
							.getSelection("org.eclipse.jdt.ui.PackageExplorer");
					if (package_exploer_selection != null) {
						Object obj = package_exploer_selection.getFirstElement();
						if (obj == null) {
							showMessage("No selection in package explorer");
						} else {
							// get file info for selection from package explorer
							IResource resource = ((ICompilationUnit)obj).getResource();
							
							if (resource.getType() == IResource.FILE) {
							    IFile ifile = (IFile) resource;
							    relativePath = ifile.getProjectRelativePath().toOSString();
								projectName = ifile.getProject().getName();
							}
						}
					} else {
						showMessage("No active editor or selection in package explorer");
						return ;
					}
				}
				
				// create leaf with file info
				TreeObject child = new TreeObject(relativePath, projectName);
				
				// get invisibleRoot
				TreeParent invisibleRoot = (TreeParent)viewer.getInput();
				
				// get selection
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj == null) {
					// default to insert invisibleRoot
					invisibleRoot.addChild(child);
				} else {
					TreeObject targetParent = (TreeObject)obj;
					invisibleRoot.addChild(targetParent, child);
				}
				
				// update data source
				viewer.setInput(invisibleRoot);
				
				// save to persistent
				BookmarkView.savePersistantData(invisibleRoot);
			}
		};
		this.addBookmarkAction.setText("Add bookmark here");
		this.addBookmarkAction.setToolTipText("Add bookmark here");
		this.addBookmarkAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_BKMRK_TSK));
		
		// double click action to open file
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				
				if (obj != null) {
					TreeObject treeObject = (TreeObject)obj;
					if (treeObject.flag == 1) {
						// expand and collapse folder when double click 
						if (viewer.getExpandedState(treeObject)) {
							viewer.collapseToLevel(treeObject, 1);
						} else {
							viewer.expandToLevel(treeObject, 1);
						}
						return ;
					}
					String relativePath = treeObject.getName();
                    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                    IProject project = workspaceRoot.getProject(treeObject.getProjectName()); 
                    IFile file1 = project.getFile((new Path(relativePath)));
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage(); 
                    IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file1.getName());

					try {
					    page.openEditor(new FileEditorInput(file1), desc.getId()); 
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
	
	/**
	 * Use eclipse Preferences API to make data persistent
	 * @param dataSource
	 */
	private static void savePersistantData(TreeParent dataSource) {
		Preferences prefs = InstanceScope.INSTANCE
				  .getNode(ID);
		
		// change object to string
		Gson gson = new Gson();
		
		// change object byte array
		ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o;
		try {
			o = new ObjectOutputStream(b);
			o.writeObject(dataSource);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        byte[] byteDataArray = b.toByteArray();
		
        // use gson to change byte array to string
		String json_str = gson.toJson(byteDataArray);
		
		prefs.put(DATA_STORE_KEY, json_str);
		try {
			// store to disk
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	private TreeParent loadPersistantData() {
		Preferences prefs = InstanceScope.INSTANCE
				  .getNode(ID);
		
		String json_str = prefs.get(DATA_STORE_KEY, "");
		
		if (json_str == "") {
			// no data source yet, do initialization
			TreeParent invisibleRoot = new TreeParent("");
			return invisibleRoot;
		} else {
			Gson gson = new Gson();
			byte[] byteDataArray = gson.fromJson(json_str, byte[].class);
			
			// deserialize object from byteDataArray
			ByteArrayInputStream b = new ByteArrayInputStream(byteDataArray);
	        ObjectInputStream o;
	        TreeParent invisibleRoot = null;
			try {
				o = new ObjectInputStream(b);
		        invisibleRoot = (TreeParent)o.readObject();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return invisibleRoot;
		}
	}
}