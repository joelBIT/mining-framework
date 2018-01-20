package joelbits.model.project;

import joelbits.model.project.types.ChangeType;
import joelbits.model.project.types.SourceCodeFileType;

/**
 * A file committed in a Revision.
 */
public final class ChangedFile {
    private final String name;                // The full name and path of the file
    private final ChangeType change;          // The type of change for this file
    private final SourceCodeFileType type;              // The type of file

    public ChangedFile(String name, ChangeType change, SourceCodeFileType type) {
        this.name = name;
        this.change = change;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ChangeType getChange() {
        return change;
    }

    public SourceCodeFileType getType() {
        return type;
    }
}
