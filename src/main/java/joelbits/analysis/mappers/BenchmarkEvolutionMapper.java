package joelbits.analysis.mappers;

import joelbits.analysis.utils.AnalysisUtil;
import joelbits.analysis.visitors.BenchmarkEvolutionVisitor;
import joelbits.model.ast.ASTRoot;
import joelbits.model.project.CodeRepository;
import joelbits.model.project.Project;
import joelbits.model.project.Revision;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public class BenchmarkEvolutionMapper extends Mapper<Text, BytesWritable, Text, Text> {

    @Override
    public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
        Project project = AnalysisUtil.getProject(value);
        for (CodeRepository repository : project.getRepositories()) {
            for (Revision revision : repository.getRevisions()) {
                List<ASTRoot> benchmarkFiles = AnalysisUtil.allChangedBenchmarkFiles(revision, repository.getUrl());
                for (ASTRoot changedFile : benchmarkFiles) {
                    String declarationName = changedFile.getNamespaces().get(0).getDeclarations().get(0).getName();
                    BenchmarkEvolutionVisitor visitor = new BenchmarkEvolutionVisitor();
                    changedFile.accept(visitor);
                    for (String change : visitor.getBenchmarkChanges()) {
                        context.write(new Text(project.getName() + ":" + declarationName + ":" + change), new Text(revision.getCommitDate().toString() + " log: " + revision.getLog()));
                    }
                }
            }
        }
        context.write(new Text(project.getName()), new Text(project.getCreatedDate().toString()));
    }
}
