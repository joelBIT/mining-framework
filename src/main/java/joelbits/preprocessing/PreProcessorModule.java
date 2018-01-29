package joelbits.preprocessing;

import joelbits.plugins.CloneService;
import joelbits.plugins.spi.Clone;
import joelbits.preprocessing.preprocessors.PreProcessor;
import joelbits.preprocessing.preprocessors.PreProcessorFactory;
import joelbits.preprocessing.types.SourceType;
import joelbits.preprocessing.utils.FileRepositoryExtractor;
import joelbits.utils.FrameworkUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.GenericOptionsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PreProcessorModule {
    private static final Logger log = LoggerFactory.getLogger(PreProcessorModule.class);

    public static void main(String[] args) throws IOException {
        new PreProcessorModule().preProcess(args);
    }

    public void preProcess(String[] args) throws IOException {
        String[] otherArgs = new GenericOptionsParser(new Configuration(), args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("Expects at least 2 parameters");
            System.exit(2);
        }

        String projectsMetadata = args[0];
        String source = args[1];

        if (otherArgs.length > 2) {
            String clonePlugin = source + "cloner";
            try {
                List<String> repositories = getRepositories(projectsMetadata);
                Clone cloner = CloneService.getInstance().getClonePlugin(clonePlugin);
                cloner.clone(repositories);
                log.info("Finished cloning repositories");
            } catch (Exception e) {
                error("Error cloning repositories", e);
            }
        }

        try {
            PreProcessor preProcessor = PreProcessorFactory.getPreProcessor(SourceType.valueOf(source.toUpperCase()));
            preProcessor.preprocess(createFile(projectsMetadata));
            writeCachePathsFile(preProcessor);

            Configuration configuration = new Configuration();
            persistProjects(preProcessor, configuration, new Path(FrameworkUtil.projectSequenceFile()));
            persistBenchmarkFiles(preProcessor, configuration, new Path(FrameworkUtil.benchmarksMapFile()));
        } catch (Exception e) {
            error("Could not find any PreProcessor of type " + source, e);
        }
    }

    private void writeCachePathsFile(PreProcessor preProcessor) throws IOException {
        List<String> cachePaths = new ArrayList<>();
        cachePaths.addAll(preProcessor.projects().keySet());
        Files.write(Paths.get("cachePaths.txt"), cachePaths, Charset.forName("UTF-8"));
    }

    private void persistBenchmarkFiles(PreProcessor preProcessor, Configuration conf, Path path) {
        MapFile.Writer.Option keyClass = MapFile.Writer.keyClass(Text.class);
        try (MapFile.Writer mapWriter = new MapFile.Writer(conf, path, keyClass, MapFile.Writer.valueClass(BytesWritable.class))) {

            Map<String, Map<String, byte[]>> benchmarkFiles = new TreeMap<>(preProcessor.changedBenchmarkFiles());
            for (Map.Entry<String, Map<String, byte[]>> revision : benchmarkFiles.entrySet()) {

                Map<String, byte[]> sortedBenchmarks = new TreeMap<>(revision.getValue());
                for (Map.Entry<String, byte[]> benchmarkFile : sortedBenchmarks.entrySet()) {
                    String key = revision.getKey() + ":" + benchmarkFile.getKey();
                    mapWriter.append(new Text(key), new BytesWritable(benchmarkFile.getValue()));
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private void persistProjects(PreProcessor preProcessor, Configuration conf, Path path) {
        SequenceFile.Writer.Option keyClass = SequenceFile.Writer.keyClass(Text.class);
        SequenceFile.Writer.Option keyValue = SequenceFile.Writer.valueClass(BytesWritable.class);
        SequenceFile.Writer.Option file = SequenceFile.Writer.file(path);

        try (SequenceFile.Writer seqWriter = SequenceFile.createWriter(conf, file, keyClass, keyValue)) {
            Map<String, byte[]> projects = preProcessor.projects();
            for (Map.Entry<String, byte[]> project : projects.entrySet()) {
                seqWriter.append(new Text(project.getKey()), new BytesWritable(project.getValue()));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private void error(String errorMessage, Exception e) {
        log.error(e.toString(), e);
        System.err.println(errorMessage);
        System.exit(-1);
    }

    /**
     * If no file containing a list of repositories were given as a parameter (--repositoryList), a
     * preprocessor (corresponding to --source) will be used to extract this list of repositories from
     * the project metadata file given as parameter (--projectsMetadata).
     *
     * @return                  list of repositories to clone/parse/persist
     */
    private List<String> getRepositories(String projectsMetadata) {
        List<String> repositories = new ArrayList<>();
        File metadataFile = createFile(projectsMetadata);
        try {
            repositories.addAll(FileRepositoryExtractor.repositories(metadataFile, "full_name", "items"));
        } catch (Exception e) {
            error("Could not extract list of repositories from metadata file", e);
        }

        return repositories;
    }

    private File createFile(String fileName) {
        return new File(FrameworkUtil.jarPath() + fileName);
    }
}