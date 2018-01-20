package joelbits.preprocessor.types;

/**
 * Different sources may need different PreProcessors since the unstructed data at the sources probably differ. By
 * differentiating between sources the relevant PreProcessor may be used to output the structured data.
 */
public enum SourceType {
    GITHUB("github"),
    BITBUCKET("bitbucket"),
    SOURCEFORGE("sourceforge"),
    OTHER("other");

    private final String type;

    SourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}