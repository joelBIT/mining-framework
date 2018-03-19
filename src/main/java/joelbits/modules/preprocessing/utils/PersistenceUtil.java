package joelbits.modules.preprocessing.utils;

import joelbits.utils.PathUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Used to create the Hadoop SequenceFile and the Hadoop MapFile. The SequenceFile contains the parsed
 * source code files that contained microbenchmarks. The MapFile contains projects and their metadata,
 * such as revisions and path to changed files.
 */
public final class PersistenceUtil {
    private static final Logger log = LoggerFactory.getLogger(PersistenceUtil.class);
    private final Configuration configuration = new Configuration();
    private String outputFileName = "";

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void persistProjects(Map<String, byte[]> projects) {
        SequenceFile.Writer.Option keyClass = SequenceFile.Writer.keyClass(Text.class);
        SequenceFile.Writer.Option keyValue = SequenceFile.Writer.valueClass(BytesWritable.class);
        String projectFile = StringUtils.isEmpty(outputFileName) ? PathUtil.projectSequenceFile() : outputFileName + File.separator;
        SequenceFile.Writer.Option file = SequenceFile.Writer.file(new Path(projectFile));

        try (SequenceFile.Writer seqWriter = SequenceFile.createWriter(configuration, file, keyClass, keyValue)) {
            for (Map.Entry<String, byte[]> project : projects.entrySet()) {
                seqWriter.append(new Text(project.getKey()), new BytesWritable(project.getValue()));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public void persistBenchmarkFiles(Map<String, Map<String, byte[]>> changedBenchmarkFiles) {
        MapFile.Writer.Option keyClass = MapFile.Writer.keyClass(Text.class);
        String benchmarksFile = StringUtils.isEmpty(outputFileName) ? PathUtil.benchmarksMapFile() : outputFileName + "Benchmarks"+ File.separator;
        try (MapFile.Writer mapWriter = new MapFile.Writer(configuration, new Path(benchmarksFile), keyClass, MapFile.Writer.valueClass(BytesWritable.class))) {
            persistFiles(changedBenchmarkFiles, mapWriter);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private void persistFiles(Map<String, Map<String, byte[]>> changedBenchmarkFiles, MapFile.Writer mapWriter) throws IOException {
        Map<String, Map<String, byte[]>> benchmarkFiles = new TreeMap<>(changedBenchmarkFiles);
        for (Map.Entry<String, Map<String, byte[]>> revision : benchmarkFiles.entrySet()) {
            Map<String, byte[]> sortedBenchmarks = new TreeMap<>(revision.getValue());
            for (Map.Entry<String, byte[]> benchmarkFile : sortedBenchmarks.entrySet()) {
                String key = revision.getKey() + ":" + benchmarkFile.getKey();
                mapWriter.append(new Text(key), new BytesWritable(benchmarkFile.getValue()));
            }
        }
    }
}
