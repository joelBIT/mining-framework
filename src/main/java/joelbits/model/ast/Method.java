package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 * A method declaration.
 */
public class Method implements ASTNode {
    private String name;                        // The name of the method
    private List<Variable> arguments;           // The arguments of the method
    private DeclarationType returnType;         // The type returned from the method; if the method returns nothing, this type will be void
    private List<Modifier> modifiers;           // A list of all modifiers on the method

    @Override
    public void accept(Visitor visitor) {

    }
}
