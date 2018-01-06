package joelbits.model.ast;

import java.util.List;

/**
 * Container class that holds a file's parsed AST.
 */
public class ASTRootImpl {
    private List<String> imports;           // The imported namespaces and types
    private List<Namespace> namespaces;     // The top-level namespaces in the file
}
