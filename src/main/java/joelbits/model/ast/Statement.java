package joelbits.model.ast;

import joelbits.model.ast.types.StatementType;
import joelbits.analysisExecuter.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * roughly equivalent to sentences in natural languages. A statement forms a complete unit of execution.
 */
public final class Statement implements ASTNode {
    private final StatementType type;                     // Based on this type, different attributes in the record will be set.
    private final List<Statement> statements;
    private final Expression expression;
    private final List<Expression> expressions;
    private final Expression condition;
    private final List<Expression> initializations;
    private final Variable variableDeclaration;
    private final List<Expression> updates;               // Expressions that are updated in the statement

    public Statement(StatementType type, List<Statement> statements, Expression expression, List<Expression> expressions, Expression condition, List<Expression> initializations, Variable variableDeclaration, List<Expression> updates) {
        this.type = type;
        this.statements = new ArrayList<>(statements);
        this.expression = expression;
        this.expressions = new ArrayList<>(expressions);
        this.condition = condition;
        this.initializations = new ArrayList<>(initializations);
        this.variableDeclaration = variableDeclaration;
        this.updates = new ArrayList<>(updates);
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
            for (Statement statement : statements) {
                if (!statement.accept(visitor)) {
                    break;
                }
            }
            for (Expression exp : expressions) {
                if (!exp.accept(visitor)) {
                    break;
                }
            }
            for (Expression initialization : initializations) {
                if (!initialization.accept(visitor)) {
                    break;
                }
            }
            for (Expression update : updates) {
                if (!update.accept(visitor)) {
                    break;
                }
            }
            variableDeclaration.accept(visitor);
            condition.accept(visitor);
            expression.accept(visitor);
        }

        return visitor.visitLeave(this);
    }

    public StatementType getType() {
        return type;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Expression> getInitializations() {
        return initializations;
    }

    public Variable getVariableDeclaration() {
        return variableDeclaration;
    }

    public List<Expression> getUpdates() {
        return updates;
    }
}
