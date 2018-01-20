package joelbits.model.project.types;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

public enum SourceCodeFileType {
    BINARY, JAVA, GO, TEXT, XML, JSON , OTHER;

    public static boolean exist(String file) {
        String fileType = FilenameUtils.getExtension(file).toLowerCase();
        return Arrays.stream(SourceCodeFileType.values())
                .map(type -> type.name().toLowerCase())
                .anyMatch(type -> type.equals(fileType));
    }
}
