package joelbits.model.ast;

import joelbits.visitors.Visitor;

/**
 * Any time a call to accept answers false, it signals the parent node's accept method to stop
 * processing children at that level in the tree.
 */
public interface ASTNode {
    boolean accept(Visitor visitor);
}
