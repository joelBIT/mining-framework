package joelbits.model.project;

import joelbits.model.project.types.ChangeType;
import joelbits.model.types.FileType;

/**
 * A file committed in a Revision.
 */
public final class ChangedFile {
    private final String name;                // The full name and path of the file
    private final ChangeType change;          // The type of change for this file
    private final FileType type;              // The type of file

    public ChangedFile(String name, ChangeType change, FileType type) {
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

    public FileType getType() {
        return type;
    }
}
