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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import com.piece_framework.makegood.ui.views.ViewOpener;
import com.piece_framework.makegood.ui.EditorParser;

public class TestOutlinePartListener implements IPartListener2 {
    @Override
    public void partActivated(IWorkbenchPartReference partReference) {
        IWorkbenchPart activePart = partReference.getPage().getActivePart();
        if (activePart == null) return;
        if (!(activePart instanceof IEditorPart)) return;

        IEditorPart editor = (IEditorPart) activePart;

        TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
        if (view != null) view.setViewerInput(new EditorParser(editor));

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

                    if (!(event.getSource() instanceof ITextViewer)) return;
                    ITextViewer textViewer = (ITextViewer) event.getSource();

                    EditorParser parser = new EditorParser(textViewer.getDocument());
                    if (!view.hasContent()) view.setViewerInput(parser);
                    view.setViewerSelection(parser);
                }
            });
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partReference) {}

    @Override
    public void partClosed(IWorkbenchPartReference partReference) {}

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
