/**
 * Copyright (c) 2011 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 *
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.indexing.IndexDocument;
import org.eclipse.dltk.core.search.indexing.core.AbstractProjectIndexer;

public class ProjectIndexer extends AbstractProjectIndexer {
    public ProjectIndexer() {}

    @Override
    public void doIndexing(IndexDocument document) {
        if (document.isExternal()) return;

        boolean includeTest = false;
        try {
            IType[] types = document.getSourceModule().getAllTypes();
            for (IType type: types) {
                ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
                for (IType superType: hierarchy.getAllSupertypes(type)) {
                    if (superType.getTypeQualifiedName().equals("PHPUnit_Framework_TestCase")) {
                        includeTest = true;
                        break;
                    }
                }
                if (includeTest) break;
            }
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (includeTest) {
            System.out.println(document.getPath() + " includes the test class.");
        }
    }
}
