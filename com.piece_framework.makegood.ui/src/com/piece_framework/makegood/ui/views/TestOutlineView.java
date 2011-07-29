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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.viewsupport.DecoratingModelLabelProvider;
import org.eclipse.dltk.ui.viewsupport.ScriptUILabelProvider;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.piece_framework.makegood.core.PHPResource;
import com.piece_framework.makegood.ui.Activator;
import com.piece_framework.makegood.ui.EditorParser;
import com.piece_framework.makegood.ui.ide.ActiveEditor;

/**
 * @since 1.x.0
 */
public class TestOutlineView extends ViewPart {
    public static final String ID = "com.piece_framework.makegood.ui.views.testOutlineView"; //$NON-NLS-1$

    private TreeViewer viewer;
    private TreeSelectionChangedListener selectionChangedListener =
        new TreeSelectionChangedListener();

    public TestOutlineView() {}

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        viewer = new TreeViewer(parent);
        viewer.setContentProvider(new TestOutlineContentProvider());
        viewer.setLabelProvider(new DecoratingModelLabelProvider(new ScriptUILabelProvider()));

        MenuManager contextMenuManager = new MenuManager();
        contextMenuManager.setRemoveAllWhenShown(true);
        Menu contextMenu = contextMenuManager.createContextMenu(viewer.getTree());
        viewer.getTree().setMenu(contextMenu);

        getSite().registerContextMenu(contextMenuManager, viewer);
        getSite().setSelectionProvider(viewer);

        setViewerInput();
        setViewerSelection();

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                if (event.getSelection().isEmpty()) return;
                Assert.isTrue(event.getSelection() instanceof StructuredSelection);
                StructuredSelection selection = (StructuredSelection) event.getSelection();

                showEditor(selection, true);
            }
        });
    }

    @Override
    public void setFocus() {}

    /**
     * @since 1.x.0
     */
    public void setViewerInput() {
        for (Job job: Job.getJobManager().find(null)) {
            if (job.getName().startsWith("DLTK indexing")) {
                job.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done(IJobChangeEvent event) {
                        TestOutlineUpdateJob.update();
                    }
                });
                return;
            }
        }

        if (!ActiveEditor.isPHP()) return;

        EditorParser parser = EditorParser.createActiveEditorParser();
        List<IType> testClasses = PHPResource.getTestClasses(parser.getSourceModule());
        if (testClasses.size() == 0) return;

        if (viewer == null) return;
        if (viewer.getContentProvider() == null) return;
        viewer.setInput(testClasses);
        viewer.expandAll();
    }

    /**
     * @since 1.x.0
     */
    public void resetViewerInput() {
        if (viewer == null) return;
        if (viewer.getContentProvider() == null) return;
        viewer.setInput(null);
    }

    /**
     * @since 1.x.0
     */
    public void setViewerSelection() {
        if (!ActiveEditor.isPHP()) return;
        if (!hasContent()) return;

        EditorParser parser = EditorParser.createActiveEditorParser();

        // To empty the selection object at the selection event, the listener is temporarily deleted.
        viewer.removeSelectionChangedListener(selectionChangedListener);
        if (parser.getModelElementOnSelection() != null)
            viewer.setSelection(new StructuredSelection(parser.getModelElementOnSelection()));
        viewer.addSelectionChangedListener(selectionChangedListener);
    }

    /**
     * @since 1.x.0
     */
    public boolean hasContent() {
        return viewer != null && viewer.getInput() != null;
    }

    private void showEditor(StructuredSelection selection, Boolean openWhenClosed) {
        Assert.isTrue(selection.getFirstElement() instanceof IMember);
        IMember member = (IMember) selection.getFirstElement();
        if (member.getSourceModule() == null) return;

        IEditorPart target = findTargetEditor(member);

        ISourceRange nameRange = null;
        try {
            nameRange = member.getNameRange();
        } catch (ModelException e) {
            Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
        }
        if (nameRange == null) return;

        boolean editorIsOpened = target != null;
        if (editorIsOpened) {
            IWorkbenchPage page = target.getSite().getPage();
            if (page.getActiveEditor() != target) page.activate(target);

            ((ITextEditor) target).selectAndReveal(
                nameRange.getOffset(),
                nameRange.getLength());
        } else if (!editorIsOpened && openWhenClosed) {
            EditorOpener.open(
                (IFile) member.getResource(),
                nameRange.getOffset(),
                nameRange.getLength());
        }
    }

    private IEditorPart findTargetEditor(IMember member) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) return null;
        IWorkbenchPage page = window.getActivePage();
        if (page == null) return null;

        IEditorPart target = null;
        for (IEditorReference reference: page.getEditorReferences()) {
            IEditorPart editor = reference.getEditor(true);
            EditorParser parser = new EditorParser(editor);
            if (member.getSourceModule().equals(parser.getSourceModule())) {
                target = editor;
                break;
            }
        }
        return target;
    }

    private class TreeSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (event.getSelection().isEmpty()) return;
            Assert.isTrue(event.getSelection() instanceof StructuredSelection);
            StructuredSelection selection = (StructuredSelection) event.getSelection();

            showEditor(selection, false);
        }
    }

    private class TestOutlineContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object[] getChildren(Object parentElement) {
            List children = null;
            if (parentElement instanceof List) {
                children = (List) parentElement;
            } else if (parentElement instanceof IType) {
                try {
                    children = new ArrayList<IMethod>();
                    for (IMethod method : ((IType) parentElement).getMethods()) {
                        if (PHPResource.isTestMethod(method)) children.add(method);
                    }
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
            return (element != null && element instanceof IMember) ?
                   ((IMember) element).getParent() :
                   null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (!(element instanceof IType)) return false;
            IMethod[] methods = null;;
            try {
                methods = ((IType) element).getMethods();
            } catch (ModelException e) {
                Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
            }
            return methods.length > 0;
        }

        @Override
        public void dispose() {}

        @Override
        public void inputChanged(Viewer viewer,
                                 Object oldInput,
                                 Object newInput) {}
    }
}
