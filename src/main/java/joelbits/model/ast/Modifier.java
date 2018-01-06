package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.model.ast.types.ModifierType;
import joelbits.model.ast.types.VisibilityType;
import joelbits.visitors.Visitor;

import java.util.List;

/**
 * A single modifier.
 */
public class Modifier implements ASTNode {
    private String name;                    // If the DeclarationType is ANNOTATION, then the name of the annotation
    private ModifierType type;              // The type of modifier
    private List<String> members;           // If the DeclarationType is ANNOTATION, then a list of all members explicitly assigned values
    private List<Expression> values;        // If the DeclarationType is ANNOTATION, then a list of all values that were assigned to members
    private VisibilityType visibility;      // A type of visibility modifier
    private DeclarationType other;          // If the DeclarationType is OTHER, the modifier string from the source code

    @Override
    public void accept(Visitor visitor) {

    }
}
