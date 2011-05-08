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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

public class TestOutlineUpdateJob extends UIJob {
    private static final String NAME = "MakeGood Test Outline Update";

    /**
     * @since 1.4.0
     */
    public static void update() {
        if (Job.getJobManager().find(NAME).length > 0) return;

        TestOutlineUpdateJob job = new TestOutlineUpdateJob(NAME);
        job.schedule();
    }

    private TestOutlineUpdateJob(String name) {
        super(name);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        TestOutlineView view = (TestOutlineView) ViewOpener.find(TestOutlineView.ID);
        if (view != null) {
            view.resetViewerInput();
            view.setViewerInput();
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object family) {
        if (!(family instanceof String)) return super.belongsTo(family);

        return getName().equals((String) family);
    }
}
