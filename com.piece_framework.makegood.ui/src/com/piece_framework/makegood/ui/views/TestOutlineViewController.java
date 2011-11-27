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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.ui.editor.IPhpScriptReconcilingListener;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.progress.UIJob;

import com.piece_framework.makegood.ui.EditorParser;
import com.piece_framework.makegood.ui.TestClassCollectorChangeListener;
import com.piece_framework.makegood.ui.ide.ActiveEditor;

/**
 * @since 1.x.0
 */
public class TestOutlineViewController implements TestClassCollectorChangeListener, IPartListener2 {
    private static final String NAME = "MakeGood Test Outline Update";
    private IEditorPart activePHPEditor;

    @Override
    public void collectorChanged(final IType type) {
        if (Job.getJobManager().find(NAME).length > 0) return;
        if (activePHPEditor == null) return;
        if (!existsTypeInEditor(type, activePHPEditor)) return;

        UIJob job = new UIJob(NAME) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                updateTestOutline();
                return Status.OK_STATUS;
            }

            @Override
            public boolean belongsTo(Object family) {
                if (!(family instanceof String)) return super.belongsTo(family);

                return getName().equals((String) family);
            }
        };
        job.schedule();
    }

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
                new UIJob("MakeGood Update Test Outline") {
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

    private boolean existsTypeInEditor(IType type, IEditorPart editor) {
        EditorParser parser = new EditorParser(editor);
        if (parser.getTypes() == null) return false;
        for (IType typeInEditor: parser.getTypes()) {
            if (type.getElementName().equals(typeInEditor.getElementName())) {
                return true;
            }
        }
        return false;
    }
}
