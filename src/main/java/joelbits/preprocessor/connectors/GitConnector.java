package joelbits.preprocessor.connectors;

import joelbits.model.project.types.SourceCodeFileType;
import joelbits.preprocessor.connectors.utils.TreeIterator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Connects to a git repository and allows a client to access its data.
 */
public final class GitConnector implements Connector {
    private static final Logger log = LoggerFactory.getLogger(GitConnector.class);
    private Repository repository;
    private final Map<String, RevCommit> commits = new HashMap<>();
    private final List<String> allCommitIds = new ArrayList<>();

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
                allCommitIds.add(commitId);
            }
        }
    }

    private void clearData() {
        commits.clear();
        allCommitIds.clear();
    }

    /**
     * Retrieve file paths for all files in a project snapshot.
     *
     * @param commitId              revision sha1 for repository snapshot
     * @return                      a set of file paths for all files in repository snapshot
     * @throws IOException
     */
    @Override
    public Set<String> snapshotFiles(String commitId) throws IOException {
        Set<String> filePaths = new HashSet<>();

        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.reset(commits.get(commitId).getTree());
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                filePaths.add(treeWalk.getPathString());
            }
            treeWalk.close();
        }

        return filePaths;
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

    @Override
    public List<String> allCommitIds() {
        return allCommitIds;
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
    public Map<String, String> changedFilesBetweenCommits(String newCommitId, String oldCommitId) throws Exception {
        try (Git git = new Git(repository)) {
            List<DiffEntry> diffs = git.diff()
                    .setOldTree(TreeIterator.prepareTreeParser(repository, oldCommitId))
                    .setNewTree(TreeIterator.prepareTreeParser(repository, newCommitId))
                    .call();

            return diffs.stream()
                    .filter(file -> hasRelevantFileType(file.getNewPath()))
                    .filter(file -> hasRelevantChangeType(file.getChangeType()))
                    .collect(toMap(DiffEntry::getNewPath, file -> file.getChangeType().name()));
        } catch (GitAPIException | IOException e) {
            log.error(e.toString(), e);
        }

        return Collections.emptyMap();
    }

    private boolean hasRelevantChangeType(ChangeType type) {
        return EnumSet.of(ChangeType.ADD, ChangeType.MODIFY, ChangeType.DELETE).contains(type);
    }

    private boolean hasRelevantFileType(String file) {
        return SourceCodeFileType.exist(file);
    }

    @Override
    public String toString() {
        return "GitConnector";
    }
}
