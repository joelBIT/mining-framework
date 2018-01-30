package joelbits.model.ast;

import joelbits.model.ast.types.ModifierType;
import joelbits.model.ast.types.VisibilityType;
import joelbits.analysis.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A single modifier.
 */
public final class Modifier implements ASTNode {
    private final String name;                    // If the DeclarationType is ANNOTATION, then the name of the annotation
    private final ModifierType type;              // The type of modifier
    private final List<String> membersAndValues;  // If the DeclarationType is ANNOTATION, then a list of all members and their assigned values, if any
    private final VisibilityType visibility;      // A type of visibility modifier
    private final String other;                   // If the DeclarationType is OTHER, the modifier string from the source code

    public Modifier(String name, ModifierType type, List<String> membersAndValues, VisibilityType visibility, String other) {
        this.name = name;
        this.type = type;
        this.membersAndValues = new ArrayList<>(membersAndValues);
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
        return visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public ModifierType getType() {
        return type;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public String getOther() {
        return other;
    }

    public List<String> getMembersAndValues() {
        return membersAndValues;
    }
}
