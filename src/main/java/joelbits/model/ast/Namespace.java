package joelbits.model.ast;

import joelbits.visitors.Visitor;

import java.util.List;

/**
 * A namespace (aka, package) in a source file.
 */
public class Namespace implements ASTNode {
    private String name;                        // The name of the namespace
    private List<Declaration> declarations;     // Declarations contained in this namespace
    private List<Modifier> modifiers;           // Any modifiers/annotations on the namespace

    @Override
    public void accept(Visitor visitor) {

    }
}
