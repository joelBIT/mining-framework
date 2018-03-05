package joelbits.analysis.converters;

import com.google.protobuf.InvalidProtocolBufferException;
import joelbits.model.project.*;
import joelbits.model.project.protobuf.ProjectProtos;
import joelbits.model.project.types.ChangeType;
import joelbits.model.project.types.ProjectType;
import joelbits.model.project.types.RepositoryType;
import joelbits.model.project.types.SourceCodeFileType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Maps a Project protocol buffer message into its Project model representation.
 */
public class ProjectConverter {

    public static Project convert(byte[] project) throws InvalidProtocolBufferException {
        ProjectProtos.Project parsedProject = ProjectProtos.Project.parseFrom(project);

        List<CodeRepository> repositories = new ArrayList<>();
        for (ProjectProtos.CodeRepository repository : parsedProject.getRepositoriesList()) {
            repositories.add(convertRepository(repository));
        }

        ProjectType type = ProjectType.valueOf(parsedProject.getType().name());
        Set<String> languages = new HashSet<>(parsedProject.getProgrammingLanguagesList());
        LocalDateTime createdAt = getLocalDateTime(parsedProject.getCreatedDate().getSeconds());
        int watchers = parsedProject.getWatchers();
        int forks = parsedProject.getForks();

        return new Project(parsedProject.getId(), parsedProject.getName(), type, repositories, parsedProject.getUrl(), createdAt, languages, forks, watchers);
    }

    private static CodeRepository convertRepository(ProjectProtos.CodeRepository repository) {
        RepositoryType type = RepositoryType.valueOf(repository.getType().name());

        List<Revision> revisions = new ArrayList<>();
        for (ProjectProtos.Revision revision : repository.getRevisionsList()) {
            revisions.add(convertRevision(revision));
        }

        return new CodeRepository(repository.getUrl(), type, revisions);
    }

    private static Revision convertRevision(ProjectProtos.Revision revision) {
        LocalDateTime commitDate = getLocalDateTime(revision.getCommitDate().getSeconds());
        Person committer = convertPerson(revision.getCommitter());

        List<ChangedFile> changedFiles = new ArrayList<>();
        for (ProjectProtos.ChangedFile file : revision.getFilesList()) {
            changedFiles.add(convertChangedFile(file));
        }

        return new Revision(revision.getId(), commitDate, committer, changedFiles, revision.getLog());
    }

    private static LocalDateTime getLocalDateTime(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static Person convertPerson(ProjectProtos.Person person) {
        return new Person(person.getUsername(), person.getEmail());
    }

    private static ChangedFile convertChangedFile(ProjectProtos.ChangedFile file) {
        ChangeType changeType = ChangeType.valueOf(file.getChange().name());
        SourceCodeFileType type = SourceCodeFileType.valueOf(file.getType().name());

        return new ChangedFile(file.getName(), changeType, type);
    }
}
