package joelbits.model.ast;

import joelbits.model.ast.types.ExpressionType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 *  A construct made up of variables, operators, and method invocations, which are
 *  constructed according to the syntax of the language, that evaluates to a single value.
 */
public class Expression implements ASTNode {
    private ExpressionType type;
    private List<Expression> expressions;
    private String literal;                     // Syntactic representations of boolean, character, numeric, or string data.
    private String method;
    private String variable;
    private List<Expression> methodArguments;
    private List<Variable> variableDeclarations;

    @Override
    public void accept(Visitor visitor) {

    }
}
