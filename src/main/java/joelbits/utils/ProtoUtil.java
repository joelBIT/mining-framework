package joelbits.utils;

import com.google.protobuf.Timestamp;
import joelbits.model.project.protobuf.ProjectProtos;

import java.util.List;

import static joelbits.model.project.protobuf.ProjectProtos.Person;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile.FileType;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile.ChangeType;
import static joelbits.model.project.protobuf.ProjectProtos.Revision;
import static joelbits.model.project.protobuf.ProjectProtos.Project;
import static joelbits.model.project.protobuf.ProjectProtos.Project.ProjectType;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository.RepositoryType;

import static joelbits.model.ast.protobuf.ASTProtos.ASTRoot;
import static joelbits.model.ast.protobuf.ASTProtos.Namespace;
import static joelbits.model.ast.protobuf.ASTProtos.Declaration;
import static joelbits.model.ast.protobuf.ASTProtos.DeclarationType;
import static joelbits.model.ast.protobuf.ASTProtos.Expression;
import static joelbits.model.ast.protobuf.ASTProtos.Expression.ExpressionType;
import static joelbits.model.ast.protobuf.ASTProtos.Statement;
import static joelbits.model.ast.protobuf.ASTProtos.Statement.StatementType;
import static joelbits.model.ast.protobuf.ASTProtos.Modifier;
import static joelbits.model.ast.protobuf.ASTProtos.Modifier.ModifierType;
import static joelbits.model.ast.protobuf.ASTProtos.Modifier.VisibilityType;
import static joelbits.model.ast.protobuf.ASTProtos.Variable;
import static joelbits.model.ast.protobuf.ASTProtos.Method;
import static joelbits.model.ast.protobuf.ASTProtos.Type;

public final class ProtoUtil {
    public static Person committer(String username, String email) {
        return committer(username, username, email);
    }

    public static Person committer(String username, String realName, String email) {
        return Person.newBuilder()
                .setEmail(email)
                .setUsername(username)
                .setRealName(realName)
                .build();
    }

    public static Revision revision(int commitTime, String revisionId, Person committer, String log, List<ChangedFile> changedFiles) {
        return ProjectProtos.Revision.newBuilder()
                .setCommitDate(timestamp(commitTime))
                .setId(revisionId)
                .setCommitter(committer)
                .setLog(log)
                .addAllFiles(changedFiles)
                .build();
    }

    private static Timestamp timestamp(int seconds) {
        return Timestamp.newBuilder().setSeconds(seconds).build();
    }

    public static ChangedFile changedFile(String name, String fileType, String changeType) {
        return ChangedFile.newBuilder()
                .setName(name)
                .setType(FileType.valueOf(fileType.toUpperCase()))
                .setChange(ChangeType.valueOf(convertChangeType(changeType)))
                .build();
    }

    /**
     * Since some sources may have different ChangeTypes than used in the Protocol Buffer, they have to
     * be mapped to corresponding ChangeType.
     *
     * @param type          the ChangeType of the parsed source
     * @return              the ChangeType used in the Project Protocol Buffer message
     */
    private static String convertChangeType(String type) {
        switch(type.toUpperCase()) {
            case "MODIFY":
                return "MODIFIED";
            case "ADD":
                return "ADDED";
            case "DELETE":
                return "DELETED";
            default:
                return type.toUpperCase();
        }
    }

    public static CodeRepository repository(String url, RepositoryType type, List<Revision> revisions) {
        return CodeRepository.newBuilder()
                .setUrl(url)
                .setType(type)
                .addAllRevision(revisions)
                .build();
    }

    public static Project project(String name, int createdTime, String id, String url, ProjectType type, List<CodeRepository> repositories, List<String> languages) {
        return Project.newBuilder()
                .addAllRepositories(repositories)
                .addAllProgrammingLanguages(languages)
                .setCreatedDate(timestamp(createdTime))
                .setId(id)
                .setName(name)
                .setType(type)
                .setUrl(url)
                .addAllRepositories(repositories)
                .build();
    }

    public static ASTRoot astRoot(List<String> imports, List<Namespace> namespaces) {
        return ASTRoot.newBuilder()
                .addAllImports(imports)
                .addAllNamespaces(namespaces)
                .build();
    }

    public static Namespace namespace(String name, List<Declaration> declarations, List<Modifier> modifiers) {
        return Namespace.newBuilder()
                .setName(name)
                .addAllDeclarations(declarations)
                .addAllModifiers(modifiers)
                .build();
    }

    public static Declaration declaration(String name, String type, List<Declaration> nestedDeclarations, List<Modifier> modifiers, List<Variable> fields, List<Method> methods, List<Type> parents) {
        return Declaration.newBuilder()
                .setName(name)
                .setType(DeclarationType.valueOf(type.toUpperCase()))
                .addAllNestedDeclarations(nestedDeclarations)
                .addAllModifiers(modifiers)
                .addAllFields(fields)
                .addAllMethods(methods)
                .addAllParents(parents)
                .build();
    }

    public static Modifier modifier(String name, String type, List<String> members, List<Expression> values, String visibility, String other) {
        return Modifier.newBuilder()
                .setName(name)
                .setType(ModifierType.valueOf(type.toUpperCase()))
                .addAllMembers(members)
                .addAllValues(values)
                .setVisibility(VisibilityType.valueOf(visibility.toUpperCase()))
                .setOther(other)
                .build();
    }

    public static Method method(String name, List<Variable> arguments, Type returnType, List<Modifier> modifiers, List<Statement> statements, List<Type> exceptionTypes) {
        return Method.newBuilder()
                .setName(name)
                .addAllArguments(arguments)
                .setReturnType(returnType)
                .addAllModifiers(modifiers)
                .addAllStatements(statements)
                .addAllExceptionTypes(exceptionTypes)
                .build();
    }

    public static Type type(String name, String type) {
        return Type.newBuilder()
                .setName(name)
                .setType(DeclarationType.valueOf(type.toUpperCase()))
                .build();
    }

    public static Variable variable(String name, Type type, Expression initializer, List<Modifier> modifiers) {
        return Variable.newBuilder()
                .setName(name)
                .setType(type)
                .setInitializer(initializer)
                .addAllModifiers(modifiers)
                .build();
    }

    public static Expression expression(String type, List<Expression> expressions, String literal, String method, String variable, List<Expression> methodArguments, List<Variable> variableDeclarations, Modifier annotation) {
        return Expression.newBuilder()
                .setType(ExpressionType.valueOf(type.toUpperCase()))
                .addAllExpressions(expressions)
                .setLiteral(literal)
                .setMethod(method)
                .setVariable(variable)
                .addAllMethodArguments(methodArguments)
                .addAllVariableDeclarations(variableDeclarations)
                .setAnnotation(annotation)
                .build();
    }

    public static Statement statement(String type, List<Statement> statements, Expression expression, List<Expression> expressions, Expression condition, List<Expression> initializations, Variable variableDeclaration, List<Expression> updates) {
        return Statement.newBuilder()
                .setType(StatementType.valueOf(type.toUpperCase()))
                .addAllStatements(statements)
                .setExpression(expression)
                .addAllExpressions(expressions)
                .setCondition(condition)
                .addAllInitializations(initializations)
                .setVariableDeclaration(variableDeclaration)
                .addAllUpdates(updates)
                .build();
    }
}
