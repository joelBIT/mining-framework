package joelbits.model.ast;

import joelbits.analysis.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A namespace (aka, package) in a source file.
 */
public final class Namespace implements ASTNode {
    private final String name;                        // The name of the namespace
    private final List<Declaration> declarations;     // Declarations contained in this namespace
    private final List<Modifier> modifiers;           // Any modifiers/annotations on the namespace

    public Namespace(String name, List<Declaration> declarations, List<Modifier> modifiers) {
        this.name = name;
        this.declarations = new ArrayList<>(declarations);
        this.modifiers = new ArrayList<>(modifiers);
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
            for (Declaration declaration : declarations) {
                if (!declaration.accept(visitor)) {
                    break;
                }
            }
            for (Modifier modifier : modifiers) {
                if (!modifier.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visitLeave(this);
    }

    public String getName() {
        return name;
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }
}
