package joelbits.preprocessor.parsers.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos.Expression;
import joelbits.model.ast.protobuf.ASTProtos.Modifier;
import joelbits.preprocessor.parsers.utils.ASTNodeCreater;
import joelbits.preprocessor.utils.TypeConverter;

import java.util.List;
import java.util.Map;

public class DeclarationModifierVisitor extends VoidVisitorAdapter<List<Modifier>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<Modifier> modifiers) {
        for (AnnotationExpr topLevelAnnotation : declaration.getAnnotations()) {
            Map<String, Expression> membersAndValues = TypeConverter.convertAnnotationMembers(topLevelAnnotation);
            modifiers.add(ASTNodeCreater.createAnnotationModifier(topLevelAnnotation, membersAndValues));
        }

        for (com.github.javaparser.ast.Modifier topLevelModifier : declaration.getModifiers()) {
            modifiers.add(ASTNodeCreater.createModifier(topLevelModifier.name()));
        }
    }
}
