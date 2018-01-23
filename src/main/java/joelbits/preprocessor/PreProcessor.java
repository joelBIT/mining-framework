package joelbits.preprocessor;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PreProcessor {
    void preprocess(File projectsMetadata);
    Map<String, byte[]> projects();
    Map<String, Map<String, byte[]>> changedBenchmarkFiles();
}