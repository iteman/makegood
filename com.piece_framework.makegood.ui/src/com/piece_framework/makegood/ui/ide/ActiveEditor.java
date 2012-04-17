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

package com.piece_framework.makegood.ui.ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.piece_framework.makegood.core.PHPResource;

/**
 * @since 1.4.0
 */
public class ActiveEditor {
    /**
     * @since 1.4.0
     */
    public static boolean isPHP() {
        return isPHP(get());
    }

    /**
     * @since 1.4.0
     */
    public static boolean isPHP(IEditorPart editor) {
        if (editor == null) return false;
        if (!(editor.getEditorInput() instanceof IFileEditorInput)) return false;
        IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
        return PHPResource.isPHPSource(file);
    }

    /**
     * @since 1.4.0
     */
    public static IEditorPart get() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) return null;
        IWorkbenchPage page = window.getActivePage();
        if (page == null) return null;
        return page.getActiveEditor();
    }
}
