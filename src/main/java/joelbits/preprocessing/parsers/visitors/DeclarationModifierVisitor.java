package joelbits.preprocessing.parsers.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import joelbits.model.ast.protobuf.ASTProtos.Modifier;
import joelbits.preprocessing.parsers.utils.ASTNodeCreator;
import joelbits.preprocessing.utils.TypeConverter;

import java.util.List;

public class DeclarationModifierVisitor extends VoidVisitorAdapter<List<Modifier>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, List<Modifier> modifiers) {
        for (AnnotationExpr topLevelAnnotation : declaration.getAnnotations()) {
            List<String> membersAndValues = TypeConverter.convertAnnotationMembers(topLevelAnnotation);
            modifiers.add(ASTNodeCreator.createAnnotationModifier(topLevelAnnotation, membersAndValues));
        }

        for (com.github.javaparser.ast.Modifier topLevelModifier : declaration.getModifiers()) {
            modifiers.add(ASTNodeCreator.createModifier(topLevelModifier.name()));
        }
    }
}
