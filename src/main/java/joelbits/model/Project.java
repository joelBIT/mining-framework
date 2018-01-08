package joelbits.model;

import joelbits.model.types.ProjectType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level type, represents a single project.
 */
public final class Project {
    private final String id;                              // Unique identifier for the project
    private final String name;                            // The name of the project
    private final ProjectType type;                       // The type (source) of the project
    private final List<CodeRepository> repositories;      // A list of all code repositories associated with this project
    private final String url;                             // A URL to the project's page (e.g., on GitHub)
    private final DateTime createdDate;                   // The time the project was created
    private final List<String> programmingLanguages;      // A list of all programming languages used by the project

    public Project(String id, String name, ProjectType type, List<CodeRepository> repositories, String url, DateTime createdDate, List<String> programmingLanguages) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.repositories = new ArrayList<>(repositories);
        this.url = url;
        this.createdDate = createdDate;
        this.programmingLanguages = new ArrayList<>(programmingLanguages);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProjectType getType() { return type; }

    public List<CodeRepository> getRepositories() {
        return repositories;
    }

    public String getUrl() {
        return url;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public List<String> getProgrammingLanguages() {
        return programmingLanguages;
    }
}
