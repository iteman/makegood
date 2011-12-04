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

package com.piece_framework.makegood.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.ui.editor.IPhpScriptReconcilingListener;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.progress.UIJob;

import com.piece_framework.makegood.ui.EditorParser;
import com.piece_framework.makegood.ui.ide.ActiveEditor;

/**
 * @since 1.x.0
 */
public class TestOutlineViewController implements IPartListener2 {
    private static final String NAME = "MakeGood Test Outline Update";

    @SuppressWarnings("restriction")
    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        if (partRef.getId().equals(TestOutlineView.ID)) return;

        IEditorPart activePHPEditor = ActiveEditor.isPHP() ? ActiveEditor.get() : null;
        if (activePHPEditor == null) return;

        ((PHPStructuredEditor) activePHPEditor).addReconcileListener(new IPhpScriptReconcilingListener() {
            @Override
            public void aboutToBeReconciled() {
            }

            @Override
            public void reconciled(final Program program,
                                   boolean forced,
                                   IProgressMonitor progressMonitor) {
                new UIJob(NAME) {
                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        updateTestOutline(program.getSourceModule());
                        return Status.OK_STATUS;
                    }
                }.schedule();
            }
        });

        updateTestOutline(EditorParser.createActiveEditorParser().getSourceModule());
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    private void updateTestOutline(ISourceModule module) {
        TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
        if (view != null) {
            view.updateTestOutline(module);
        }
    }
}
