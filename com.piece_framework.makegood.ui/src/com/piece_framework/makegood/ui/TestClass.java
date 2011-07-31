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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.core.IType;

import com.piece_framework.makegood.core.MakeGoodProperty;
import com.piece_framework.makegood.core.TestingFramework;

public class TestClass {

    private IType type;

    public TestClass(IType type) {
        this.type = type;
    }

    public IPath getPath() {
        return type.getPath();
    }

    public IType getType() {
        return type;
    }

    public static boolean isTestClassSupperType(IType type) {
        List<String> testClassSuperTypes = new ArrayList<String>();
        if (type != null && type.getResource() != null) {
            testClassSuperTypes.addAll(
                Arrays.asList(new MakeGoodProperty(type.getResource()).getTestingFramework().getTestClassSuperTypes()));
        } else {
            testClassSuperTypes.addAll(Arrays.asList(TestingFramework.PHPUnit.getTestClassSuperTypes()));
            testClassSuperTypes.addAll(Arrays.asList(TestingFramework.SimpleTest.getTestClassSuperTypes()));
            testClassSuperTypes.addAll(Arrays.asList(TestingFramework.CakePHP.getTestClassSuperTypes()));
            testClassSuperTypes.addAll(Arrays.asList(TestingFramework.CIUnit.getTestClassSuperTypes()));
        }

        for (String testClassSuperType: testClassSuperTypes) {
            if (type.getElementName().equals(testClassSuperType)) return true;
        }
        return false;
    }
}
