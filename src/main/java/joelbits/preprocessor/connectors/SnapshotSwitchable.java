package joelbits.preprocessor.connectors;

/**
 * Allows different snapshots (of either a specific file or the whole project) to be checked out from a version
 * control system to be preprocessed.
 */
public interface SnapshotSwitchable {
    void checkOutFile(String commitId, String filePath) throws Exception;
    void checkOutMostRecentRevision() throws Exception;
}
