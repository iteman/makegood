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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.viewsupport.DecoratingModelLabelProvider;
import org.eclipse.dltk.ui.viewsupport.ScriptUILabelProvider;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.piece_framework.makegood.ui.Activator;
import com.piece_framework.makegood.ui.EditorParser;
import com.piece_framework.makegood.ui.MakeGoodContext;
import com.piece_framework.makegood.ui.TestClass;
import com.piece_framework.makegood.ui.ide.ActiveEditor;

/**
 * @since 1.x.0
 */
public class TestOutlineView extends ViewPart {
    public static final String ID = "com.piece_framework.makegood.ui.views.testOutlineView"; //$NON-NLS-1$

    private TreeViewer viewer;

    public TestOutlineView() {}

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        viewer = new TreeViewer(parent);
        viewer.setContentProvider(new TestOutlineContentProvider());
        viewer.setLabelProvider(new DecoratingModelLabelProvider(new ScriptUILabelProvider()));
        TreeEventListener eventListener = new TreeEventListener();
        viewer.addSelectionChangedListener(eventListener);
        viewer.addDoubleClickListener(eventListener);

        MenuManager contextMenuManager = new MenuManager();
        contextMenuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = contextMenuManager.createContextMenu(viewer.getTree());
        viewer.getTree().setMenu(contextMenu);

        getSite().registerContextMenu(contextMenuManager, viewer);
        getSite().setSelectionProvider(viewer);

        updateTestOutline();
    }

    @Override
    public void setFocus() {}

    public void updateTestOutline() {
        if (viewer == null) return;
        if (viewer.getContentProvider() == null) return;

        viewer.setInput(null);

        if (!ActiveEditor.isPHP()) return;

        EditorParser parser = EditorParser.createActiveEditorParser();
        List<TestClass> testClasses =
            MakeGoodContext.getInstance().getTestClassCollector().getAtSourceModule(parser.getSourceModule());
        viewer.setInput(testClasses);
        viewer.expandAll();
    }

    private void showEditor(IMember member, Boolean showWhenDeactivate) {
        if (member == null) return;
        if (member.getSourceModule() == null) return;

        ISourceRange nameRange = null;
        try {
            nameRange = member.getNameRange();
        } catch (ModelException e) {
            Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
        }
        if (nameRange == null) return;

        boolean targetIsActivate =
            EditorParser.createActiveEditorParser().getSourceModule().equals(member.getSourceModule());
        if (targetIsActivate) {
            ((ITextEditor) ActiveEditor.get()).selectAndReveal(
                nameRange.getOffset(),
                nameRange.getLength());
        } else {
            if (showWhenDeactivate) {
                EditorOpener.open(
                    (IFile) member.getResource(),
                    nameRange.getOffset(),
                    nameRange.getLength());
            }
        }
    }

    private class TreeEventListener implements ISelectionChangedListener, IDoubleClickListener {
        @Override
        public void doubleClick(DoubleClickEvent event) {
            showEditor(getMember(event.getSelection()), true);
        }

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            showEditor(getMember(event.getSelection()), false);
        }

        private IMember getMember(ISelection selection) {
            if (selection.isEmpty()) return null;
            Assert.isTrue(selection instanceof StructuredSelection);
            StructuredSelection structuredSelection = (StructuredSelection) selection;
            Assert.isTrue(structuredSelection.getFirstElement() instanceof IMember);
            return (IMember) structuredSelection.getFirstElement();
        }
    }

    private class TestOutlineContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Object[] getChildren(Object parentElement) {
            List children = null;
            if (parentElement instanceof List) {
                children = (List) parentElement;
            } else if (parentElement instanceof TestClass) {
                try {
                    children = Arrays.asList(((TestClass) parentElement).getChildren());
                } catch (ModelException e) {
                    Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
                }
            } else if (parentElement instanceof IMethod) {
                children = new ArrayList();
            }
            Assert.isNotNull(children);
            return children.toArray();
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IMember) return ((IMember) element).getParent();
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            try {
                if (element instanceof IMember) return ((IMember) element).hasChildren();
            } catch (ModelException e) {
                Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
            }
            return false;
        }

        @Override
        public void dispose() {}

        @Override
        public void inputChanged(Viewer viewer,
                                 Object oldInput,
                                 Object newInput) {}
    }
}
