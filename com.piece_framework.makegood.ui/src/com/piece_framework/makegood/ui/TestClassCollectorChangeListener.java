/**
 * Copyright (c) 2011 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.ui;

import org.eclipse.dltk.core.IType;

/**
 * @since 1.x.0
 */
public interface TestClassCollectorChangeListener {
    public void collectorChanged(IType type);
}
