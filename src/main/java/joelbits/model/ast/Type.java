package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.analysis.visitors.Visitor;

/**
 * A type in an AST.
 */
public final class Type implements ASTNode {
    private final String name;
    private final DeclarationType type;

    public Type(String name, DeclarationType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean accept(Visitor visitor) {
        return visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public DeclarationType getType() {
        return type;
    }
}
