package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 * A type declaration, such as a class or interface.
 */
public class Declaration implements ASTNode {
    private String name;                                    // The name of the declaration
    private List<Declaration> nestedDeclarations;           // Any nested declarations
    private List<Modifier> modifiers;                       // The modifiers/annotations on this declaration
    private DeclarationType type;                           // The type of this declaration
    private List<Variable> fields;                          // The fields in the declaration
    private List<Method> methods;                           // The methods in the declaration
    private List<Declaration> parents;                      // The explicitly named parent type(s) of this declaration

    @Override
    public void accept(Visitor visitor) {

    }
}
