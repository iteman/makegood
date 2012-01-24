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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.IModelElementVisitorExtension;
import org.eclipse.dltk.core.IOpenable;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptModel;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ITypeHierarchy;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.WorkingCopyOwner;

import com.piece_framework.makegood.core.MakeGoodProperty;
import com.piece_framework.makegood.core.PHPResource;
import com.piece_framework.makegood.core.TestingFramework;

/**
 * @since 1.x.0
 */
public class TestClass implements IType {
    private IType type;
    private IType baseType;

    public TestClass(IType type) {
        this.type = type;
        while (this.type instanceof TestClass) {
            this.type = ((TestClass) this.type).type;
        }
    }

    @Override
    public IPath getPath() {
        return type.getPath();
    }

    @Override
    public ISourceRange getNameRange() throws ModelException {
        return type.getNameRange();
    }

    @Override
    public int getFlags() throws ModelException {
        return type.getFlags();
    }

    @Override
    public IType getDeclaringType() {
        return createTestClass(type.getDeclaringType());
    }

    @Override
    public ISourceModule getSourceModule() {
        return type.getSourceModule();
    }

    @Override
    public IType getType(String name, int occurrenceCount) {
        return createTestClass(type.getType(name, occurrenceCount));
    }

    @Override
    public int getElementType() {
        return type.getElementType();
    }

    @Override
    public String getElementName() {
        return type.getElementName();
    }

    @Override
    public IModelElement getParent() {
        return type.getParent();
    }

    @Override
    public boolean isReadOnly() {
        return type.isReadOnly();
    }

    @Override
    public IResource getResource() {
        return type.getResource();
    }

    @Override
    public boolean exists() {
        return type.exists();
    }

    @Override
    public IModelElement getAncestor(int ancestorType) {
        return type.getAncestor(ancestorType);
    }

    @Override
    public IOpenable getOpenable() {
        return type.getOpenable();
    }

    @Override
    public IScriptModel getModel() {
        return type.getModel();
    }

    @Override
    public IScriptProject getScriptProject() {
        return type.getScriptProject();
    }

    @Override
    public IResource getUnderlyingResource() throws ModelException {
        return type.getUnderlyingResource();
    }

    @Override
    public IResource getCorrespondingResource() throws ModelException {
        return type.getCorrespondingResource();
    }

    @Override
    public IModelElement getPrimaryElement() {
        return type.getPrimaryElement();
    }

    @Override
    public String getHandleIdentifier() {
        return type.getHandleIdentifier();
    }

    @Override
    public boolean isStructureKnown() throws ModelException {
        return type.isStructureKnown();
    }

