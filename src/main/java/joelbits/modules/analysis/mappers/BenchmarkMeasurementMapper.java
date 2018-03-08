package joelbits.modules.analysis.mappers;

import joelbits.modules.analysis.utils.AnalysisUtil;
import joelbits.modules.analysis.visitors.BenchmarkMeasurementVisitor;
import joelbits.model.ast.ASTRoot;
import joelbits.model.project.CodeRepository;
import joelbits.model.project.Project;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.eclipse.jgit.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BenchmarkMeasurementMapper extends Mapper<Text, BytesWritable, Text, Text> {
    private final List<String> processedBenchmarkFiles = new ArrayList<>();

    @Override
    public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
        Project project = AnalysisUtil.getProject(value);
        for (CodeRepository repository : project.getRepositories()) {
            Set<ASTRoot> benchmarkFiles = AnalysisUtil.latestFileSnapshots(repository);

            for (ASTRoot changedFile : benchmarkFiles) {
                String declarationName = changedFile.getNamespaces().get(0).getDeclarations().get(0).getName();
                if (processedBenchmarkFiles.contains(declarationName)) {
                    continue;
                }
                processedBenchmarkFiles.add(declarationName);

                BenchmarkMeasurementVisitor visitor = new BenchmarkMeasurementVisitor();
                changedFile.accept(visitor);
                writeBenchmarkMeasurementConfigurations(context, declarationName, visitor);
            }
        }
    }

    private void writeBenchmarkMeasurementConfigurations(Context context, String declarationName, BenchmarkMeasurementVisitor visitor) throws IOException, InterruptedException {
        Map<String, List<String>> classConfigurations = visitor.getClassConfigurations();
        if (classConfigurations.isEmpty()) {
            for (Map.Entry<String, Map<String, List<String>>> benchmark : visitor.getBenchmarkConfigurations().entrySet()) {
                for (Map.Entry<String, List<String>> configuration : benchmark.getValue().entrySet()) {
                    if (!StringUtils.isEmptyOrNull(configuration.getKey())) {
                        context.write(new Text(declarationName + ":" + benchmark.getKey()), new Text("@" + configuration.getKey() + "(" + StringUtils.join(configuration.getValue(), ",").trim() + ")"));
                    }
                }
            }

            return;
        }

        for(Map.Entry<String, Map<String, List<String>>> benchmark : visitor.getBenchmarkConfigurations().entrySet()) {
            for (Map.Entry<String, List<String>> benchmarkConfiguration : benchmark.getValue().entrySet()) {
                if (classConfigurations.containsKey(benchmarkConfiguration.getKey())) {
                    context.write(new Text(declarationName + ":" + benchmark.getKey()), new Text("@" + benchmarkConfiguration.getKey() + "(" + StringUtils.join(benchmarkConfiguration.getValue(), ",").trim() + ")"));
                }
            }
            for (Map.Entry<String, List<String>> classConfiguration : classConfigurations.entrySet()) {
                if (!benchmark.getValue().keySet().contains(classConfiguration)) {
                    context.write(new Text(declarationName + ":" + benchmark.getKey()), new Text("@" + classConfiguration.getKey() + "(" + StringUtils.join(classConfiguration.getValue(), ",").trim() + ")"));
                }
            }
        }
    }
}
