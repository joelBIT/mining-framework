package joelbits.model.ast;

import joelbits.visitors.Visitor;

public interface ASTRoot {
    void accept(Visitor visitor);
}
