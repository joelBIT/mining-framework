package joelbits.preprocessor;

import com.fasterxml.jackson.databind.JsonNode;
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
    private final Map<String, Map<String, List<byte[]>>> benchmarkFilesEvolution = new HashMap<>();
    private final Set<String> benchmarkFiles = new HashSet<>();

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
     * Each projects in the metadata file must be identical to corresponding repository in the input repository list.
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
                String mostRecentCommitId = gitConnector.mostRecentCommitId();
                identifyBenchmarkFiles(codeRepository, gitConnector.snapshotFiles(mostRecentCommitId));

                while (gitConnector.hasParentRevisions(mostRecentCommitId)) {
                    String parentRevision = gitConnector.getParentRevision(mostRecentCommitId);
                    List<ChangedFile> revisionFiles = new ArrayList<>();

                    Map<String, String> changedFiles = gitConnector.changedFilesBetweenCommits(mostRecentCommitId, parentRevision);
                    for (Map.Entry<String, String> changeFile : changedFiles.entrySet()) {
                        if (!benchmarkFiles.contains(changeFile.getValue())) {
                            continue;
                        }

                        String path = changeFile.getValue();
                        String changeType = gitConnector.fileChangeType(changeFile.getKey());
                        revisionFiles.add(ProjectNodeCreator.changedFile(path, changeType));

                        String fileTableKey = node.get("url").asText() + ":" + mostRecentCommitId;
                        try {
                            addChangedBenchmarkFile(codeRepository, mostRecentCommitId, path, fileTableKey);
                        } catch (Exception e1) {
                            log.error(e1.toString(), e1);
                            continue;
                        }
                    }

                    repositoryRevisions.add(createRevision(mostRecentCommitId, revisionFiles));
                    mostRecentCommitId = parentRevision;
                }
                List<CodeRepository> codeRepositories = new ArrayList<>();
                codeRepositories.add(createCodeRepository(node, repositoryRevisions));
                Project project = createProject(node, codeRepositories);

                projects.put(project.getUrl(), project.toByteArray());
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        log.info("Finished preprocessing of projects");
    }

    private void addChangedBenchmarkFile(String codeRepository, String mostRecentCommitId, String path, String fileTableKey) throws Exception {
        if (!benchmarkFilesEvolution.containsKey(fileTableKey)) {
            benchmarkFilesEvolution.put(fileTableKey, new HashMap<>());
        }
        if (!benchmarkFilesEvolution.get(fileTableKey).containsKey(path)) {
            benchmarkFilesEvolution.get(fileTableKey).put(path, new ArrayList<>());
        }

        benchmarkFilesEvolution.get(fileTableKey).get(path).add(parseFile(codeRepository, mostRecentCommitId, path));
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
                    benchmarkFiles.add(filePath);
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

        return ProjectNodeCreator.project(name, creationTime, id, url, ProjectType.GITHUB, codeRepositories, Collections.singletonList(language));
    }

    @Override
    public Map<String, byte[]> projects() {
        return projects;
    }

    @Override
    public Map<String, Map<String, List<byte[]>>> changedBenchmarkFiles() {
        return benchmarkFilesEvolution;
    }
}