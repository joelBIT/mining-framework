package joelbits.modules.preprocessing.utils;

import java.util.*;

public final class BenchmarkContainer {
    private final Set<String> benchmarkFilesInNewestSnapshot = new HashSet<>();
    private final Map<String, Map<String, byte[]>> benchmarkFilesEvolution = new HashMap<>();

    public void clearSnapshot() {
        this.benchmarkFilesInNewestSnapshot.clear();
    }

    public boolean snapshotContains(String key) {
        return this.benchmarkFilesInNewestSnapshot.contains(key);
    }

    public void addSnapshotFile(String filePath) {
        this.benchmarkFilesInNewestSnapshot.add(filePath);
    }

    public void addEvolutionFile(String fileTableKey, String path, byte[] parsedFile) {
        if (!benchmarkFilesEvolution.containsKey(fileTableKey)) {
            benchmarkFilesEvolution.put(fileTableKey, new HashMap<>());
        }
        benchmarkFilesEvolution.get(fileTableKey).put(path, parsedFile);
    }

    public Map<String, Map<String, byte[]>> benchmarkFilesEvolution() {
        return Collections.synchronizedMap(benchmarkFilesEvolution);
    }
}
