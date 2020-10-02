package bookmark.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.Preferences;

import com.google.gson.Gson;

import bookmark.constant.Constant;
import bookmark.views.action.AddAllBookmarkAction;
import bookmark.views.action.AddBookmarkAction;
import bookmark.views.action.AddFolderAction;
import bookmark.views.action.CutAction;
import bookmark.views.action.DeleteAction;
import bookmark.views.action.DoubleClickAction;
import bookmark.views.action.PasteAction;
import bookmark.views.action.RenameAction;
import bookmark.views.viewer.NameSorter;
import bookmark.views.viewer.ViewContentProvider;
import bookmark.views.viewer.ViewLabelProvider;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained from the model. The sample creates a dummy model on the fly, but a real implementation would connect
 * to the model available either in this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can present the same model objects using different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views in order to ensure that objects of the same type are presented in the same way everywhere.
 * <p>
 */
public class BookmarkView extends ViewPart {

	private TreeViewer viewer;

	private Action addFolderAction;
	private Action addBookmarkAction;
	private Action addAllBookmarkAction;
	private Action deleteAction;
	private Action renameAction;
	private Action doubleClickAction;

	private Action cutAction;
	private Action pasteAction;
	private Mark marker;


	/**
	 * The constructor.
	 */
	public BookmarkView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
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
		this.makeActions();
		this.hookContextMenu();
		this.hookDoubleClickAction();
		this.contributeToActionBars();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private TreeParent loadPersistantData() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Constant.ID);

		String json_str = prefs.get(Constant.DATA_STORE_KEY, "");

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
				invisibleRoot = (TreeParent) o.readObject();
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

	@SuppressWarnings("deprecation")
	private void makeActions() {
		this.addFolderAction = new AddFolderAction(viewer);
		this.addBookmarkAction = new AddBookmarkAction(viewer, getSite().getWorkbenchWindow().getSelectionService());
		this.addAllBookmarkAction = new AddAllBookmarkAction(viewer);
		this.deleteAction = new DeleteAction(viewer);
		this.renameAction = new RenameAction(viewer);
		this.doubleClickAction = new DoubleClickAction(viewer);
		this.marker = new Mark();
		this.cutAction = new CutAction(viewer, marker);
		this.pasteAction = new PasteAction(viewer, marker);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {

				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					TreeObject target = (TreeObject) selection.getFirstElement();
					if (target instanceof TreeParent) {
						manager.add(addBookmarkAction);
						manager.add(addFolderAction);
						manager.add(deleteAction);
						manager.add(renameAction);
						manager.add(addAllBookmarkAction);
						
						manager.add(cutAction);
						manager.add(pasteAction);
					} else {
						manager.add(deleteAction);
					}
				} else {
					manager.add(addBookmarkAction);
					manager.add(addFolderAction);
					manager.add(addAllBookmarkAction);

					manager.add(cutAction);
					manager.add(pasteAction);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {

	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.addBookmarkAction);
		manager.add(this.addFolderAction);
		manager.add(this.deleteAction);
		
		manager.add(this.cutAction);
		manager.add(this.pasteAction);
	}

}