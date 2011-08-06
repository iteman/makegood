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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.indexing.IndexDocument;
import org.eclipse.dltk.core.search.indexing.core.AbstractProjectIndexer;

public class ProjectIndexer extends AbstractProjectIndexer {

    public ProjectIndexer() {
    }

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

        IType[] types;
        try {
            types = document.getSourceModule().getAllTypes();
        } catch (ModelException e) {
            Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
            return;
        }

        for (IType type: types) {
            try {
                if (TestClass.isTestClassSupperType(type)) {
                    collectTestClass(type);
                }

                if (TestClass.isTestClass(type)) {
                    MakeGoodContext.getInstance().getTestClassCollector().add(type);
                } else {
                    MakeGoodContext.getInstance().getTestClassCollector().remove(type);
                }
            } catch (ModelException e) {
                Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
            }
        }
    }

    private void collectTestClass(IType type) throws ModelException {
        ITypeHierarchy hierarchy = type.newTypeHierarchy(new NullProgressMonitor());
        for (IType subtype: hierarchy.getAllSubtypes(type)) {
            boolean isExternal = subtype.getResource() == null;
            if (isExternal)  continue;

            MakeGoodContext.getInstance().getTestClassCollector().add(subtype);
            collectTestClass(subtype);
        }
    }
}
