package joelbits.preprocessor.connectors;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Ability to work with revisions.
 */
public interface RevisionProcessible {
    Map<String, String> changedFilesBetweenCommits(String newCommitId, String oldCommitId) throws Exception;
    Set<String> snapshotFiles(String commitId) throws IOException;
    String leastRecentCommitId();
    String mostRecentCommitId();
    String getParentRevision(String commitId);
    boolean hasParentRevisions(String commitId);
}
