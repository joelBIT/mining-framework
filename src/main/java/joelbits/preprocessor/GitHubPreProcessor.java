package joelbits.preprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import joelbits.preprocessor.connectors.Connector;
import static joelbits.model.project.protobuf.ProjectProtos.Person;
import static joelbits.model.project.protobuf.ProjectProtos.Revision;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository.RepositoryType;
import static joelbits.model.project.protobuf.ProjectProtos.Project;
import static joelbits.model.project.protobuf.ProjectProtos.Project.ProjectType;

import joelbits.preprocessor.parsers.Parser;
import joelbits.preprocessor.utils.FileRepositoryExtractor;
import joelbits.preprocessor.utils.ProjectNodeCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Used to pre-process unstructured data found at the hosting service GitHub.
 */
public final class GitHubPreProcessor implements PreProcessor {
    private static final Logger log = LoggerFactory.getLogger(GitHubPreProcessor.class);
    private final Map<String, byte[]> projects = new HashMap<>();
    private final Map<String, Map<String, byte[]>> benchmarkFilesEvolution = new HashMap<>();
    private final Set<String> benchmarkFilesInNewestSnapshot = new HashSet<>();

    @Inject
    @Named("java")
    private Parser javaParser;
    @Inject
    @Named("git")
    private Connector gitConnector;

    public GitHubPreProcessor() {
        Guice.createInjector(new InjectionModule()).injectMembers(this);
    }

    /**
     * Each project in the metadata file must be identical to corresponding repository in the input repository list.
     *
     * @param projectsMetadata
     */
    @Override
    public void preprocess(File projectsMetadata) {
        try {
            Iterator<JsonNode> iterator = FileRepositoryExtractor.getRepositoryIterator(projectsMetadata, "items");
            while (iterator.hasNext()) {
                JsonNode node = iterator.next();
                String codeRepository = node.get("full_name").asText();
                try {
                    gitConnector.connect(codeRepository);
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    continue;
                }

                List<Revision> repositoryRevisions = new ArrayList<>();
                List<ChangedFile> revisionFiles = new ArrayList<>();

                try {
                    identifyBenchmarkFiles(codeRepository, gitConnector.snapshotFiles(gitConnector.mostRecentCommitId()));
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }

                List<String> allCommitIds = gitConnector.allCommitIds();
                PeekingIterator<String> commitIterator = Iterators.peekingIterator(allCommitIds.iterator());
                while (commitIterator.hasNext()) {
                    String mostRecentCommitId = commitIterator.next();
                    if (!commitIterator.hasNext()) {
                        break;
                    }

                    try {
                        Map<String, String> changedFiles = gitConnector.changedFilesBetweenCommits(mostRecentCommitId, commitIterator.peek());
                        for (Map.Entry<String, String> changeFile : changedFiles.entrySet()) {
                            if (!benchmarkFilesInNewestSnapshot.contains(changeFile.getKey())) {
                                continue;
                            }

                            String path = changeFile.getKey();
                            String changeType = changeFile.getValue();
                            revisionFiles.add(ProjectNodeCreator.changedFile(path, changeType));

                            String fileTableKey = node.get("url").asText() + ":" + mostRecentCommitId;
                            try {
                                addChangedBenchmarkFile(codeRepository, mostRecentCommitId, path, fileTableKey);
                            } catch (Exception e1) {
                                log.error(e1.toString(), e1);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    repositoryRevisions.add(createRevision(mostRecentCommitId, revisionFiles));
                    revisionFiles.clear();
                }

                addRepositoryToProject(node, repositoryRevisions);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        log.info("Finished preprocessing of projects");
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
        String path = System.getProperty("user.dir") + File.separator + repositoryName + File.separator;

        for (String filePath : filesInRepository) {
            try {
                if (filePath.toLowerCase().endsWith(".java") && javaParser.hasBenchmarks(new File(path + filePath))) {
                    benchmarkFilesInNewestSnapshot.add(filePath);
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
                continue;
            }
        }
    }

    private byte[] parseFile(String codeRepository, String mostRecentCommitId, String path) throws Exception {
        String fullPath = System.getProperty("user.dir") + File.separator + codeRepository + File.separator + path;
        gitConnector.checkOutFile(mostRecentCommitId, path);

        return javaParser.parse(new File(fullPath));
    }

    private Revision createRevision(String mostRecentCommitId, List<ChangedFile> revisionFiles) {
        Person committer = createCommitter(mostRecentCommitId);
        int commitTime = gitConnector.commitTime(mostRecentCommitId);
        String log = gitConnector.logMessage(mostRecentCommitId);

        return ProjectNodeCreator.createRevision(mostRecentCommitId, revisionFiles, committer, commitTime, log);
    }

    private Person createCommitter(String mostRecentCommitId) {
        return ProjectNodeCreator.committer(gitConnector.committerName(mostRecentCommitId), gitConnector.committerEmail(mostRecentCommitId));
    }

    private CodeRepository createCodeRepository(JsonNode node, List<Revision> protosRevisions) {
        return ProjectNodeCreator.repository(node.get("html_url").asText(), RepositoryType.GIT, protosRevisions);
    }

    private Project createProject(JsonNode node, List<CodeRepository> codeRepositories) {
        int creationTime = Math.toIntExact(OffsetDateTime.parse(node.get("created_at").asText()).toEpochSecond());
        String id = node.get("id").asText();
        String name = node.get("name").asText();
        String url = node.get("url").asText();
        String language = node.get("language").asText();
        int forks = node.get("forks").asInt();
        int watchers = node.get("watchers").asInt();

        return ProjectNodeCreator.project(name, creationTime, id, url, ProjectType.GITHUB, codeRepositories, Collections.singletonList(language), forks, watchers);
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