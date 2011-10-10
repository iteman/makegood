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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;

import com.piece_framework.makegood.core.MakeGoodProperty;

public class TestClassCollector {
    private Map<String, TestClass> testClasses;
    private List<TestClassCollectorChangeListener> collectorChangeListeners = new ArrayList<TestClassCollectorChangeListener>();

    public TestClassCollector() {
        testClasses = Collections.synchronizedMap(new HashMap<String, TestClass>());
    }

    public void add(IType type) {
        TestClass testClass = new TestClass(type);
        testClasses.put(getKey(type), testClass);

        notify(type);
    }

    public void remove(IType type) {
        if (type.getPath() == null) return;
        testClasses.remove(getKey(type));

        notify(type);
    }

    public TestClass get(IType type) {
        return testClasses.get(getKey(type));
    }

    public List<TestClass> getAtSourceModule(ISourceModule sourceModule) {
        List<TestClass> result = new ArrayList<TestClass>();
        if (sourceModule == null) return result;
        if (!(new MakeGoodProperty(sourceModule.getResource())
                .getTestingFramework().hasTests(sourceModule))) return result;

        try {
            for (IType type: sourceModule.getTypes()) {
                TestClass testClass = get(type);
                if (testClass != null) result.add(testClass);
            }
        } catch (ModelException e) {}
        return result;
    }

    public void addCollectorChangeListener(TestClassCollectorChangeListener listener) {
        if (!collectorChangeListeners.contains(listener)) {
            collectorChangeListeners.add(listener);
        }
    }

    public void removeCollectorChangeListener(TestClassCollectorChangeListener listener) {
        collectorChangeListeners.remove(listener);
    }

    private String getKey(IType type) {
        return type.getPath() + "#" + type.getElementName();
    }

    private void notify(IType type) {
        for (TestClassCollectorChangeListener collectorChangeListener: collectorChangeListeners) {
            collectorChangeListener.collectorChanged(type);
        }
    }
}
