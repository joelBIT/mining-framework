package joelbits.modules.preprocessing.utils;

public final class TypeConverter {
    /**
     * Since some sources may have different ChangeTypes than used in the Protocol Buffer, they have to
     * be mapped to corresponding ChangeType.
     *
     * @param type          the ChangeType of the parsed source
     * @return              the ChangeType used in the Project Protocol Buffer message
     */
    public String convertChangeType(String type) {
        switch(type.toUpperCase()) {
            case "MODIFY":
                return "MODIFIED";
            case "ADD":
                return "ADDED";
            case "DELETE":
                return "DELETED";
            default:
                return type.toUpperCase();
        }
    }
}
