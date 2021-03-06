/**
 * 
 */
package org.corpus_tools.atomic.api.editors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.corpus_tools.atomic.api.events.PartContextListener;
import org.corpus_tools.atomic.exceptions.AtomicGeneralException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * An abstraction for editors working on Salt {@link SDocumentGraph}s.
 * 
 * This class is intended to be extended by clients implementing new
 * editors that work on {@link SDocumentGraph}s. The input for
 * editors extending this class must be a SaltXML file containing
 * the persisted document graph. The input type must be
 * {@link FileEditorInput}. The editor retains a single instance
 * instance of {@link SDocumentGraph} which contains the contents
 * of the input file.
 *
 * @author Stephan Druskat <mail@sdruskat.net>
 *
 */
public abstract class DocumentGraphEditor extends EditorPart {
	
	private static final Logger log = LogManager.getLogger(DocumentGraphEditor.class);


	/**
	 * The default name for {@link SaltProject} serialization files.
	 */
	public static final String SALT_PROJECT_FILE_NAME = "saltProject.salt";
	
	
	protected SDocumentGraph graph = null;


	protected boolean dirty;


	private boolean isValidInput = true;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		IPath resPath = ((FileEditorInput) getEditorInput()).getPath();
		SaltUtil.saveDocumentGraph(getGraph(), URI.createFileURI(resPath.toOSString()));
		
		this.dirty = false;
		firePropertyChange(EditorPart.PROP_DIRTY);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {	
//		IPath origResPath = ((FileEditorInput) getEditorInput()).getPath();
//		FileDialog dlg = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//		// use the folder of the current file as argument
//		dlg.setFilterPath(origResPath.toFile().getParent());
//		dlg.setFilterExtensions(new String[] {"*.salt", "*.*"});
//		String path = dlg.open();
//		if(path != null) {
//			SaltUtil.saveDocumentGraph(getGraph(), URI.createFileURI(path));
//			
//			this.dirty = false;
//			firePropertyChange(EditorPart.PROP_DIRTY);
//		}
	}

	/** 
	 * Handles initialization of the editor.
	 * 
	 * Clients should check against {@link #isInputValid()} after calling
	 * `super.init(site, input)` in order to catch any exceptions that
	 * may occur when the initialization is aborted (e.g., due to the
	 * user trying to open a Salt project file with a document editor
	 * extending this class).
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// Site and input must be set in init
		setSite(site);
		setInput(input);
		
		if (!(input instanceof FileEditorInput)) {
			log.error("Input for editors of type {} must be of type {}, but the provided input is of type {}!", this.getClass().getName(), FileEditorInput.class.getName(), input.getClass().getName(), new AtomicGeneralException());
		}
		else {
			String filePath = ((FileEditorInput) input).getPath().makeAbsolute().toOSString();
			log.trace("Input for editor {} is file '{}'.", this.getClass().getName(), filePath);
			if (filePath.endsWith(SALT_PROJECT_FILE_NAME)) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Cannot open project file", "This editor does work only on Salt document files, but you have tried to open a Salt project file (\"" + SALT_PROJECT_FILE_NAME + "\").\n\nClosing editor.");
				isValidInput = false;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						getSite().getPage().closeEditor(DocumentGraphEditor.this, false); 
					}
				});
				log.info("Attempt to open editor for Salt documents (" + this.getClass().getSimpleName() + ") with Salt project file aborted.");
				return;
			}
			IProject iProject = ((FileEditorInput) input).getFile().getProject();
			IFile saltProjectResource = null;
			try {
				for (IResource member : iProject.members()) {
					if (member instanceof IFile && member.getName().equalsIgnoreCase(SALT_PROJECT_FILE_NAME)) { // FIXME Externalize and treat for case, etc
						saltProjectResource = (IFile) member;
					}
				}
			}
			catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String projectFilePath = saltProjectResource.getRawLocation().toOSString();
			SaltProject saltProject = SaltFactory.createSaltProject();
			saltProject.loadCorpusStructure(URI.createFileURI(projectFilePath));
			for (SDocument doc : saltProject.getCorpusGraphs().get(0).getDocuments()) {
				if (doc.getDocumentGraphLocation().equals(URI.createFileURI(filePath))) {
					doc.loadDocumentGraph();
					graph = doc.getDocumentGraph();
				}
			}
			log.trace("Loaded document graph {}.", graph);
			
			// Set up editor for automatic context switches on activation/deactivation 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new PartContextListener(site.getId(), site.getPluginId()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return dirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/** 
	 * Simply passes the parent argument to an abstract method which
	 * clients need to override to create a control for this
	 * editor part.
	 * 
	 * **Note:** Clients should not override this method, albeit its
	 * visibility. Instead, they should override the method 
	 * {@link #createEditorPartControl(Composite)}!
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		createEditorPartControl(parent);
	}

	/**
	 * TODO: Description
	 *
	 * @param parent
	 */
	public abstract void createEditorPartControl(Composite parent);

//	/* (non-Javadoc)
//	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
//	 */
//	@Override
//	public void setFocus() {
//		// TODO Auto-generated method stub
//
//	}

	/**
	 * Returns the single instance of {@link SDocumentGraph} that
	 * the editor can work on. The graph is initially set in {@link #init(IEditorSite, IEditorInput)}
	 * and the referenced object itself should not be changed anywhere 
	 * in the editor.
	 * 
	 * @return the graph
	 */
	public final SDocumentGraph getGraph() {
		return graph;
	}

	/**
	 * @param dirty the dirty to set
	 */
	public final void setDirty(boolean dirty) {
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	/**
	 * @return the isValidInput
	 */
	public final boolean isInputValid() {
		return isValidInput;
	}
	
	@Override
	public void setFocus() {
		/*
		 * Empty implementation here prevents forced
		 * implementation down the inheritance line
		 * but can still be overridden to provide
		 * customized implementations.
		 */
	}

}
