package joelbits.modules.preprocessing.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProjectContainer {
    private final Map<String, byte[]> projects = new HashMap<>();

    public void put(String url, byte[] project) {
        this.projects.put(url, project);
    }

    public Map<String, byte[]> projects() {
        return Collections.synchronizedMap(projects);
    }
}
