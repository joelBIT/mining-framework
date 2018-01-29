package joelbits.preprocessing.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum InputFileType {
    JSON(".json"),
    CSV(".csv");

    private static final Logger log = LoggerFactory.getLogger(InputFileType.class);
    private final String fileType;

    InputFileType(String fileType) {
        this.fileType = fileType;
    }

    public static boolean isFileOfType(String fileName, InputFileType fileType) {
        return fileName.toLowerCase().endsWith(fileType.fileType);
    }
}