    @Override
    public void accept(IModelElementVisitor visitor) throws ModelException {
        if (visitor.visit(this)) {
            IModelElement[] elements = getChildren();
            for (int i = 0; i < elements.length; ++i) {
                elements[i].accept(visitor);
            }
            if (visitor instanceof IModelElementVisitorExtension) {
                ((IModelElementVisitorExtension) visitor).endVisit(this);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdapter(Class adapter) {
        return type.getAdapter(adapter);
    }

    @Override
    public ISourceRange getSourceRange() throws ModelException {
        return type.getSourceRange();
    }

    @Override
    public String getSource() throws ModelException {
        return type.getSource();
    }

    @Override
    public IModelElement[] getChildren() throws ModelException {
        List<IModelElement> children = new ArrayList<IModelElement>();

        if (getFlags() != Modifiers.AccNameSpace) {
            children.addAll(Arrays.asList(getMethods()));

            ITypeHierarchy hierarchy = newTypeHierarchy(new NullProgressMonitor());
            for (IType supertype: hierarchy.getSupertypes(type)) {
                if (!TestClass.isTestClassSupperType(supertype)) {
                    children.add(createTestClass(supertype));
                }
            }
        } else {
            for (IType type: getTypes()) {
                if (isTestClass(type)) {
                    children.add(createTestClass(type));
                }
            }
        }

        return children.toArray(new IModelElement[0]);
    }

    @Override
    public boolean hasChildren() throws ModelException {
        return getChildren().length > 0;
    }

    @Override
    public String[] getSuperClasses() throws ModelException {
        return type.getSuperClasses();
    }

    @Override
    public IField getField(String name) {
        return type.getField(name);
    }

    @Override
    public IField[] getFields() throws ModelException {
        return type.getFields();
    }

    @Override
    public IType getType(String name) {
        return createTestClass(type.getType(name));
    }

    @Override
    public IType[] getTypes() throws ModelException {
        List<IType> types= new ArrayList<IType>();
        for (IType type: this.type.getTypes()) {
            types.add(createTestClass(type));
        }
        return types.toArray(new IType[0]);
    }

    @Override
    public IMethod getMethod(String name) {
        return createTestMethod(type.getMethod(name));
    }

    @Override
    public IMethod[] getMethods() throws ModelException {
        List<IMethod> methods = new ArrayList<IMethod>();
        for (IMethod method: type.getMethods()) {
            if (PHPResource.isTestMethod(method)) methods.add(createTestMethod(method));
        }
        return methods.toArray(new IMethod[0]);
    }

    @Override
    @SuppressWarnings(value = "deprecation")
    public String getFullyQualifiedName(String enclosingTypeSeparator) {
        return type.getFullyQualifiedName(enclosingTypeSeparator);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getFullyQualifiedName() {
        return type.getFullyQualifiedName();
    }

    @Override
    public void codeComplete(char[] snippet,
                             int insertion,
                             int position,
                             char[][] localVariableTypeNames,
                             char[][] localVariableNames,
                             int[] localVariableModifiers,
                             boolean isStatic,
                             CompletionRequestor requestor) throws ModelException {
        type.codeComplete(
            snippet,
            insertion,
            position,
            localVariableTypeNames,
            localVariableNames,
            localVariableModifiers,
            isStatic,
            requestor);
    }

    @Override
    public void codeComplete(char[] snippet,
                             int insertion,
                             int position,
                             char[][] localVariableTypeNames,
                             char[][] localVariableNames,
                             int[] localVariableModifiers,
                             boolean isStatic,
                             CompletionRequestor requestor,
                             WorkingCopyOwner owner) throws ModelException {
        type.codeComplete(
            snippet,
            insertion,
            position,
            localVariableTypeNames,
            localVariableNames,
            localVariableModifiers,
            isStatic,
            requestor,
            owner);
    }

    @Override
    public IScriptFolder getScriptFolder() {
        return type.getScriptFolder();
    }

    @Override
    public String getTypeQualifiedName() {
        return type.getTypeQualifiedName();
    }

    @Override
    public String getTypeQualifiedName(String enclosingTypeSeparator) {
        return type.getTypeQualifiedName(enclosingTypeSeparator);
    }

    @Override
    public IMethod[] findMethods(IMethod method) {
        return type.findMethods(method);
    }

    @Override
    public ITypeHierarchy loadTypeHierachy(InputStream input,
                                           IProgressMonitor monitor) throws ModelException {
        return type.loadTypeHierachy(input, monitor);
    }

    @Override
    public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws ModelException {
        return type.newSupertypeHierarchy(monitor);
    }

    @Override
    public ITypeHierarchy newSupertypeHierarchy(ISourceModule[] workingCopies,
                                                IProgressMonitor monitor) throws ModelException {
        return type.newSupertypeHierarchy(workingCopies, monitor);
    }

    @Override
    public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner,
                                                IProgressMonitor monitor) throws ModelException {
        return type.newSupertypeHierarchy(owner, monitor);
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IScriptProject project,
                                           IProgressMonitor monitor) throws ModelException {
        return type.newTypeHierarchy(project, monitor);
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IScriptProject project,
                                           WorkingCopyOwner owner,
                                           IProgressMonitor monitor) throws ModelException {
        return type.newTypeHierarchy(project, owner, monitor);
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws ModelException {
        return type.newTypeHierarchy(monitor);
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(ISourceModule[] workingCopies,
                                           IProgressMonitor monitor) throws ModelException {
        return type.newTypeHierarchy(workingCopies, monitor);
    }

    @Override
    public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner,
                                           IProgressMonitor monitor) throws ModelException {
        return type.newSupertypeHierarchy(owner, monitor);
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

    public static boolean isTestClass(IType type) {
        if (type == null || type.getResource() == null) return false;
        try {
            if (type.getFlags() != Modifiers.AccNameSpace) {
                return new MakeGoodProperty(type.getResource()).getTestingFramework().hasTests(type.getSourceModule());
            } else {
                for (IType child: type.getTypes()) {
                    if (isTestClass(child)) return true;
                }
            }
        } catch (ModelException e) {}
        return false;
    }

    public void setBaseType(IType baseType) {
        this.baseType = baseType;
    }

    public boolean isSubtype(IType targetSuperType) throws ModelException {
        ITypeHierarchy hierarchy = newSupertypeHierarchy(new NullProgressMonitor());
        for (IType superType: hierarchy.getAllSuperclasses(type)) {
            if (superType.getElementName().equals(targetSuperType.getElementName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isNamespace() {
        try {
            return getFlags() == Modifiers.AccNameSpace;
        } catch (ModelException e) {
            return false;
        }
    }

    private TestClass createTestClass(IType type) {
        if (type == null) return null;
        TestClass testClass = new TestClass(type);
        if (baseType != null) testClass.setBaseType(baseType);
        return testClass;
    }

    private TestMethod createTestMethod(IMethod method) {
        TestMethod testMethod = new TestMethod(method);
        if (baseType != null) testMethod.setBaseType(baseType);
        return testMethod;
    }
}
