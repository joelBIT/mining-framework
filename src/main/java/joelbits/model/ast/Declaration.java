package joelbits.model.ast;

import joelbits.model.ast.types.DeclarationType;
import joelbits.visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

/**
 * A type declaration, such as a class or interface.
 */
public final class Declaration implements ASTNode {
    private final String name;                                    // The name of the declaration
    private final List<Declaration> nestedDeclarations;           // Any nested declarations
    private final List<Modifier> modifiers;                       // The modifiers/annotations on this declaration
    private final DeclarationType type;                           // The type of this declaration
    private final List<Variable> fields;                          // The fields in the declaration
    private final List<Method> methods;                           // The methods in the declaration
    private final List<Type> parents;                             // The explicitly named parent type(s) of this declaration

    public Declaration(String name, List<Declaration> nestedDeclarations, List<Modifier> modifiers, DeclarationType type, List<Variable> fields, List<Method> methods, List<Type> parents) {
        this.name = name;
        this.nestedDeclarations = new ArrayList<>(nestedDeclarations);
        this.modifiers = new ArrayList<>(modifiers);
        this.type = type;
        this.fields = new ArrayList<>(fields);
        this.methods = new ArrayList<>(methods);
        this.parents = new ArrayList<>(parents);
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
            for (Declaration nestedDeclaration : nestedDeclarations) {
                if (!nestedDeclaration.accept(visitor)) {
                    break;
                }
            }
            for (Modifier modifier : modifiers) {
                if (!modifier.accept(visitor)) {
                    break;
                }
            }
            for (Method method : methods) {
                if (!method.accept(visitor)) {
                    break;
                }
            }
            for (Variable variable : fields) {
                if (!variable.accept(visitor)) {
                    break;
                }
            }
            for (Type type : parents) {
                if (!type.accept(visitor)) {
                    break;
                }
            }
        }

        return visitor.visitLeave(this);
    }

    public String getName() {
        return name;
    }

    public List<Declaration> getNestedDeclarations() {
        return nestedDeclarations;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public DeclarationType getType() {
        return type;
    }

    public List<Variable> getFields() {
        return fields;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public List<Type> getParents() {
        return parents;
    }
}
