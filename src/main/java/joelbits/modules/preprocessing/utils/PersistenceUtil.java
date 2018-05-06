package joelbits.modules.preprocessing.utils;

import joelbits.model.utils.PathUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Used to create the Hadoop SequenceFile and the Hadoop MapFile. The SequenceFile contains the source
 * code file ASTs. The MapFile contains projects and their metadata,
 * such as revisions and path to changed files.
 */
public final class PersistenceUtil {
    private static final Logger log = LoggerFactory.getLogger(PersistenceUtil.class);
    private final Configuration configuration = new Configuration();
    private String outputFileName = StringUtils.EMPTY;
    private static final String FILES_NAME_ENDING = "Files" + File.separator;
    private final List<Path> temporaryFilePaths = new ArrayList<>();

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    /**
     * Creates a dataset containing project metadata.
     *
     * @param projects  the projects to be persisted
     */
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

    /**
     * Creates a dataset containing the ASTs of changed files in a project.
     *
     * @param changedFiles              the ASTs of the changed files
     * @param temporaryDatasetName      the name of the created dataset
     */
    public void persistChangedFiles(Map<String, Map<String, byte[]>> changedFiles, String temporaryDatasetName) {
        MapFile.Writer.Option keyClass = MapFile.Writer.keyClass(Text.class);
        String datasetName = temporaryDatasetName + FILES_NAME_ENDING;
        temporaryFilePaths.add(new Path(datasetName));
        try (MapFile.Writer mapWriter = new MapFile.Writer(configuration, new Path(datasetName), keyClass, MapFile.Writer.valueClass(BytesWritable.class))) {
            persistFiles(changedFiles, mapWriter);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void persistFiles(Map<String, Map<String, byte[]>> changedFiles, MapFile.Writer mapWriter) throws IOException {
        Map<String, Map<String, byte[]>> files = new TreeMap<>(changedFiles);
        for (Map.Entry<String, Map<String, byte[]>> revision : files.entrySet()) {
            Map<String, byte[]> sortedFiles = new TreeMap<>(revision.getValue());
            for (Map.Entry<String, byte[]> sortedFile : sortedFiles.entrySet()) {
                String key = revision.getKey() + ":" + sortedFile.getKey();
                mapWriter.append(new Text(key), new BytesWritable(sortedFile.getValue()));
            }
        }
    }

    /**
     * Merges all created temporary datasets into one dataset and deletes the temporary datasets afterwards.
     *
     * @throws Exception
     */
    public void mergeDataSets() throws Exception {
        MapFile.Merger merger = new MapFile.Merger(configuration);
        Path[] paths = temporaryFilePaths.toArray(new Path[temporaryFilePaths.size()]);
        String filesFile = StringUtils.isEmpty(outputFileName) ? PathUtil.changedFilesMapFile() : outputFileName + FILES_NAME_ENDING;
        merger.merge(paths, true, new Path(filesFile));
    }
}
