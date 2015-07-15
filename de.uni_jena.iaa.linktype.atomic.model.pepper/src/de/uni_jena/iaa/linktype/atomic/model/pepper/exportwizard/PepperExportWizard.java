/*******************************************************************************
 * Copyright 2014 Friedrich Schiller University Jena
 * Vivid Sky - Softwaremanufaktur, Michael Gr�bsch.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.uni_jena.iaa.linktype.atomic.model.pepper.exportwizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.FormatDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.MODULE_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.PepperModuleDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.core.ModuleResolver;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperExporter;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.AbstractPepperWizard;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.AbstractPepperWizard.WizardMode;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.PepperModuleRunnable;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.PepperWizardPageDirectory;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.PepperWizardPageFormat;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.PepperWizardPageModule;
import de.uni_jena.iaa.linktype.atomic.model.pepper.wizard.PepperWizardPageProperties;

/**
 *
 * @author  Michael Gr�bsch
 * @version $Revision$, $Date$
 */
public class PepperExportWizard
  extends 
    AbstractPepperWizard<PepperExporter>
  implements 
    IExportWizard
{
  protected IProject selectedProject = null;

  public PepperExportWizard()
  {
    super("Export via Pepper", WizardMode.EXPORT);
  }

// =============================================> called by Eclipse  
  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    initialize();
    if (1 == selection.size())
    {
      Object element = selection.getFirstElement();
      if (element instanceof IProject)
      {
        selectedProject = (IProject) element;
      }
      else
      {
        new MessageDialog
          ( this.getShell()
          , "Error"
          , null
          , "Selection is not a project!"
          , MessageDialog.ERROR
          , new String[]{ IDialogConstants.OK_LABEL }
          , 0).open();
      }
    }
    else
    {
      new MessageDialog
        ( this.getShell()
        , "Error"
        , null
        , "To run the Pepper Export Wizard select exactly one project!"
        , MessageDialog.ERROR
        , new String[]{ IDialogConstants.OK_LABEL }
        , 0).open();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addPages()
  {
    if (selectedProject != null)
    {
      addPage(new PepperWizardPageModule<PepperExporter>(this, "selectExporter", "Select Export Module", DEFAULT_PAGE_IAMGE_DESCRIPTOR, "Select the pepper export module."));
      addPage(new PepperWizardPageFormat<PepperExporter>(this, "selectFormat", "Select Export Format", DEFAULT_PAGE_IAMGE_DESCRIPTOR, "Select the pepper export format."));
      addPage(new PepperWizardPageDirectory<PepperExporter>(this, "selectTargetPath", "Select Export Path", DEFAULT_PAGE_IAMGE_DESCRIPTOR, "Select the pepper export path."));
      addPage(new PepperWizardPageProperties<PepperExporter>(this, "selectProperties", "Select Export Properties", DEFAULT_PAGE_IAMGE_DESCRIPTOR, "Edit the pepper export module properties."));
    }
  }
//=============================================< called by Eclipse
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  protected List<PepperExporter> resolvePepperModules(ModuleResolver pepperModuleResolver)
//  {
//    return pepperModuleResolver.getPepperExporters();
//  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<FormatDesc> getSupportedFormats()
  {
    PepperExporter module = getPepperModule();
    return module != null ? module.getSupportedFormats() : new ArrayList<FormatDesc>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PepperModuleRunnable createModuleRunnable(IProject project, boolean cancelable)
  {
    return new ExportModuleRunnable(this, project, cancelable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected IProject getProject() throws CoreException
  {
    return selectedProject;
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public List<PepperModuleDesc> getPepperModules() {
  	return(super.getPepperModules(MODULE_TYPE.EXPORTER));
  }
}
