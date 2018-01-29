package joelbits.preprocessing.parsers.utils;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import joelbits.preprocessing.utils.TypeConverter;

import static joelbits.model.ast.protobuf.ASTProtos.Modifier.VisibilityType;
import static joelbits.model.ast.protobuf.ASTProtos.Method;
import static joelbits.model.ast.protobuf.ASTProtos.Variable;
import static joelbits.model.ast.protobuf.ASTProtos.Declaration;
import static joelbits.model.ast.protobuf.ASTProtos.DeclarationType;
import static joelbits.model.ast.protobuf.ASTProtos.Modifier;
import static joelbits.model.ast.protobuf.ASTProtos.Modifier.ModifierType;
import static joelbits.model.ast.protobuf.ASTProtos.Expression;
import static joelbits.model.ast.protobuf.ASTProtos.Expression.ExpressionType;
import static joelbits.model.ast.protobuf.ASTProtos.Type;
import static joelbits.model.ast.protobuf.ASTProtos.ASTRoot;
import static joelbits.model.ast.protobuf.ASTProtos.Namespace;

import java.util.List;

public class ASTNodeCreator {
    public static Variable createVariable(String name, String type, List<Modifier> argumentModifiers) {
        return Variable.newBuilder()
                .setType(createType(type))
                .setName(name)
                .addAllModifiers(argumentModifiers)
                .build();
    }

    public static Type createType(String name) {
        return Type.newBuilder()
                .setName(name)
                .setType(DeclarationType.OTHER)
                .build();
    }

    public static Modifier createAnnotationModifier(AnnotationExpr annotationExpr, List<String> membersAndValues) {
        return Modifier.newBuilder()
                .setType(ModifierType.ANNOTATION)
                .setName(annotationExpr.getNameAsString())
                .addAllMembersAndValues(membersAndValues)
                .build();
    }

    public static Method createMethod(List<Modifier> methodModifiers, MethodDeclaration method, List<Variable> arguments, List<Expression> methodBody) {
        return Method.newBuilder()
                .setName(method.getNameAsString())
                .setReturnType(ASTNodeCreator.createType(method.getType().asString()))
                .addAllModifiers(methodModifiers)
                .addAllArguments(arguments)
                .addAllBodyContent(methodBody)
                .build();
    }

    public static Modifier createModifier(String modifierName) {
        ModifierType type = TypeConverter.getModifierType(modifierName);
        Modifier.Builder builder = Modifier.newBuilder();

        if (type.equals(ModifierType.VISIBILITY)) {
            return builder.setVisibility(VisibilityType.valueOf(modifierName)).setType(type).build();
        }
        if (type.equals(ModifierType.OTHER)) {
            return builder.setOther(modifierName).setType(type).build();
        }

        return builder.setName(modifierName).setType(type).build();
    }

    public static Expression createMethodCallExpression(MethodCallExpr methodCall, List<Expression> methodArguments) {
        return Expression.newBuilder()
                .setType(ExpressionType.METHODCALL)
                .setMethod(methodCall.getNameAsString())
                .addAllMethodArguments(methodArguments)
                .build();
    }

    public static Expression createMethodCallArgumentExpression(String argument) {
        return Expression.newBuilder()
                .setType(ExpressionType.OTHER)
                .setVariable(argument)
                .build();
    }

    public static Declaration createNamespaceDeclaration(ClassOrInterfaceDeclaration declaration, List<Variable> allFields, List<Method> allMethods, List<Modifier> topModifiers, List<Declaration> nestedDeclarations) {
        return Declaration.newBuilder()
                .setName(declaration.getNameAsString())
                .setType(TypeConverter.getDeclarationType(declaration))
                .addAllModifiers(topModifiers)
                .addAllFields(allFields)
                .addAllMethods(allMethods)
                .addAllNestedDeclarations(nestedDeclarations)
                .build();
    }

    public static Declaration createNestedDeclaration(ClassOrInterfaceDeclaration declaration, List<Variable> allFields, List<Method> allMethods, List<Modifier> modifiers) {
        return Declaration.newBuilder()
                .setName(declaration.getNameAsString())
                .setType(TypeConverter.getDeclarationType(declaration))
                .addAllModifiers(modifiers)
                .addAllFields(allFields)
                .addAllMethods(allMethods)
                .build();
    }

    public static ASTRoot createAstRoot(List<String> imports, List<Namespace> namespaces) {
        return ASTRoot.newBuilder()
                .addAllImports(imports)
                .addAllNamespaces(namespaces)
                .build();
    }
}
