package org.palladiosimulator.jdt.core.visitor;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;

public class EnumConstantDeclarationVisitor extends AstVisitor<EnumConstantDeclaration> {

    public EnumConstantDeclarationVisitor() {
        super();
    }

    public EnumConstantDeclarationVisitor(final boolean visitDocTags, final boolean visitChildren) {
        super(visitDocTags, visitChildren);
    }

    @Override
    public boolean visit(final EnumConstantDeclaration node) {
        if (containsVisitedNode(node)) {
            return false;
        }
        addVisitedNode(node);

        return visitChildren;
    }

}
