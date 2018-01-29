package joelbits.analysisExecuter.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import joelbits.analysisExecuter.converters.ASTConverter;
import joelbits.analysisExecuter.converters.ProjectConverter;
import joelbits.model.ast.ASTRoot;
import joelbits.model.project.ChangedFile;
import joelbits.model.project.CodeRepository;
import joelbits.model.project.Project;
import joelbits.model.project.Revision;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class AnalysisUtil {
    private static final Logger log = LoggerFactory.getLogger(AnalysisUtil.class);

    public static ASTRoot getAST(byte[] benchmarkFile) throws InvalidProtocolBufferException {
        return ASTConverter.convert(benchmarkFile);
    }

    public static Project getProject(BytesWritable value) throws InvalidProtocolBufferException {
        byte[] project = Arrays.copyOf(value.getBytes(), value.getLength());
        return ProjectConverter.convert(project);
    }

    /**
     * Return latest version of all unique benchmark files (in their ASTRoot representation) from repository.
     *
     * @param repository        the repository to retrieve latest benchmark file snapshots from
     * @return                  list of latest version of all benchmark files in repository
     */
    public static Set<ASTRoot> latestFileSnapshots(CodeRepository repository) {
        Set<ASTRoot> latestVersionsChangedFiles = new HashSet<>();
        Set<String> uniqueBenchmarkFiles = new HashSet<>();
        Set<String> mapFileKeys = new HashSet<>();

        for (Revision revision : repository.getRevisions()) {
            for (ChangedFile file : revision.getFiles()) {
                if (!uniqueBenchmarkFiles.contains(file.getName())) {
                    uniqueBenchmarkFiles.add(file.getName());
                    mapFileKeys.add(repository.getUrl() + ":" + revision.getId() + ":" + file.getName());
                }
            }
        }

        Set<byte[]> benchmarkFiles = readMapFile(mapFileKeys);
        for (byte[] file : benchmarkFiles) {
            try {
                latestVersionsChangedFiles.add(ASTConverter.convert(file));
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }

        return latestVersionsChangedFiles;
    }

    private static Set<byte[]> readMapFile(Set<String> mapFileKeys) {
        Set<byte[]> benchmarkFiles = new HashSet<>();

        Configuration conf = new Configuration();
        Path path = new Path(System.getProperty("user.dir") + File.separator + "benchmarkFiles" + File.separator);
        try (MapFile.Reader mapReader = new MapFile.Reader(path, conf)) {
            for (String fileKey : mapFileKeys) {
                BytesWritable value = (BytesWritable) ReflectionUtils.newInstance(mapReader.getValueClass(), conf);
                mapReader.next(new Text(fileKey), value);
                benchmarkFiles.add(Arrays.copyOf(value.getBytes(), value.getLength()));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return benchmarkFiles;
    }
}
