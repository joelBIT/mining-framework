package joelbits.preprocessor.types;

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