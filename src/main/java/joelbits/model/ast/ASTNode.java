package joelbits.model.ast;

import joelbits.visitors.Visitor;

public interface ASTNode {
    void accept(Visitor visitor);
}
