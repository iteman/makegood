/**
 * Copyright (c) 2010 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.ui.editor.IPhpScriptReconcilingListener;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.progress.UIJob;

import com.piece_framework.makegood.ui.ide.ActiveEditor;
import com.piece_framework.makegood.ui.views.TestOutlineView;
import com.piece_framework.makegood.ui.views.ViewOpener;

/**
 * @since 1.4.0
 */
public class TestOutlinePartListener implements IPartListener2 {
    @Override
    public void partActivated(IWorkbenchPartReference partReference) {
        boolean activatedIsEditor = partReference.getPage().getActivePart() instanceof IEditorPart;
        if (!activatedIsEditor) return;

        TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
        if (view != null) view.resetViewerInput();

        if (!ActiveEditor.isPHP()) return;

        if (view != null) {
            view.setViewerInput();
            view.setViewerSelection();
        }

        addReconcileListener(ActiveEditor.get());
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partReference) {}

    @Override
    public void partClosed(IWorkbenchPartReference partReference) {
        if (ActiveEditor.get() == null) {
            TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
            if (view != null) view.resetViewerInput();
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partReference) {}

    @Override
    public void partOpened(IWorkbenchPartReference partReference) {}

    @Override
    public void partHidden(IWorkbenchPartReference partReference) {}

    @Override
    public void partVisible(IWorkbenchPartReference partReference) {}

    @Override
    public void partInputChanged(IWorkbenchPartReference partReference) {}

    @SuppressWarnings("restriction")
    public static void addReconcileListener(IEditorPart editor) {
        ((PHPStructuredEditor) editor).addReconcileListener(new IPhpScriptReconcilingListener() {
            private String previousXML;

            @Override
            public void aboutToBeReconciled() {}

            @Override
            public void reconciled(Program program,
                                   boolean forced,
                                   IProgressMonitor progressMonitor) {
                if (!program.toString().equals(previousXML)) {
                    if (Job.getJobManager().find("MakeGood Test Outline Update").length > 0) return;

                    Job job = new UIJob("MakeGood Test Outline Update") { //$NON-NLS-1$
                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
                            if (view != null) {
                                view.resetViewerInput();
                                view.setViewerInput();
                            }
                            return Status.OK_STATUS;
                        }

                        @Override
                        public boolean belongsTo(Object family) {
                            if (!(family instanceof String)) return super.belongsTo(family);

                            return getName().equals((String) family);
                        }
                    };
                    job.schedule();

                    previousXML = program.toString();
                }
            }
        });
    }
}
