package joelbits.model.ast;

import joelbits.model.ast.types.ExpressionType;
import joelbits.analysis.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 *  A construct made up of variables, operators, and method invocations, which are
 *  constructed according to the syntax of the language, that evaluates to a single value.
 */
public final class Expression implements ASTNode {
    private final ExpressionType type;
    private final String literal;               // Syntactic representations of boolean, character, numeric, or string data.
    private final String method;
    private final String variable;
    private final List<Expression> methodArguments;
    private final List<Variable> variableDeclarations;

    public Expression(ExpressionType type, String literal, String method, String variable, List<Expression> methodArguments, List<Variable> variableDeclarations) {
        this.type = type;
        this.literal = literal;
        this.method = method;
        this.variable = variable;
        this.methodArguments = new ArrayList<>(methodArguments);
        this.variableDeclarations = new ArrayList<>(variableDeclarations);
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
            for (Expression argument : methodArguments) {
                if (!argument.accept(visitor)) {
                    break;
                }
            }
            for (Variable declaration : variableDeclarations) {
                if (!declaration.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visitLeave(this);
    }

    public ExpressionType getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    public String getMethod() {
        return method;
    }

    public String getVariable() {
        return variable;
    }

    public List<Expression> getMethodArguments() {
        return methodArguments;
    }

    public List<Variable> getVariableDeclarations() {
        return variableDeclarations;
    }
}
