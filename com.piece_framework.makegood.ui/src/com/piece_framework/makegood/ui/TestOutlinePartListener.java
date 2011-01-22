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

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartReference;

import com.piece_framework.makegood.ui.ide.ActiveEditor;
import com.piece_framework.makegood.ui.views.TestOutlineView;
import com.piece_framework.makegood.ui.views.ViewShow;

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

        IEditorPart editor = ActiveEditor.get();
        editor.addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propertyId) {
                // TODO: when source changed
            }
        });

        ISelectionProvider provider = editor.getEditorSite().getSelectionProvider();
        if (provider instanceof IPostSelectionProvider) {
            ((IPostSelectionProvider) provider).addPostSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
                    if (view == null) return;
                    if (!view.hasContent()) view.setViewerInput();
                    view.setViewerSelection();
                }
            });
        }
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
}
