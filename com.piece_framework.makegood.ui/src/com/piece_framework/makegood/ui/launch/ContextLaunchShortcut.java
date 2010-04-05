package com.piece_framework.makegood.ui.launch;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.ui.IEditorPart;

public class ContextLaunchShortcut extends NoSearchLaunchShortcut {
    @Override
    protected IModelElement getTargetElement(IEditorPart editor) {
        EditorParser parser = new EditorParser(editor);
        IModelElement element = parser.getModelElementOnSelection();
        if (element == null) {
            return parser.getSourceModule();
        }
        if (element.getElementType() == IModelElement.FIELD) {
            element = element.getParent();
        }
        return element;
    }
}
