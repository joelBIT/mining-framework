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
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository.RepositoryType;
import static joelbits.model.project.protobuf.ProjectProtos.Project;
import static joelbits.model.project.protobuf.ProjectProtos.Project.ProjectType;

import joelbits.modules.preprocessing.plugins.spi.MicrobenchmarkParser;
import joelbits.modules.preprocessing.utils.ProjectNodeCreator;
import joelbits.utils.FileUtil;
import joelbits.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Used to pre-process raw data in software repositories.
 */
public final class RepositoryPreProcessor implements PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(RepositoryPreProcessor.class);
    private final Map<String, byte[]> projects = new HashMap<>();
    private final Map<String, Map<String, byte[]>> benchmarkFilesEvolution = new HashMap<>();
    private final Set<String> benchmarkFilesInNewestSnapshot = new HashSet<>();
    private final ProjectNodeCreator projectNodeCreator = new ProjectNodeCreator();
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
                benchmarkFilesInNewestSnapshot.clear();
                JsonNode node = iterator.next();
                String codeRepository = node.get(fileConfig.repositoryFullNameNode(source)).asText();

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
                            if (!benchmarkFilesInNewestSnapshot.contains(changeFile.getKey())) {
                                continue;
                            }

                            String path = changeFile.getKey();
                            String changeType = changeFile.getValue();
                            revisionFiles.add(projectNodeCreator.changedFile(path, changeType));

                            String fileTableKey = node.get(fileConfig.repositoryHtmlUrlNode(source)).asText() + ":" + mostRecentCommitId;
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
                        repositoryRevisions.add(createRevision(mostRecentCommitId, revisionFiles));
                        revisionFiles.clear();
                    }
                }

                addRepositoryToProject(node, repositoryRevisions);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
            System.err.println(e);
        }

        log.info("Finished preprocessing of projects");
        System.out.println("Finished preprocessing of projects");
    }

    private void addRepositoryToProject(JsonNode node, List<Revision> repositoryRevisions) {
        List<CodeRepository> codeRepositories = new ArrayList<>();
        codeRepositories.add(createCodeRepository(node, repositoryRevisions));
        Project project = createProject(node, codeRepositories);

        projects.put(project.getUrl(), project.toByteArray());
    }

    private void addChangedBenchmarkFile(String codeRepository, String mostRecentCommitId, String path, String fileTableKey) throws Exception {
        if (!benchmarkFilesEvolution.containsKey(fileTableKey)) {
            benchmarkFilesEvolution.put(fileTableKey, new HashMap<>());
        }

        benchmarkFilesEvolution.get(fileTableKey).put(path, parseFile(codeRepository, mostRecentCommitId, path));
    }

    /**
     * Identifies which files in a repository's codebase contains benchmarks.
     *
     * @param repositoryName            the repository name
     * @param filesInRepository         all files in a specific snapshot of the repository
     */
    private void identifyBenchmarkFiles(String repositoryName, Set<String> filesInRepository) {
        String path = PathUtil.clonedRepositoriesFolder() + File.separator + repositoryName + File.separator;

        for (String filePath : filesInRepository) {
            try {
                if (filePath.toLowerCase().endsWith("." + parser.toString().toLowerCase()) && parser.hasBenchmarks(new File(path + filePath))) {
                    benchmarkFilesInNewestSnapshot.add(filePath);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                System.err.println(e);
                continue;
            }
        }
        System.out.println(repositoryName + " has " + benchmarkFilesInNewestSnapshot.size() + " files that contains JMH imports");
    }

    private byte[] parseFile(String codeRepository, String mostRecentCommitId, String path) throws Exception {
        String fullPath = PathUtil.clonedRepositoriesFolder() + File.separator + codeRepository + File.separator + path;
        connector.checkOutFile(mostRecentCommitId, path);

        return parser.parse(new File(fullPath));
    }

    private Revision createRevision(String mostRecentCommitId, List<ChangedFile> revisionFiles) {
        Person committer = createCommitter(mostRecentCommitId);
        int commitTime = connector.commitTime(mostRecentCommitId);
        String log = connector.logMessage(mostRecentCommitId);

        return projectNodeCreator.createRevision(mostRecentCommitId, revisionFiles, committer, commitTime, log);
    }

    private Person createCommitter(String mostRecentCommitId) {
        return projectNodeCreator.committer(connector.committerName(mostRecentCommitId), connector.committerEmail(mostRecentCommitId));
    }

    private CodeRepository createCodeRepository(JsonNode node, List<Revision> protosRevisions) {
        return projectNodeCreator.repository(node.get(fileConfig.repositoryHtmlUrlNode(source)).asText(), RepositoryType.GIT, protosRevisions);
    }

    private Project createProject(JsonNode node, List<CodeRepository> codeRepositories) {
        int creationTime = Math.toIntExact(OffsetDateTime.parse(node.get(fileConfig.repositoryCreatedAtNode(source)).asText()).toEpochSecond());
        String id = node.get(fileConfig.repositoryIdNode(source)).asText();
        String name = node.get(fileConfig.repositoryNameNode(source)).asText();
        String url = node.get(fileConfig.repositoryUrlNode(source)).asText();
        String language = node.get(fileConfig.repositoryLanguageNode(source)).asText();
        int forks = node.get(fileConfig.repositoryForksNode(source)).asInt();
        int watchers = node.get(fileConfig.repositoryWatchersNode(source)).asInt();

        return projectNodeCreator.project(name, creationTime, id, url, ProjectType.GITHUB, codeRepositories, Collections.singletonList(language), forks, watchers);
    }

    @Override
    public Map<String, byte[]> projects() {
        return projects;
    }

    @Override
    public Map<String, Map<String, byte[]>> changedBenchmarkFiles() {
        return benchmarkFilesEvolution;
    }
}