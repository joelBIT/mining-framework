package joelbits.model.ast;

import joelbits.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class that holds a file's parsed AST.
 */
public final class ASTRoot implements ASTNode {
    private final List<String> imports;           // The imported namespaces and types
    private final List<Namespace> namespaces;     // The top-level namespaces in the file

    public ASTRoot(List<String> imports, List<Namespace> namespaces) {
        this.imports = new ArrayList<>(imports);
        this.namespaces = new ArrayList<>(namespaces);
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
            for (Namespace namespace : namespaces) {
                if (!namespace.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visitLeave(this);
    }

    public List<String> getImports() {
        return imports;
    }

    public List<Namespace> getNamespaces() {
        return namespaces;
    }
}
