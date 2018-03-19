package joelbits.modules.analysis;

import joelbits.modules.analysis.plugins.AnalysisService;
import joelbits.modules.analysis.plugins.spi.Analysis;
import joelbits.utils.PathUtil;
import org.apache.commons.lang.StringUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        List<String> datasets = new ArrayList<>();
        checkArguments(args, datasets);

        Configuration configuration = this.getConf();
        Job job = Job.getInstance(configuration, "Analysis Job");
        loadAnalysisPlugin(args, job);
        setDefaultSettings(job);
        loadDatasets(datasets, job);
        FileOutputFormat.setOutputPath(job, new Path(PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY));

        int completionStatus = job.waitForCompletion(true) ? 0 : 1;
        if (completionStatus == 0) {
            createOutput(args[2], configuration);
        }

        return completionStatus;
    }

    private void createOutput(String arg, Configuration configuration) {
        String inputJobDirectory = PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY;
        String outputFile = PathUtil.jarPath() + arg;
        try {
            FileUtil.copyMerge(FileSystem.get(configuration), new Path(inputJobDirectory), FileSystem.get(configuration), new Path(outputFile), true, configuration, null);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void checkArguments(String[] args, List<String> datasets) {
        if (args.length < 3) {
            System.err.println("At least three parameters are expected");
            System.exit(2);
        } else if (args.length > 3) {
            for (int i = 3; i < args.length; i++) {
                datasets.add(PathUtil.jarPath() + args[i] + File.separator);
            }
        }
    }

    private void setDefaultSettings(Job job) {
        job.setJarByClass(AnalysisModule.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
    }

    private void loadAnalysisPlugin(String[] args, Job job) {
        try {
            Analysis analysisPlugin = AnalysisService.getInstance().getAnalysisPlugin(args[0]);
            job.setMapperClass(analysisPlugin.mapper(args[1]));
            job.setReducerClass(analysisPlugin.reducer(args[1]));
        } catch (IllegalArgumentException e) {
            System.err.println("Could not find mapper/reducer for " + args[1]);
            System.exit(2);
        }
    }

    private void loadDatasets(List<String> datasets, Job job) {
        try {
            if (datasets.isEmpty()) {
                FileInputFormat.addInputPath(job, new Path(PathUtil.projectSequenceFile()));
            } else {
                FileInputFormat.addInputPaths(job, StringUtils.join(datasets, ","));
            }
        } catch (IOException e) {
            System.err.println("Could not find one or more of the datasets");
        }
    }
}
