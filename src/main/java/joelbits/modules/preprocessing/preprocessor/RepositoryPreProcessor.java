package joelbits.modules.preprocessing.preprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import joelbits.configs.FileConfig;
import joelbits.modules.preprocessing.plugins.spi.Connector;
import static joelbits.model.project.protobuf.ProjectProtos.Person;
import static joelbits.model.project.protobuf.ProjectProtos.Revision;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import static joelbits.model.project.protobuf.ProjectProtos.Project;

import joelbits.modules.preprocessing.plugins.spi.MicrobenchmarkParser;
import joelbits.modules.preprocessing.utils.BenchmarkContainer;
import joelbits.modules.preprocessing.utils.NodeExtractor;
import joelbits.modules.preprocessing.utils.ProjectContainer;
import joelbits.utils.FileUtil;
import joelbits.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Used to pre-process raw data in software repositories.
 */
public final class RepositoryPreProcessor implements PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepositoryPreProcessor.class);
    private final ProjectContainer projectContainer = new ProjectContainer();
    private final BenchmarkContainer benchmarkContainer = new BenchmarkContainer();
    private final FileConfig fileConfig = new FileConfig();
    private final MicrobenchmarkParser parser;
    private final Connector connector;
    private final String source;

    public RepositoryPreProcessor(MicrobenchmarkParser parser, Connector connector, String source) {
        this.parser = parser;
        this.connector = connector;
        this.source = source;
    }

    /**
     * Each project in the metadata file must be identical to corresponding repository in the input repository list.
     *
     * @param projectsMetadata
     */
    @Override
    public void process(File projectsMetadata) {
        try {
            Iterator<JsonNode> iterator = FileUtil.getJSONFileIterator(projectsMetadata, fileConfig.repositoryListNode(source));
            while (iterator.hasNext()) {
                benchmarkContainer.clearSnapshot();
                NodeExtractor nodeExtractor = new NodeExtractor(iterator.next(), source);
                String codeRepository = nodeExtractor.codeRepository();

                try {
                    connector.connect(codeRepository);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    System.err.println(e);
                    continue;
                }

                List<Revision> repositoryRevisions = new ArrayList<>();
                List<ChangedFile> revisionFiles = new ArrayList<>();

                try {
                    identifyBenchmarkFiles(codeRepository, connector.snapshotFiles(connector.mostRecentCommitId()));
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    System.err.println(e);
                }

                List<String> allCommitIds = connector.allCommitIds();
                System.out.println(codeRepository + " has " + allCommitIds.size() + " revisions");
                PeekingIterator<String> commitIterator = Iterators.peekingIterator(allCommitIds.iterator());
                while (commitIterator.hasNext()) {
                    String mostRecentCommitId = commitIterator.next();
                    if (!commitIterator.hasNext()) {
                        break;
                    }

                    try {
                        Map<String, String> changedFiles = connector.getCommitFileChanges(mostRecentCommitId);
                        for (Map.Entry<String, String> changeFile : changedFiles.entrySet()) {
                            if (!benchmarkContainer.snapshotContains(changeFile.getKey())) {
                                continue;
                            }

                            String path = changeFile.getKey();
                            String changeType = changeFile.getValue();
                            revisionFiles.add(nodeExtractor.createChangedFile(path, changeType));

                            String fileTableKey = nodeExtractor.fileTableKey(mostRecentCommitId);
                            try {
                                addChangedBenchmarkFile(codeRepository, mostRecentCommitId, path, fileTableKey);
                            } catch (Exception e1) {
                                log.error(e1.toString(), e1);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    if (!revisionFiles.isEmpty()) {
                        repositoryRevisions.add(createRevision(mostRecentCommitId, nodeExtractor, revisionFiles));
                        revisionFiles.clear();
                    }
                }

                addRepositoryToProject(nodeExtractor, repositoryRevisions);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
            System.err.println(e);
        }

        log.info("Finished preprocessing of projects");
        System.out.println("Finished preprocessing of projects");
    }

    private void addRepositoryToProject(NodeExtractor nodeExtractor, List<Revision> repositoryRevisions) {
        List<CodeRepository> codeRepositories = new ArrayList<>();
        codeRepositories.add(nodeExtractor.createCodeRepository(repositoryRevisions));
        Project project = nodeExtractor.createProject(codeRepositories);

        projectContainer.put(project.getUrl(), project.toByteArray());
    }

    private void addChangedBenchmarkFile(String codeRepository, String mostRecentCommitId, String path, String fileTableKey) throws Exception {
        byte[] parsedFile = parseFile(codeRepository, mostRecentCommitId, path);
        benchmarkContainer.addEvolutionFile(fileTableKey, path, parsedFile);
    }

    /**
     * Identifies which files in a repository's codebase snapshotContains benchmarks.
     *
     * @param repositoryName            the repository name
     * @param filesInRepository         all files in a specific snapshot of the repository
     */
    private void identifyBenchmarkFiles(String repositoryName, Set<String> filesInRepository) {
        String path = PathUtil.clonedRepositoriesFolder() + File.separator + repositoryName + File.separator;

        for (String filePath : filesInRepository) {
            try {
                if (filePath.toLowerCase().endsWith("." + parser.toString().toLowerCase()) && parser.hasBenchmarks(new File(path + filePath))) {
                    benchmarkContainer.addSnapshotFile(filePath);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                System.err.println(e);
                continue;
            }
        }
    }

    private byte[] parseFile(String codeRepository, String mostRecentCommitId, String path) throws Exception {
        String fullPath = PathUtil.clonedRepositoriesFolder() + File.separator + codeRepository + File.separator + path;
        connector.checkOutFile(mostRecentCommitId, path);

        return parser.parse(new File(fullPath));
    }

    private Revision createRevision(String mostRecentCommitId, NodeExtractor nodeExtractor, List<ChangedFile> revisionFiles) {
        Person committer = nodeExtractor.createCommitter(connector.committerName(mostRecentCommitId), connector.committerEmail(mostRecentCommitId));
        int commitTime = connector.commitTime(mostRecentCommitId);
        String log = connector.logMessage(mostRecentCommitId);

        return nodeExtractor.createRevision(mostRecentCommitId, committer, revisionFiles, commitTime, log);
    }

    @Override
    public Map<String, byte[]> projects() {
        return projectContainer.projects();
    }

    @Override
    public Map<String, Map<String, byte[]>> changedBenchmarkFiles() {
        return benchmarkContainer.benchmarkFilesEvolution();
    }
}