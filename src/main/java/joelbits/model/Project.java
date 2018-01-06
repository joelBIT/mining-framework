package joelbits.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level type, represents a single project.
 */
public class Project {
    private String id;                              // Unique identifier for the project
    private String name;                            // The name of the project
    private List<CodeRepository> repositories;      // A list of all code repositories associated with this project
    private String url;                             // A URL to the project's page (e.g., on GitHub)
    private DateTime createdDate;                   // The time the project was created
    private List<String> programmingLanguages;      // A list of all programming languages used by the project

    public Project(String id, String name, List<CodeRepository> repositories, String url, DateTime createdDate, List<String> programmingLanguages) {
        this.id = id;
        this.name = name;
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
