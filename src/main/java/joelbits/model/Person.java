package joelbits.model;

/**
 * A unique person's information.
 */
public final class Person {
    private final String username;            // The person's username
    private final String realName;            // The real name, if known, otherwise same as username
    private final String email;               // The person's email address, if known

    public Person(String username, String email) {
        this(username, username, email);
    }

    public Person(String username, String realName, String email) {
        this.username = username;
        this.realName = realName;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }
}
