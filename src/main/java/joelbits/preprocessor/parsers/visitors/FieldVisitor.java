package joelbits.preprocessor.parsers.visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos.Modifier;
import joelbits.model.ast.protobuf.ASTProtos.Variable;
import joelbits.model.ast.protobuf.ASTProtos.Expression;
import joelbits.preprocessor.parsers.utils.ASTNodeCreater;
import joelbits.preprocessor.parsers.utils.TypeConverter;

import java.util.List;
import java.util.Map;

public class FieldVisitor extends VoidVisitorAdapter<List<Variable>> {
    @Override
    public void visit(FieldDeclaration field, List<Variable> fieldsInDeclaration) {
        List<Modifier> modifiers = TypeConverter.convertModifiers(field.getModifiers());

        for (AnnotationExpr annotationExpr : field.getAnnotations()) {
            Map<String, Expression> annotationMembers = TypeConverter.convertAnnotationMembers(annotationExpr);
            modifiers.add(ASTNodeCreater.createAnnotationModifier(annotationExpr, annotationMembers));
        }

        for (VariableDeclarator test : field.getVariables()) {
            fieldsInDeclaration.add(ASTNodeCreater.createVariable(test.getName().asString(), field.getElementType().asString(), modifiers));
        }
    }
}