package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 * A variable declaration (field, local, parameter, etc).
 */
public class Variable implements ASTNode {
    private String name;                        // The name of the variable
    private DeclarationType type;               // The type of the variable
    private Expression initializer;             // If the variable has an initial assignment, the expression is stored here
    private List<Modifier> modifiers;           // A list of all modifiers on the variable

    @Override
    public void accept(Visitor visitor) {

    }
}
