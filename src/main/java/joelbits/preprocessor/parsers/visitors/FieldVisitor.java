package joelbits.preprocessor.parsers.visitors;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos.Modifier;
import joelbits.model.ast.protobuf.ASTProtos.Variable;
import joelbits.preprocessor.parsers.utils.ASTNodeCreator;
import joelbits.preprocessor.utils.TypeConverter;

import java.util.List;

public class FieldVisitor extends VoidVisitorAdapter<List<Variable>> {
    @Override
    public void visit(FieldDeclaration field, List<Variable> fieldsInDeclaration) {
        List<Modifier> modifiers = TypeConverter.convertModifiers(field.getModifiers());

        for (AnnotationExpr annotationExpr : field.getAnnotations()) {
            List<String> annotationMembers = TypeConverter.convertAnnotationMembers(annotationExpr);
            modifiers.add(ASTNodeCreator.createAnnotationModifier(annotationExpr, annotationMembers));
        }

        for (VariableDeclarator test : field.getVariables()) {
            fieldsInDeclaration.add(ASTNodeCreator.createVariable(test.getName().asString(), field.getElementType().asString(), modifiers));
        }
    }
}