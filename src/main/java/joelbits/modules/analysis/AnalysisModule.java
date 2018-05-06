package joelbits.modules.analysis;

import joelbits.model.utils.PathUtil;
import joelbits.modules.analysis.plugins.AnalysisService;
import joelbits.modules.analysis.plugins.spi.Analysis;
import joelbits.utils.CommandLineUtil;
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
 * Entry point to the analysis module of the framework.
 */
public final class AnalysisModule extends Configured implements Tool {
    private static final String OUTPUT_JOB_DIRECTORY = "output_job";
    private CommandLineUtil cmd;
    private Job job;
    private static final String DATASET = "dataset";
    private static final String OUTPUT_FILE = "output";
    private static final String ANALYSIS = "analysis";
    private static final String ANALYSIS_PLUGIN = "plugin";

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new Configuration(), new AnalysisModule(), args));
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration configuration = this.getConf();
        job = Job.getInstance(configuration, "Analysis Job");
        CommandLineUtil.CommandLineBuilder cmdBuilder = new CommandLineUtil.CommandLineBuilder(args);

        try {
            cmd = cmdBuilder
                    .parameterWithArgument(DATASET, false, "name of dataset(s) to perform analyses on")
                    .parameterWithArgument(OUTPUT_FILE, true, "name of the created file containing the analysis results")
                    .parameterWithArgument(ANALYSIS_PLUGIN, true, "name of the analysis plugin to use")
                    .parameterWithArgument(ANALYSIS, true, "name of the specific analysis to perform")
                    .build();

            loadDatasets(cmd.getArgumentValue(DATASET));
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }

        loadAnalysisPlugin();
        setDefaultSettings();
        FileOutputFormat
                .setOutputPath(job, new Path(PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY));

        int completionStatus = 1;
        try {
            completionStatus = job.waitForCompletion(true) ? 0 : 1;
            if (completionStatus == 0) {
                createOutput(outputFileName(), configuration);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        return completionStatus;
    }

    private String outputFileName() {
        return cmd.getArgumentValue(OUTPUT_FILE);
    }

    private void createOutput(String arg, Configuration configuration) {
        String inputJobDirectory = PathUtil.jarPath() + OUTPUT_JOB_DIRECTORY;
        String outputFile = PathUtil.jarPath() + arg;
        try {
            FileUtil.copyMerge(FileSystem.get(configuration), new Path(inputJobDirectory), FileSystem.get(configuration), new Path(outputFile), true, configuration, null);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void setDefaultSettings() {
        job.setJarByClass(AnalysisModule.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
    }

    private void loadAnalysisPlugin() {
        try {
            Analysis analysisPlugin = AnalysisService.getInstance().getAnalysisPlugin(plugin());
            job.setMapperClass(analysisPlugin.mapper(analysis()));
            job.setReducerClass(analysisPlugin.reducer(analysis()));
        } catch (IllegalArgumentException e) {
            System.err.println("Could not find mapper/reducer for " + analysis());
            System.exit(2);
        }
    }

    private String plugin() {
        return cmd.getArgumentValue(ANALYSIS_PLUGIN);
    }

    private String analysis() {
        return cmd.getArgumentValue(ANALYSIS);
    }

    private void loadDatasets(String datasets) {
        try {
            if (datasets.isEmpty()) {
                FileInputFormat.addInputPath(job, new Path(PathUtil.projectSequenceFile()));
            } else {
                FileInputFormat.addInputPaths(job, datasets);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
