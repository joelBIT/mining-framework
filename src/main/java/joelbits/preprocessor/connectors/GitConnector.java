package joelbits.preprocessor.connectors;

import joelbits.preprocessor.connectors.utils.TreeIterator;
import joelbits.model.project.types.SourceCodeFileType;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Connects to a git repository and allows a client to access its data.
 */
public final class GitConnector implements Connector {
    private static final Logger log = LoggerFactory.getLogger(GitConnector.class);
    private Repository repository;
    private final Map<String, RevCommit> commits = new HashMap<>();
    private final Map<String, List<DiffEntry>> differenceConsecutiveCommits = new HashMap<>();
    private final Map<String, DiffEntry> fileChanges = new HashMap<>();

    @Override
    public void connect(String repositoryName) throws Exception {
        String path = System.getProperty("user.dir") + File.separator + repositoryName + File.separator;
        File repository = new File(path + Constants.DOT_GIT);
        this.repository = new FileRepositoryBuilder()
                .setGitDir(repository)
                .readEnvironment()
                .findGitDir()
                .build();

        collect();
        log.info("Finished collecting data from " + repository.getName());
    }

    private void collect() throws GitAPIException, IOException {
        clearData();
        try (Git git = new Git(repository)) {
            for (RevCommit commit : git.log().call()) {
                String commitId = commit.getId().name();
                commits.put(commitId, commit);
                storeConsecutiveCommitChanges(git, commit);
            }
        }
    }

    private void clearData() {
        commits.clear();
        fileChanges.clear();
        differenceConsecutiveCommits.clear();
    }

    /**
     * Store the changes made between two consecutive commits (i.e., two commits that have a parent/child relationship).
     * If a commit does not have a parent (the root commit) an empty list is stored.
     *
     * @param git                   the git repository
     * @param commit                the object representing the current commit
     * @throws GitAPIException
     * @throws IOException
     */
    private void storeConsecutiveCommitChanges(Git git, RevCommit commit) throws GitAPIException, IOException {
        String commitId = commit.getId().name();
        if (commit.getParentCount() == 0) {
            differenceConsecutiveCommits.put(commitId, Collections.emptyList());
            return;
        }

        List<DiffEntry> diffs = git.diff()
                .setOldTree(TreeIterator.prepareTreeParser(repository, commit.getParent(0).toObjectId().name()))
                .setNewTree(TreeIterator.prepareTreeParser(repository, commitId))
                .call();

        storeEachFileChange(diffs);
        differenceConsecutiveCommits.put(commitId, new ArrayList<>(diffs));
    }

    /**
     * Store a reference from each file change (sha1 value) to its respective DiffEntry object for future processing.
     * Then you can retrieve that DiffEntry in order to extract information such as the new file path, or
     * the type of the change (e.g., ADD, MODIFY, DELETE).
     *
     * @param diffs    list of files that have been subject to changes between two (in this case) consecutive commits
     */
    private void storeEachFileChange(List<DiffEntry> diffs) {
        for (DiffEntry entry : diffs) {
            fileChanges.put(entry.getNewId().name(), entry);
        }
    }

    /**
     *  Between two commits there are files changed, and each of these file changes has its own object (DiffEntry)
     *  containing information about that specific file's changes. This object can be retrieved by using its unique
     *  sha1 identifier value.
     *
     * @param fileChangeId      the sha1 value of a specific file change within a specific commit
     * @return                  the type of the file change, e.g., ADD, DELETE, MODIFY
     */
    @Override
    public String fileChangeType(String fileChangeId) {
        return Optional.ofNullable(fileChanges.get(fileChangeId))
                .map(DiffEntry::getChangeType)
                .map(ChangeType::name)
                .orElse(StringUtils.EMPTY);
    }

    /**
     * Checks out the most recent snapshot of the project. Can be used to restore a cloned project to its
     * initial state after parsing revisions.
     *
     * @throws GitAPIException
     */
    @Override
    public void checkOutMostRecentRevision() throws GitAPIException {
        try (Git git = new Git(repository)) {
            git.checkout().setName(Constants.R_HEADS + Constants.MASTER).call();
        }
    }

    /**
     * Checks out a specific file in a specific revision.
     *
     * @param commitId              the sha1 value of the revision the file is in
     * @param filePath              the file path of the file to check out
     * @throws GitAPIException
     */
    @Override
    public void checkOutFile(String commitId, String filePath) throws GitAPIException {
        try (Git git = new Git(repository)) {
            git.checkout().setStartPoint(commits.get(commitId)).addPath(filePath).call();
        }
    }

    @Override
    public String logMessage(String commitId) {
        return commits.get(commitId).getFullMessage();
    }

    @Override
    public boolean hasParentRevisions(String commitId) {
        return commits.get(commitId).getParentCount() > 0;
    }

