package joelbits.preprocessing.parsers.visitors;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos;
import joelbits.preprocessing.parsers.utils.ASTNodeCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor parsing data about each method invocation performed inside a specific method.
 */
public class MethodCallVisitor extends VoidVisitorAdapter<List<ASTProtos.Expression>> {
    @Override
    public void visit(MethodCallExpr methodCall, List<ASTProtos.Expression> methodInvocations) {
        super.visit(methodCall, methodInvocations);

        List<ASTProtos.Expression> methodArguments = new ArrayList<>();
        for (Expression parameter : methodCall.getArguments()) {
            methodArguments.add(ASTNodeCreator.createMethodCallArgumentExpression(parameter.toString()));
        }

        methodInvocations.add(ASTNodeCreator.createMethodCallExpression(methodCall, methodArguments));
    }
}