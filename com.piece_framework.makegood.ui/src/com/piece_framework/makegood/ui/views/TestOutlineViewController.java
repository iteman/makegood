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

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.piece_framework.makegood.ui.EditorParser;
import com.piece_framework.makegood.ui.ide.ActiveEditor;

/**
 * @since 1.x.0
 */
public class TestOutlineViewController implements IPartListener2 {
    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
        IEditorPart activePHPEditor = ActiveEditor.isPHP() ? ActiveEditor.get() : null;
        if (activePHPEditor == null) return;
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
