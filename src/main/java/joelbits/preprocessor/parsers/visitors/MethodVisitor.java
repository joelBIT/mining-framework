package joelbits.preprocessor.parsers.visitors;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos;
import joelbits.model.ast.protobuf.ASTProtos.Method;
import joelbits.model.ast.protobuf.ASTProtos.Variable;
import joelbits.model.ast.protobuf.ASTProtos.Expression;
import joelbits.model.ast.protobuf.ASTProtos.Expression.ExpressionType;
import joelbits.preprocessor.parsers.utils.ASTNodeCreator;
import joelbits.preprocessor.utils.TypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor parsing data from the loaded class on a method level.
 */
public class MethodVisitor extends VoidVisitorAdapter<List<Method>> {
    @Override
    public void visit(MethodDeclaration method, List<Method> methods) {
        List<ASTProtos.Modifier> methodModifiers = new ArrayList<>();

        for (Modifier modifier : method.getModifiers()) {
            ASTProtos.Modifier visibility = ASTNodeCreator.createModifier(modifier.name());
            methodModifiers.add(visibility);
        }

        for (AnnotationExpr annotationExpr : method.getAnnotations()) {
            List<String> annotationMembers = TypeConverter.convertAnnotationMembers(annotationExpr);
            methodModifiers.add(ASTNodeCreator.createAnnotationModifier(annotationExpr, annotationMembers));
        }

        List<Variable> arguments = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            List<ASTProtos.Modifier> argumentModifiers = TypeConverter.convertModifiers(parameter.getModifiers());
            arguments.add(ASTNodeCreator.createVariable(parameter.getNameAsString(), parameter.getType().asString(), argumentModifiers));
        }

        List<Expression> methodCalls = new ArrayList<>();
        method.accept(new MethodCallVisitor(), methodCalls);

        List<Expression> methodBodyStatements = new ArrayList<>();
        methodBodyStatements.add(ASTNodeCreator.createExpressionExpressions(methodCalls, ExpressionType.METHODCALL));
        methods.add(ASTNodeCreator.createMethod(methodModifiers, method, arguments, methodBodyStatements));
    }
}