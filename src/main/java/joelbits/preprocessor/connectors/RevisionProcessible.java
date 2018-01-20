package joelbits.preprocessor.connectors;

import java.util.Map;

/**
 * Ability to work with revisions.
 */
public interface RevisionProcessible {
    Map<String, String> changedFilesBetweenCommits(String newCommitId, String oldCommitId);
    String leastRecentCommitId();
    String mostRecentCommitId();
    String getParentRevision(String commitId);
    boolean hasParentRevisions(String commitId);
}
