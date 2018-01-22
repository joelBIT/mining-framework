package joelbits.model.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A  single revision in a CodeRepository.
 */
public final class Revision {
    private final String id;                      // A unique identifier for the revision
    private final LocalDateTime CommitDate;       // The CommitDate the revision was committed
    private final Person committer;               // The person who committed the revision
    private final List<ChangedFile> files;        // A list of all files committed in the revision
    private final String log;                     // The log message attached to the revision

    public Revision(String id, LocalDateTime commitDate, Person committer, List<ChangedFile> files, String log) {
        this.id = id;
        this.CommitDate = commitDate;
        this.committer = committer;
        this.files = new ArrayList<>(files);
        this.log = log;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCommitDate() {
        return CommitDate;
    }

    public Person getCommitter() {
        return committer;
    }

    public List<ChangedFile> getFiles() {
        return files;
    }

    public String getLog() {
        return log;
    }
}
