package joelbits.model.ast;

import joelbits.model.ast.types.ModifierType;
import joelbits.model.ast.types.VisibilityType;
import joelbits.analysisExecuter.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A single modifier.
 */
public final class Modifier implements ASTNode {
    private final String name;                    // If the DeclarationType is ANNOTATION, then the name of the annotation
    private final ModifierType type;              // The type of modifier
    private final List<String> members;           // If the DeclarationType is ANNOTATION, then a list of all members explicitly assigned values
    private final List<Expression> values;        // If the DeclarationType is ANNOTATION, then a list of all values that were assigned to members
    private final VisibilityType visibility;      // A type of visibility modifier
    private final String other;                   // If the DeclarationType is OTHER, the modifier string from the source code

    public Modifier(String name, ModifierType type, List<String> members, List<Expression> values, VisibilityType visibility, String other) {
        this.name = name;
        this.type = type;
        this.members = new ArrayList<>(members);
        this.values = new ArrayList<>(values);
        this.visibility = visibility;
        this.other = other;
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
            for (Expression expression : values) {
                if (!expression.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visitLeave(this);
    }

    public String getName() {
        return name;
    }

    public ModifierType getType() {
        return type;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<Expression> getValues() {
        return values;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public String getOther() {
        return other;
    }
}
