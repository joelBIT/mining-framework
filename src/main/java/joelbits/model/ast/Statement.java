package joelbits.model.ast;

import joelbits.model.ast.types.StatementType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 * roughly equivalent to sentences in natural languages. A statement forms a complete unit of execution.
 */
public class Statement implements ASTNode {
    private StatementType statement;
    private List<Statement> statements;
    private Expression expression;
    private List<Expression> expressions;
    private Expression condition;
    private List<Expression> initializations;
    private Variable variableDeclaration;
    private List<Expression> updates;               // Expressions that are updated in the statement

    @Override
    public void accept(Visitor visitor) {

    }
}
