package joelbits.analysisExecuter;

import joelbits.analysisExecuter.utils.AnalysisMapperFactory;
import joelbits.analysisExecuter.utils.AnalysisReducerFactory;
import joelbits.utils.FrameworkUtil;
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
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * The driver program for MapReduce job.
 */
public class AnalysisModule extends Configured implements Tool {
    private static final String OUTPUT_JOB_DIRECTORY = "output";

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new Configuration(), new AnalysisModule(), args));
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration configuration = this.getConf();
        String[] otherArgs = new GenericOptionsParser(configuration, args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("At least two parameters expected");
            System.exit(2);
        }

        Job job = Job.getInstance(configuration, "Analysis Job");
        job.setJarByClass(AnalysisModule.class);

        try {
            job.setMapperClass(AnalysisMapperFactory.mapper(args[0]));
            job.setReducerClass(AnalysisReducerFactory.reducer(args[0]));
        } catch (IllegalArgumentException e) {
            System.err.println("Could not find mapper/reducer for " + args[0]);
            System.exit(2);
        }


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(FrameworkUtil.projectSequenceFile()));
        job.setInputFormatClass(SequenceFileInputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path(FrameworkUtil.jarPath() + OUTPUT_JOB_DIRECTORY));
        job.setOutputFormatClass(TextOutputFormat.class);

        int completionStatus = job.waitForCompletion(true) ? 0 : 1;

        if (completionStatus == 0) {
            String inputJobDirectory = FrameworkUtil.jarPath() + OUTPUT_JOB_DIRECTORY;
            String outputFile = FrameworkUtil.jarPath() + args[1];
            try {
                FileUtil.copyMerge(FileSystem.get(configuration), new Path(inputJobDirectory), FileSystem.get(configuration), new Path(outputFile), true, configuration, null);
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        return completionStatus;
    }
}
