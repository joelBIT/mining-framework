package joelbits.model.project;

import joelbits.model.project.Revision;
import joelbits.model.project.types.RepositoryType;

import java.util.ArrayList;
import java.util.List;

/**
 * A source code repository (Git, SVN, CVS, etc).
 */
public final class CodeRepository {
    private final String url;                         // The URL to access the code repository
    private final RepositoryType type;                // The type of code repository (SVN, GIT, etc)
    private final List<Revision> revisions;           // All of the revisions contained in the code repository

    public CodeRepository(String url, RepositoryType type, List<Revision> revisions) {
        this.url = url;
        this.type = type;
        this.revisions = new ArrayList<>(revisions);
    }

    public String getUrl() {
        return url;
    }

    public RepositoryType getType() {
        return type;
    }

    public List<Revision> getRevisions() {
        return revisions;
    }
}
