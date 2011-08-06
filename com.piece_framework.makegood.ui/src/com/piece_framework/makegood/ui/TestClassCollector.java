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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dltk.core.IType;

public class TestClassCollector {
    private Map<String, TestClass> testClasses;

    public TestClassCollector() {
        testClasses = Collections.synchronizedMap(new HashMap<String, TestClass>());
    }

    public void add(IType type) {
        TestClass testClass = new TestClass(type);
        testClasses.put(getKey(type), testClass);
    }

    public void remove(IType type) {
        if (type.getPath() == null) return;
        testClasses.remove(type.getPath());
    }

    public TestClass get(IType type) {
        return testClasses.get(getKey(type));
    }

    public TestClass[] getAll() {
        return testClasses.values().toArray(new TestClass[0]);
    }

    private String getKey(IType type) {
        return type.getPath() + "#" + type.getElementName();
    }
}
