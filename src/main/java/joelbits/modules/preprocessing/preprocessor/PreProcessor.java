package joelbits.modules.preprocessing.preprocessor;

import java.io.File;
import java.util.Map;

public interface PreProcessor {
    void process(File projectsMetadata);
    Map<String, byte[]> projects();
    Map<String, Map<String, byte[]>> changedBenchmarkFiles();
}