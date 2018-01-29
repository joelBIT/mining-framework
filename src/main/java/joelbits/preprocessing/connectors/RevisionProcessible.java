package joelbits.preprocessing.connectors;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ability to work with revisions.
 */
public interface RevisionProcessible {
    Map<String, String> changedFilesBetweenCommits(String newCommitId, String oldCommitId) throws Exception;
    Set<String> snapshotFiles(String commitId) throws IOException;
    String mostRecentCommitId();
    List<String> allCommitIds();
}
