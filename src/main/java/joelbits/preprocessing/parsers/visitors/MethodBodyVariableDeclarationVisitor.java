package joelbits.preprocessing.parsers.visitors;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos;
import joelbits.model.ast.protobuf.ASTProtos.Variable;
import joelbits.model.ast.protobuf.ASTProtos.Expression.ExpressionType;
import joelbits.preprocessing.parsers.utils.ASTNodeCreator;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * A visitor for the variable declarations performed inside a specific method.
 */
public class MethodBodyVariableDeclarationVisitor extends VoidVisitorAdapter<List<ASTProtos.Expression>> {
    @Override
    public void visit(VariableDeclarationExpr variableDeclarationExpr, List<ASTProtos.Expression> methodBodyVariableDeclarations) {
        super.visit(variableDeclarationExpr, methodBodyVariableDeclarations);

        List<ASTProtos.Modifier> variableModifiers = new ArrayList<>();
        for (Modifier modifier : variableDeclarationExpr.getModifiers()) {
            variableModifiers.add(ASTNodeCreator.createModifier(modifier.asString()));
        }
        for (VariableDeclarator declaration : variableDeclarationExpr.getVariables()) {
            String name = declaration.getName().asString();
            String type = declaration.getType().asString();
            String assignedValue = StringUtils.EMPTY;

            Optional<Expression> initializer = declaration.getInitializer();
            if (initializer.isPresent()) {
                assignedValue = initializer.get().toString();
            }

            Variable variable = ASTNodeCreator.createVariable(name, type, variableModifiers);
            methodBodyVariableDeclarations.add(ASTNodeCreator.createExpression(ExpressionType.VARIABLE_DECLARATION, assignedValue, name, Collections.singletonList(variable), Collections.emptyList()));
        }
    }
}