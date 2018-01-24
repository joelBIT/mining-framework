package joelbits.analysisExecuter.visitors;

import joelbits.model.ast.*;
import joelbits.model.ast.types.ModifierType;

import java.util.*;

/**
 * Collects all annotations of each class, and also collects the @Benchmark for every method in each class
 * that has one. If a configuration exist inside the @Benchmark the configuration will be added to the list
 * of the corresponding @Benchmark, otherwise the list will be empty if no configuration of the @Benchmark annotation.
 *
 * The reason for collecting the class level annotations as well as the method level @Benchmark annotations is
 * because, for example, @Warmup could be configured on class level which then applies to all methods so in that
 * case there is no need to configure each @Benchmark and thus they are probably empty. In order to know how many
 * iterations, forks, etc, that are run for each @Benchmark we must look at the corresponding class level annotations.
 */
public class BenchmarkConfigurationVisitor implements Visitor {
    private final Map<String, Map<String, List<String>>> benchmarkConfigurations = new LinkedHashMap<>();     // key is classname, value is map of @Benchmark annotations (key is method name, and value is configuration of @Benchmark annotation)
    private final Map<String, Map<String, List<String>>> classConfigurations = new HashMap<>();               // key is classname, value is map of class annotations (key is annotation name, value is configuration of annotation)

    @Override
    public boolean visit(ASTNode node) {
        return false;
    }

    @Override
    public boolean visitEnter(ASTNode node) {
        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;
            classConfigurations.put(declaration.getName(), new HashMap<>());
            benchmarkConfigurations.put(declaration.getName(), new HashMap<>());

            for (Modifier modifier : declaration.getModifiers()) {
                if (modifier.getType().equals(ModifierType.ANNOTATION)) {
                    classConfigurations.get(declaration.getName()).put(modifier.getName(), new ArrayList<>());

                    for (String configuration : modifier.getMembersAndValues()) {
                        classConfigurations.get(declaration.getName()).get(modifier.getName()).add(configuration);
                    }
                }
            }

            for (Method method : declaration.getMethods()) {
                for (Modifier modifier : method.getModifiers()) {
                    if (!modifier.getName().equals("Benchmark") || !modifier.getType().equals(ModifierType.ANNOTATION)) {
                        continue;
                    }
                    benchmarkConfigurations.get(declaration.getName()).put(method.getName(), new ArrayList<>());

                    for (String configuration : modifier.getMembersAndValues()) {
                        benchmarkConfigurations.get(declaration.getName()).get(method.getName()).add(configuration);
                    }
                }
            }
        }

        if (node instanceof Variable || node instanceof Method) {
            return false;
        }

        return true;
    }

    @Override
    public boolean visitLeave(ASTNode node) {
        if (node instanceof Variable) {
            return false;
        }
        return true;
    }

    public Map<String, Map<String, List<String>>> getBenchmarkConfigurations() {
        return benchmarkConfigurations;
    }

    public Map<String, Map<String, List<String>>> getClassConfigurations() {
        return classConfigurations;
    }
}
