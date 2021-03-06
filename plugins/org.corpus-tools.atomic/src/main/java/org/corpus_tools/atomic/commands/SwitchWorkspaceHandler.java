/*******************************************************************************
 * Copyright 2015 Friedrich-Schiller-Universität Jena
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
package org.corpus_tools.atomic.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.corpus_tools.atomic.workspace.SelectWorkspaceDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Description
 * <p>
 * 
 * @author Stephan Druskat <stephan.druskat@uni-jena.de>
 */
public class SwitchWorkspaceHandler extends AbstractHandler {
	
	/** 
	 * Defines a static logger variable so that it references the {@link org.apache.logging.log4j.Logger} instance named "SwitchWorkspaceHandler".
	 */
	private static final Logger log = LogManager.getLogger(SwitchWorkspaceHandler.class);
	
	/*
	 * @copydoc @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SelectWorkspaceDialog selectWorkspaceDialog = new SelectWorkspaceDialog(true);
		int pick = selectWorkspaceDialog.open();
		if (pick == Dialog.CANCEL) {
			log.trace("Cancel has been pressed in instance of {}. Closing the dialog.", this.getClass().getName());
			return false;
		}
		else if (pick == Dialog.OK) {
			log.trace("OK has been pressed in instance of {}. Proceeding with switching the workspace.", this.getClass().getName());
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Switch Workspace", "Atomic will now restart with the new workspace.");
			PlatformUI.getWorkbench().restart();
			return true;
		}
		else {
			return null;
		}
	}

}
