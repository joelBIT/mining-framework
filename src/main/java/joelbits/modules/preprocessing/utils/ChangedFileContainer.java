package joelbits.modules.preprocessing.utils;

import java.util.*;

public final class ChangedFileContainer {
    private final Set<String> changedFilesInNewestSnapshot = new HashSet<>();
    private final Map<String, Map<String, byte[]>> changedFilesEvolution = new HashMap<>();

    public void clearSnapshot() {
        this.changedFilesInNewestSnapshot.clear();
    }

    public boolean snapshotContains(String key) {
        return this.changedFilesInNewestSnapshot.contains(key);
    }

    public void addSnapshotFile(String filePath) {
        this.changedFilesInNewestSnapshot.add(filePath);
    }

    public void addSnapshotFiles(Set<String> filePaths) {
        this.changedFilesInNewestSnapshot.addAll(filePaths);
    }

    public void addEvolutionFile(String fileTableKey, String path, byte[] parsedFile) {
        if (!changedFilesEvolution.containsKey(fileTableKey)) {
            changedFilesEvolution.put(fileTableKey, new HashMap<>());
        }
        changedFilesEvolution.get(fileTableKey).put(path, parsedFile);
    }

    public Map<String, Map<String, byte[]>> benchmarkFilesEvolution() {
        return Collections.synchronizedMap(changedFilesEvolution);
    }
}
