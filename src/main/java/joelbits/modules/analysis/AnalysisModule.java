package joelbits.modules.analysis;

import joelbits.modules.analysis.plugins.AnalysisService;
import joelbits.modules.analysis.plugins.spi.Analysis;
import joelbits.utils.PathUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * Minimum 3 input parameters;  --analysisPlugin --analysis --outputFileName --dataset(s)
 * Example: --jmh --benchmarkConfigurations --configurations.txt --jmh_dataset
 *
 * The --analysisPlugin parameter identifies which analysis plugin to use. Since an analysis plugin
 * may contain multiple analyses the user should also add a parameter identifying which specific
 * analysis to run.
 * The --analysis parameter is the specific analysis to run, corresponding to the mapper and reducer
 * parts of the analysis.
 * The --outputFileName parameter is the name for the created output text file containing the analysis results.
 * The --dataset(s) parameter is optional and if used, it names which specific dataset(s) should be subject for
 * analysis. If this parameter is left out the default dataset name will be used (which is the default name for
 * the created dataset after preprocessing).
 *
 */
public class AnalysisModule extends Configured implements Tool {
    private static final String OUTPUT_JOB_DIRECTORY = "output";

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new Configuration(), new AnalysisModule(), args));
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration configuration = this.getConf();
        if (args.length < 3) {
            System.err.println("At least three parameters are expected");
            System.exit(2);
        }

        Job job = Job.getInstance(configuration, "Analysis Job");
        job.setJarByClass(AnalysisModule.class);

        try {
            Analysis analysisPlugin = AnalysisService.getInstance().getAnalysisPlugin(args[0]);
            job.setMapperClass(analysisPlugin.mapper(args[1]));
            job.setReducerClass(analysisPlugin.reducer(args[1]));
        } catch (IllegalArgumentException e) {
            System.err.println("Could not find mapper/reducer for " + args[1]);
            System.exit(2);
        }

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(PathUtil.projectSequenceFile()));
        //FileInputFormat.addInputPaths(job, "commaSeparatedPaths");  // use this to take input dataset to allow for replication (combining several dataset for a specific analysis)
        job.setInputFormatClass(SequenceFileInputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path(PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY));
        job.setOutputFormatClass(TextOutputFormat.class);

        int completionStatus = job.waitForCompletion(true) ? 0 : 1;

        if (completionStatus == 0) {
            String inputJobDirectory = PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY;
            String outputFile = PathUtil.jarPath() + args[2];
            try {
                FileUtil.copyMerge(FileSystem.get(configuration), new Path(inputJobDirectory), FileSystem.get(configuration), new Path(outputFile), true, configuration, null);
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        return completionStatus;
    }
}
