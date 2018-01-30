package joelbits.model.ast;

import joelbits.analysis.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A method declaration.
 */
public final class Method implements ASTNode {
    private final String name;                        // The name of the method
    private final List<Variable> arguments;           // The arguments of the method
    private final Type returnType;                    // The type returned from the method; if the method returns nothing, this type will be void
    private final List<Modifier> modifiers;           // A list of all modifiers on the method
    private final List<Expression> bodyContent;       // A list of all expressions within the method body

    public Method(String name, List<Variable> arguments, Type returnType, List<Modifier> modifiers, List<Expression> bodyContent) {
        this.name = name;
        this.arguments = new ArrayList<>(arguments);
        this.returnType = returnType;
        this.modifiers = new ArrayList<>(modifiers);
        this.bodyContent = new ArrayList<>(bodyContent);
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
            for (Variable argument : arguments) {
                if (!argument.accept(visitor)) {
                    break;
                }
            }
            for (Modifier modifier : modifiers) {
                if (!modifier.accept(visitor)) {
                    break;
                }
            }
            returnType.accept(visitor);
        }

        return visitor.visitLeave(this);
    }

    public String getName() {
        return name;
    }

    public List<Variable> getArguments() {
        return arguments;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public List<Expression> getBodyContent() {
        return bodyContent;
    }
}
