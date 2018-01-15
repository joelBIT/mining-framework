package joelbits.model.project.types;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

public enum FileType {
    BINARY, JAVA, GO, TEXT, XML, JSON , OTHER;

    public static boolean exist(String file) {
        String fileType = FilenameUtils.getExtension(file).toLowerCase();
        return Arrays.stream(FileType.values())
                .map(type -> type.name().toLowerCase())
                .anyMatch(type -> type.equals(fileType));
    }
}
