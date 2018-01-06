package joelbits.visitors;

import joelbits.model.ast.ASTNode;

public interface Visitor {
    void visit(ASTNode node);
}
