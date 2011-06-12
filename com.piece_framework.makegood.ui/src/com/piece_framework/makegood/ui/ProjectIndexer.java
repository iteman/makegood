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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.indexing.IndexDocument;
import org.eclipse.dltk.core.search.indexing.core.AbstractProjectIndexer;

public class ProjectIndexer extends AbstractProjectIndexer {
    public ProjectIndexer() {}

    @Override
    public void doIndexing(final IndexDocument document) {
        for (Job job: Job.getJobManager().find(null)) {
            if (job.getName().startsWith("DLTK indexing")) {
                job.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done(IJobChangeEvent event) {
                        ProjectIndexer.this.doIndexing(document);
                    }
                });
                return;
            }
        }

        if (document.isExternal()) return;

        boolean isTestClass = false;
        try {
            IType[] types = document.getSourceModule().getAllTypes();
            for (IType type: types) {
                if (isTestClass(type)) {
                    isTestClass = true;
                    break;
                }
            }
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (isTestClass) {
            System.out.println(document.getPath() + " includes the test class.");
        }
    }

    private boolean isTestClass(IType type) throws ModelException {
        if (type.getSuperClasses() == null) return false;

        for (String superClassName: type.getSuperClasses()) {
            if (superClassName.equals("PHPUnit_Framework_TestCase")) return true;
        }

        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
        for (IType superType: hierarchy.getAllSupertypes(type)) {
            if (isTestClass(superType)) return true;
        }
        return false;
    }
}
