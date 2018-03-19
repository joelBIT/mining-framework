package joelbits.modules.preprocessing.utils;

import com.fasterxml.jackson.databind.JsonNode;
import joelbits.configs.FileConfig;

import static joelbits.model.project.protobuf.ProjectProtos.Project;
import static joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import static joelbits.model.project.protobuf.ProjectProtos.Person;
import static joelbits.model.project.protobuf.ProjectProtos.Revision;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import static joelbits.model.project.protobuf.ProjectProtos.CodeRepository.RepositoryType;
import static joelbits.model.project.protobuf.ProjectProtos.Project.ProjectType;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public final class NodeExtractor {
    private final FileConfig fileConfig = new FileConfig();
    private final ProjectNodeCreator projectNodeCreator = new ProjectNodeCreator();
    private final String source;
    private final JsonNode node;

    public NodeExtractor(JsonNode node, String source) {
        this.node = node;
        this.source = source;
    }

    public Project createProject(List<CodeRepository> codeRepositories) {
        int creationTime = Math.toIntExact(OffsetDateTime.parse(node.get(fileConfig.repositoryCreatedAtNode(source)).asText()).toEpochSecond());
        String id = node.get(fileConfig.repositoryIdNode(source)).asText();
        String name = node.get(fileConfig.repositoryNameNode(source)).asText();
        String url = node.get(fileConfig.repositoryUrlNode(source)).asText();
        String language = node.get(fileConfig.repositoryLanguageNode(source)).asText();
        int forks = node.get(fileConfig.repositoryForksNode(source)).asInt();
        int watchers = node.get(fileConfig.repositoryWatchersNode(source)).asInt();

        return projectNodeCreator.project(name, creationTime, id, url, ProjectType.GITHUB, codeRepositories, Collections.singletonList(language), forks, watchers);
    }

    public CodeRepository createCodeRepository(List<Revision> protosRevisions) {
        return projectNodeCreator.repository(node.get(fileConfig.repositoryHtmlUrlNode(source)).asText(), RepositoryType.GIT, protosRevisions);
    }

    public Revision createRevision(String mostRecentCommitId, Person committer, List<ChangedFile> revisionFiles, int commitTime, String log) {
        return projectNodeCreator.createRevision(mostRecentCommitId, revisionFiles, committer, commitTime, log);
    }

    public Person createCommitter(String committerName, String committerEmail) {
        return projectNodeCreator.committer(committerName, committerEmail);
    }

    public String codeRepository() {
        return node.get(fileConfig.repositoryFullNameNode(source)).asText();
    }

    public String fileTableKey(String mostRecentCommitId) {
        return node.get(fileConfig.repositoryHtmlUrlNode(source)).asText() + ":" + mostRecentCommitId;
    }

    public ChangedFile createChangedFile(String path, String changeType) {
        return projectNodeCreator.changedFile(path, changeType);
    }
}
