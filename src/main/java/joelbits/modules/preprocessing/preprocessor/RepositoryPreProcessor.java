package joelbits.modules.preprocessing.preprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import joelbits.configs.FileConfig;
import joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import joelbits.model.project.protobuf.ProjectProtos.Project;
import joelbits.model.project.protobuf.ProjectProtos.Revision;
import joelbits.model.project.protobuf.ProjectProtos.Person;
import joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import joelbits.modules.preprocessing.plugins.spi.Connector;

import joelbits.modules.preprocessing.plugins.spi.LanguageParser;
import joelbits.modules.preprocessing.utils.ChangedFileContainer;
import joelbits.modules.preprocessing.utils.NodeExtractor;
import joelbits.modules.preprocessing.utils.ProjectContainer;
import joelbits.utils.FileUtil;
import joelbits.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Pre-processes raw data found in software repositories into a structured form used by the framework.
 */
public abstract class RepositoryPreProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepositoryPreProcessor.class);
    private final ProjectContainer projectContainer = new ProjectContainer();
    private final ChangedFileContainer changedFileContainer = new ChangedFileContainer();
    private final FileConfig fileConfig = new FileConfig();
    private final LanguageParser parser;
    private final Connector connector;
    private final String source;

    RepositoryPreProcessor(LanguageParser parser, Connector connector, String source) {
        this.parser = parser;
        this.connector = connector;
        this.source = source;
    }

    public abstract void process(File projectsMetadata);

    LanguageParser parser() {
        return parser;
    }

    Connector connector() {
        return connector;
    }

    String source() {
        return source;
    }

    Iterator<JsonNode> repositories(File projectsMetadata) throws IOException {
        return FileUtil.getJSONFileIterator(projectsMetadata, fileConfig.repositoryListNode(source));
    }

    public Map<String, byte[]> projects() {
        return projectContainer.projects();
    }

    void clearChangedFileContainer() {
        changedFileContainer.clearSnapshot();
    }

    void addChangedFileSnapshot(String filePath) {
        changedFileContainer.addSnapshotFile(filePath);
    }

    void addChangedFileSnapshots(Set<String> filePaths) {
        changedFileContainer.addSnapshotFiles(filePaths);
    }

    boolean snapshotContains(String key) {
        return changedFileContainer.snapshotContains(key);
    }

    public Map<String, Map<String, byte[]>> changedFiles() {
        return changedFileContainer.benchmarkFilesEvolution();
    }

    byte[] parseFile(String codeRepository, String mostRecentCommitId, String path) throws Exception {
        String fullPath = PathUtil.clonedRepositoriesFolder() + File.separator + codeRepository + File.separator + path;
        connector.checkOutFile(mostRecentCommitId, path);

        return parser.parse(new File(fullPath));
    }

    void addChangedFileEvolution(String fileTableKey, String path, byte[] parsedFile) throws Exception {
        changedFileContainer.addEvolutionFile(fileTableKey, path, parsedFile);
    }

    void addRepositoryToProject(NodeExtractor nodeExtractor, List<Revision> repositoryRevisions) {
        List<CodeRepository> codeRepositories = new ArrayList<>();
        codeRepositories.add(nodeExtractor.createCodeRepository(repositoryRevisions));
        Project project = nodeExtractor.createProject(codeRepositories);

        projectContainer.put(project.getUrl(), project.toByteArray());
    }

    Revision createRevision(String mostRecentCommitId, NodeExtractor nodeExtractor, List<ChangedFile> revisionFiles) {
        Person committer = nodeExtractor.createCommitter(connector().committerName(mostRecentCommitId), connector().committerEmail(mostRecentCommitId));
        int commitTime = connector().commitTime(mostRecentCommitId);
        String log = connector().logMessage(mostRecentCommitId);

        return nodeExtractor.createRevision(mostRecentCommitId, committer, revisionFiles, commitTime, log);
    }

    /**
     * Identifies which files in a set that are changed files and of a type required for the parser.
     *
     * @param filesInRepository     the files in a repository snapshot
     * @return                      the changed files suitable for the parser plugin
     */
    Set<String> retainChangedFilesWithParserType(Set<String> filesInRepository) {
        Set<String> filesToParse = new HashSet<>();

        for (String filePath : filesInRepository) {
            try {
                if (filePath.toLowerCase().endsWith("." + parser.toString().toLowerCase())) {
                    filesToParse.add(filePath);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                System.err.println(e.toString());
            }
        }

        return filesToParse;
    }
}