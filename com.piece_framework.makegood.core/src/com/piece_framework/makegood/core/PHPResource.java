/**
 * Copyright (c) 2009-2010 MATSUFUJI Hideharu <matsufuji2008@gmail.com>,
 *               2010-2011 KUBO Atsuhiro <kubo@iteman.jp>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.typeinference.PHPClassType;

public class PHPResource {
    public static String CONTENT_TYPE = "org.eclipse.php.core.phpsource"; //$NON-NLS-1$

    public static boolean isPHPSource(IResource target) {
        if (!(target instanceof IFile)) return false;

        IContentType contentType = Platform.getContentTypeManager().getContentType(CONTENT_TYPE);
        return contentType.isAssociatedWith(target.getName());
    }

    public static boolean hasTests(ISourceModule source) {
        if (source == null) return false;
        IResource resource = source.getResource();
        if (resource == null) return false;
        String[] testClassSuperTypes =
            new MakeGoodProperty(resource).getTestingFramework().getTestClassSuperTypes();

        try {
            for (IType type: source.getAllTypes()) {
                // The PHPFlags class is not used because it fail the weaving.
                int flag = type.getFlags();
                boolean isNotClass = (flag & Modifiers.AccNameSpace) != 0
                                     || (flag & Modifiers.AccInterface) != 0;
                if (isNotClass) continue;
                for (String testClassSuperType: testClassSuperTypes) {
                    if (hasTests(type, testClassSuperType)) {
                        return true;
                    }
                }
            }
        } catch (ModelException e) {
            Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
        }

        return false;
    }

    /**
     * @since 1.4.0
     */
    public static List<IType> getTestClasses(ISourceModule source) {
        List<IType> testClasses = new ArrayList<IType>();
        try {
            for (IType baseType : source.getAllTypes()) {
                testClasses.add(baseType);
            }
        } catch (ModelException e) {
            Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
        }
        return Collections.unmodifiableList(testClasses);
    }

    /**
     * @since 1.2.0
     */
    private static boolean hasTests(IType type, String testClassSuperType) throws ModelException {
        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
        if (hierarchy == null) return false;
        IType[] supertypes = hierarchy.getAllSuperclasses(type);
        if (supertypes == null) return false;
        for (IType supertype: supertypes) {
            if (PHPClassType.fromIType(supertype).getTypeName().equals(testClassSuperType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @since 1.4.0
     */
    public static boolean isTestMethod(IMethod method) throws ModelException {
        if (method == null) return false;
        if (!hasTests(method.getSourceModule())) return false;

        // TODO Add the following check items.
        // * The head of the method name is "test".
        // * There is the @test annotation in PHPDoc of the method.

        int flags = method.getFlags();
        return (flags & Modifiers.AccPublic) > 0
               && (flags & Modifiers.AccStatic) == 0;
    }
}
