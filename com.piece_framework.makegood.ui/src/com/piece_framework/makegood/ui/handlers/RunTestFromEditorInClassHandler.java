/**
 * Copyright (c) 2009-2010 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 *               2011 KUBO Atsuhiro <kubo@iteman.jp>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.ui.handlers.HandlerUtil;

import com.piece_framework.makegood.ui.EditorParser;

public class RunTestFromEditorInClassHandler extends RunTestFromEditorHandlerInContext {
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        getTestRunner().runTestsInClass(HandlerUtil.getActiveEditor(event));
        return null;
    }

    @Override
    public boolean isEnabled() {
        if (!super.isEnabled()) return false;

        IModelElement element = EditorParser.createActiveEditorParser().getModelElementOnSelection();
        return element.getElementType() == IModelElement.TYPE
            || element.getElementType() == IModelElement.METHOD
            || element.getElementType() == IModelElement.FIELD;
    }
}
