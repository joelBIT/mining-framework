package joelbits.model.ast;

import joelbits.analysis.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A variable declaration (field, local, parameter, etc).
 */
public final class Variable implements ASTNode {
    private final String name;                        // The name of the variable
    private final Type type;                          // The type of the variable
    private final Expression initializer;             // If the variable has an initial assignment, the expression is stored here
    private final List<Modifier> modifiers;           // A list of all modifiers on the variable

    public Variable(String name, Type type, Expression initializer, List<Modifier> modifiers) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
        this.modifiers = new ArrayList<>(modifiers);
    }

    /**
     *  The ASTNode's accept implementation uses the answer from visitEnter to determine whether its children
     *  should accept this visitor. So, if visitEnter answers true, accept is invoked on each of its children
     *  or until one of the accept invocations answers false. Once a parent node has called accept for each of
     *  its children, it will call visitor.visitLeave. This lets the visitor know it is done with this branch
     *  and proceeding to either a sibling or parent ASTNode at the same tree-depth as this node.
     *
     * @param visitor
     * @return          true if proceed with a sibling ASTNode, false if not
     */
    @Override
    public boolean accept(Visitor visitor) {
        if (visitor.visitEnter(this)) {
            for (Modifier modifier : modifiers) {
                if (!modifier.accept(visitor)) {
                    break;
                }
            }
            initializer.accept(visitor);
        }

        return visitor.visitLeave(this);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Expression getInitializer() {
        return initializer;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }
}
