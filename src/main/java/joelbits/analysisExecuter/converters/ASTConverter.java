package joelbits.analysisExecuter.converters;

import com.google.protobuf.InvalidProtocolBufferException;
import joelbits.model.ast.*;
import joelbits.model.ast.protobuf.ASTProtos;
import joelbits.model.ast.types.DeclarationType;
import joelbits.model.ast.types.ExpressionType;
import joelbits.model.ast.types.ModifierType;
import joelbits.model.ast.types.VisibilityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps a ASTRoot protocol buffer message into its ASTRoot model representation.
 */
public class ASTConverter {

    public static ASTRoot convert(byte[] astRoot) throws InvalidProtocolBufferException {
        ASTProtos.ASTRoot root = ASTProtos.ASTRoot.parseFrom(astRoot);

        List<Namespace> namespaces = new ArrayList<>();
        for (ASTProtos.Namespace namespace : root.getNamespacesList()) {
            List<Modifier> modifiers = new ArrayList<>();
            for (ASTProtos.Modifier modifier : namespace.getModifiersList()) {
                modifiers.add(convertModifier(modifier));
            }

            List<Declaration> declarations = new ArrayList<>();
            for (ASTProtos.Declaration declaration : namespace.getDeclarationsList()) {
                declarations.add(convertDeclaration(declaration));
            }

            namespaces.add(new Namespace(namespace.getName(), declarations, modifiers));
        }

        return new ASTRoot(root.getImportsList(), namespaces);
    }

    private static Modifier convertModifier(ASTProtos.Modifier modifier) {
        ModifierType type = ModifierType.valueOf(modifier.getType().name());
        VisibilityType visibilityType = VisibilityType.valueOf(modifier.getVisibility().name());

        return new Modifier(modifier.getName(), type, modifier.getMembersAndValuesList(), visibilityType, modifier.getOther());
    }

    public static Expression convertExpression(ASTProtos.Expression expression) {
        String literal = expression.getLiteral();
        String method = expression.getMethod();
        String variable = expression.getVariable();
        ExpressionType type = ExpressionType.valueOf(expression.getType().name());

        List<Expression> methodArguments = new ArrayList<>();
        if (type.equals(ExpressionType.METHODCALL)) {
            for (ASTProtos.Expression expr : expression.getMethodArgumentsList()) {
                methodArguments.add(convertExpression(expr));
            }
        }

        List<Variable> variableDeclarations = new ArrayList<>();
        for (ASTProtos.Variable var : expression.getVariableDeclarationsList()) {
            variableDeclarations.add(convertField(var));
        }

        return new Expression(type, literal, method, variable, methodArguments, variableDeclarations);
    }

    private static Declaration convertDeclaration(ASTProtos.Declaration declaration) {
        DeclarationType type = DeclarationType.valueOf(declaration.getType().name());

        List<Modifier> modifiers = new ArrayList<>();
        for (ASTProtos.Modifier modifier : declaration.getModifiersList()) {
            modifiers.add(convertModifier(modifier));
        }

        List<Type> parents = new ArrayList<>();
        for (ASTProtos.Type parent : declaration.getParentsList()) {
            parents.add(convertType(parent));
        }

        List<Variable> fields = new ArrayList<>();
        for (ASTProtos.Variable field : declaration.getFieldsList()) {
            fields.add(convertField(field));
        }

        List<Method> methods = new ArrayList<>();
        for (ASTProtos.Method method : declaration.getMethodsList()) {
            methods.add(convertMethod(method));
        }

        List<Declaration> nestedDeclarations = new ArrayList<>();
        for (ASTProtos.Declaration nestedDeclaration : declaration.getNestedDeclarationsList()) {
            nestedDeclarations.add(convertNestedDeclaration(nestedDeclaration));
        }

        return new Declaration(declaration.getName(), nestedDeclarations, modifiers, type, fields, methods, parents);
    }

    private static Type convertType(ASTProtos.Type type) {
        return new Type(type.getName(), DeclarationType.valueOf(type.getType().name()));
    }

    private static Variable convertField(ASTProtos.Variable field) {
        List<Modifier> modifiers = new ArrayList<>();
        for (ASTProtos.Modifier modifier : field.getModifiersList()) {
            modifiers.add(convertModifier(modifier));
        }

        return new Variable(field.getName(), convertType(field.getType()), convertInitializer(field.getInitializer()), modifiers);
    }

    private static Expression convertInitializer(ASTProtos.Expression expression) {
        ExpressionType type = ExpressionType.valueOf(expression.getType().name());

        return new Expression(type, expression.getLiteral(), expression.getMethod(), expression.getVariable(), new ArrayList<>(), new ArrayList<>());
    }

    private static Method convertMethod(ASTProtos.Method method) {
        List<Modifier> modifiers = new ArrayList<>();
        for (ASTProtos.Modifier modifier : method.getModifiersList()) {
            modifiers.add(convertModifier(modifier));
        }

        List<Variable> arguments = new ArrayList<>();
        for (ASTProtos.Variable argument : method.getArgumentsList()) {
            arguments.add(convertField(argument));
        }

        List<Expression> bodyContent = new ArrayList<>();

        return new Method(method.getName(), arguments, convertType(method.getReturnType()), modifiers, bodyContent);
    }

    private static Declaration convertNestedDeclaration(ASTProtos.Declaration declaration) {
        DeclarationType type = DeclarationType.valueOf(declaration.getType().name());

        List<Modifier> modifiers = new ArrayList<>();
        for (ASTProtos.Modifier modifier : declaration.getModifiersList()) {
            modifiers.add(convertModifier(modifier));
        }

        List<Type> parents = new ArrayList<>();
        for (ASTProtos.Type parent : declaration.getParentsList()) {
            parents.add(convertType(parent));
        }

        List<Variable> fields = new ArrayList<>();
        for (ASTProtos.Variable field : declaration.getFieldsList()) {
            fields.add(convertField(field));
        }

        List<Method> methods = new ArrayList<>();
        for (ASTProtos.Method method : declaration.getMethodsList()) {
            methods.add(convertMethod(method));
        }

        return new Declaration(declaration.getName(), new ArrayList<>(), modifiers, type, fields, methods, parents);
    }
}
