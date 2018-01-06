package joelbits.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * A  single revision in a CodeRepository.
 */
public class Revision {
    private String id;                      // A unique identifier for the revision
    private DateTime time;                  // The time the revision was committed
    private Person committer;               // The person who committed the revision
    private List<ChangedFile> files;        // A list of all files committed in the revision
    private String log;                     // The log message attached to the revision

    public Revision(String id, DateTime time, Person committer, List<ChangedFile> files, String log) {
        this.id = id;
        this.time = time;
        this.committer = committer;
        this.files = new ArrayList<>(files);
        this.log = log;
    }

    public String getId() {
        return id;
    }

    public DateTime getTime() {
        return time;
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
