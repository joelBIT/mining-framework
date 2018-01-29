package joelbits.utils;

import java.io.File;

public class FrameworkUtil {
    private static String PATH = System.getProperty("user.dir") + File.separator;
    private static String CHANGED_FILES = System.getProperty("user.dir") + File.separator + "benchmarkFiles" + File.separator;
    private static String PROJECTS = System.getProperty("user.dir") + File.separator + "projects" + File.separator;

    public static String jarPath() {
        return PATH;
    }

    public static String projectSequenceFile() {
        return PROJECTS;
    }

    public static String benchmarksMapFile() {
        return CHANGED_FILES;
    }
}