    @Override
    public String getParentRevision(String commitId) {
        return Optional.ofNullable(commits.get(commitId))
                .filter(d -> d.getParentCount() > 0)
                .map(d -> d.getParent(0))
                .map(RevCommit::name)
                .orElse(StringUtils.EMPTY);
    }

    @Override
    public String committerName(String commitId) {
        return commits.get(commitId).getCommitterIdent().getName();
    }

    @Override
    public String committerEmail(String commitId) {
        return commits.get(commitId).getCommitterIdent().getEmailAddress();
    }

    @Override
    public int commitTime(String commitId) {
        return commits.get(commitId).getCommitTime();
    }

    /**
     * The ID of the most recent commit made to the repository. This commit is the last in the commit chain (and
     * therefore has no children).
     *
     * @return      the ID of the most recent commit
     */
    @Override
    public String mostRecentCommitId() {
        Comparator<RevCommit> comparator = Comparator.comparing(RevCommit::getCommitTime);
        return commits.isEmpty() ? StringUtils.EMPTY : Collections.max(commits.values(), comparator).getId().name();
    }

    /**
     * The ID of the first commit ever made to the repository, i.e., the oldest commit. This is the root in the commit
     * chain (and therefore have no parent).
     *
     * @return      the ID of the repository's root commit
     */
    @Override
    public String leastRecentCommitId() {
        Comparator<RevCommit> comparator = Comparator.comparing(RevCommit::getCommitTime);
        return commits.isEmpty() ? StringUtils.EMPTY : Collections.min(commits.values(), comparator).getId().name();
    }

    /**
     * Returns the path of the files that have been changed between the two supplied commits, and their respective
     * sha1 values. The sha1 value of a file change is used as key since the sha1 values are unique.
     *
     * @param newCommitId       the ID of the commits that is newest in time
     * @param oldCommitId       the ID of the commits that are oldest in time
     * @return                  a map containing pairs of the sha1 value of a file change and the file path
     */
    @Override
    public Map<String, String> changedFilesBetweenCommits(String newCommitId, String oldCommitId) {
        if (StringUtils.isEmpty(newCommitId) || StringUtils.isEmpty(oldCommitId)) {
            log.warn("Must provide existing commit IDs");
            return Collections.emptyMap();
        }

        if (hasParentRelationship(newCommitId, oldCommitId)) {
            return differenceConsecutiveCommits.get(newCommitId).stream()
                    .filter(file -> hasRelevantChangeType(file.getChangeType()))
                    .filter(file -> hasRelevantFileType(file.getNewPath()))
                    .collect(Collectors.toMap(d -> d.getNewId().name(), DiffEntry::getNewPath));
        }

        return retrieveChangedFiles(newCommitId, oldCommitId);
    }

    private boolean hasRelevantChangeType(ChangeType type) {
        return EnumSet.of(ChangeType.ADD, ChangeType.MODIFY, ChangeType.DELETE).contains(type);
    }

    private boolean hasRelevantFileType(String file) {
        return SourceCodeFileType.exist(file);
    }

    /**
     * If the commits have a parent/child relationship the changes between these commits are already stored in the
     * differenceConsecutiveCommits map. No need to recompute these.
     *
     * @param newCommitId       commit ID of the newer commit
     * @param oldCommitId       commit ID of the older commit
     * @return                  true if parent/child relationship exists, otherwise false
     */
    private boolean hasParentRelationship(String newCommitId, String oldCommitId) {
        return Optional.ofNullable(commits.get(newCommitId))
                .filter(d -> d.getParentCount() > 0)
                .map(RevCommit::getParents)
                .filter(p -> isParent(p, oldCommitId))
                .isPresent();
    }

    private boolean isParent(RevCommit[] parents, String commitId) {
        return Stream.of(parents).anyMatch(p -> p.getId().name().equals(commitId));
    }

    /**
     * If no parent/child relationship exists between the two commits, the changes between these commits have to be
     * computed.
     *
     * @param newCommitId       commit ID of the newer commit
     * @param oldCommitId       commit ID of the older commit
     * @return                  a map containing pairs of the sha1 value of a file change and the file path
     */
    private Map<String, String> retrieveChangedFiles(String newCommitId, String oldCommitId) {
        try (Git git = new Git(repository)) {
            List<DiffEntry> diffs = git.diff()
                    .setOldTree(TreeIterator.prepareTreeParser(repository, oldCommitId))
                    .setNewTree(TreeIterator.prepareTreeParser(repository, newCommitId))
                    .call();

            return diffs.stream()
                    .collect(toMap(d -> d.getNewId().name(), d -> d.getChangeType().name()));
        } catch (GitAPIException | IOException e) {
            log.error(e.toString(), e);
        }

        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        return "GitConnector";
    }
}
