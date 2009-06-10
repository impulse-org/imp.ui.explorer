/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.ui.explorer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.imp.core.IMPMessages;
import org.eclipse.imp.model.ICompilationUnit;
import org.eclipse.imp.model.ISourceFolder;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jdt.internal.ui.packageview.PackagesMessages;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Add the <code>StatusBarUpdater</code> to your ViewPart to have the statusbar describing the selected elements.
 */
public class StatusBarUpdater implements ISelectionChangedListener {
    private final long LABEL_FLAGS= JavaElementLabels.DEFAULT_QUALIFIED | JavaElementLabels.ROOT_POST_QUALIFIED | JavaElementLabels.APPEND_ROOT_PATH
            | JavaElementLabels.M_PARAMETER_TYPES | JavaElementLabels.M_PARAMETER_NAMES | JavaElementLabels.M_APP_RETURNTYPE | JavaElementLabels.M_EXCEPTIONS
            | JavaElementLabels.F_APP_TYPE_SIGNATURE | JavaElementLabels.T_TYPE_PARAMETERS;

    private IStatusLineManager fStatusLineManager;

    public StatusBarUpdater(IStatusLineManager statusLineManager) {
        fStatusLineManager= statusLineManager;
    }

    /*
     * @see ISelectionChangedListener#selectionChanged
     */
    public void selectionChanged(SelectionChangedEvent event) {
        String statusBarMessage= formatMessage(event.getSelection());
        fStatusLineManager.setMessage(statusBarMessage);
    }

    protected String formatMessage(ISelection sel) {
        if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
            IStructuredSelection selection= (IStructuredSelection) sel;
            int nElements= selection.size();
            if (nElements > 1) {
                return IMPMessages.format(IMPMessages.StatusBarUpdater_num_elements_selected, String.valueOf(nElements));
            } else {
                Object elem= selection.getFirstElement();
                if (elem instanceof ISourceProject) {
                    return ((ISourceProject) elem).getRawProject().getName();
                } else if (elem instanceof ISourceFolder) {
                    return ((ISourceFolder) elem).getName();
                } else if (elem instanceof ICompilationUnit) {
                    return ((ICompilationUnit) elem).getName();
                } else if (elem instanceof IJavaElement) {
                    return formatJavaElementMessage((IJavaElement) elem);
                } else if (elem instanceof IResource) {
                    return formatResourceMessage((IResource) elem);
                } else if (elem instanceof ClassPathContainer) {
                    ClassPathContainer container= (ClassPathContainer) elem;
//                    return container.getLabel(container) + JavaElementLabels.CONCAT_STRING + container.getJavaProject().getElementName();
                    return getLabel(container) + JavaElementLabels.CONCAT_STRING + container.getJavaProject().getElementName();
                } else if (elem instanceof IAdaptable) {
                    IWorkbenchAdapter wbadapter= (IWorkbenchAdapter) ((IAdaptable) elem).getAdapter(IWorkbenchAdapter.class);
                    if (wbadapter != null) {
                        return wbadapter.getLabel(elem);
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    
	public String getLabel(ClassPathContainer cpc) {
		// SMS 26 Apr 2008
		// I adapted this method from the corresponding one on ClassPathContainer.
		// Most of the original is concerned with finding the container in the context
		// of a path, and I don't think we have a path here, so I've commented those
		// parts out.
		
		if (cpc != null && cpc instanceof IClasspathContainer)
			return ((IClasspathContainer)cpc).getDescription();
		
//		IPath path= fClassPathEntry.getPath();
//		String containerId= path.segment(0);
//		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(containerId);
//		if (initializer != null) {
//			String description= initializer.getDescription(path, fProject);
//			return Messages.format(PackagesMessages.ClassPathContainer_unbound_label, description); 
//		}
//		return Messages.format(PackagesMessages.ClassPathContainer_unknown_label, path.toString());
		
		return PackagesMessages.ClassPathContainer_unknown_label;
	}

	
    private String formatJavaElementMessage(IJavaElement element) {
        return JavaElementLabels.getElementLabel(element, LABEL_FLAGS);
    }

    private String formatResourceMessage(IResource element) {
        IContainer parent= element.getParent();
        if (parent != null && parent.getType() != IResource.ROOT)
            return element.getName() + JavaElementLabels.CONCAT_STRING + parent.getFullPath().makeRelative().toString();
        else
            return element.getName();
    }
}
