package joelbits.preprocessor.utils;

import com.google.protobuf.Timestamp;
import joelbits.model.project.protobuf.ProjectProtos.ChangedFile;
import joelbits.model.project.protobuf.ProjectProtos.Revision;
import joelbits.model.project.protobuf.ProjectProtos.Person;
import joelbits.model.project.protobuf.ProjectProtos.Project;
import joelbits.model.project.protobuf.ProjectProtos.Project.ProjectType;
import joelbits.model.project.protobuf.ProjectProtos.CodeRepository;
import joelbits.model.project.protobuf.ProjectProtos.CodeRepository.RepositoryType;

import java.util.List;

public final class ProjectNodeCreator {
    public static Revision createRevision(String revisionId, List<ChangedFile> revisionFiles, Person committer, int commitTime, String log) {
        return Revision.newBuilder()
                .setCommitDate(timestamp(commitTime))
                .setId(revisionId)
                .setCommitter(committer)
                .setLog(log)
                .addAllFiles(revisionFiles)
                .build();
    }

    private static Timestamp timestamp(int seconds) {
        return Timestamp.newBuilder().setSeconds(seconds).build();
    }

    public static Person committer(String username, String email) {
        return committer(username, username, email);
    }

    public static Person committer(String username, String realName, String email) {
        return Person.newBuilder()
                .setEmail(email)
                .setUsername(username)
                .setRealName(realName)
                .build();
    }

    public static Project project(String name, int createdTime, String id, String url, ProjectType type, List<CodeRepository> repositories, List<String> languages) {
        return Project.newBuilder()
                .setId(id)
                .setName(name)
                .setType(type)
                .setUrl(url)
                .setCreatedDate(timestamp(createdTime))
                .addAllRepositories(repositories)
                .addAllProgrammingLanguages(languages)
                .build();
    }

    public static CodeRepository repository(String url, RepositoryType type, List<Revision> revisions) {
        return CodeRepository.newBuilder()
                .setUrl(url)
                .setType(type)
                .addAllRevision(revisions)
                .build();
    }

    public static ChangedFile changedFile(String name, String fileType, String changeType) {
        return ChangedFile.newBuilder()
                .setName(name)
                .setType(ChangedFile.FileType.valueOf(fileType.toUpperCase()))
                .setChange(ChangedFile.ChangeType.valueOf(TypeConverter.convertChangeType(changeType)))
                .build();
    }
}
