/**
 * Represents a guest who is making a reservation.
 */
public class Guest {
    private final String name;
    private final String contactNumber;
    private final String email;

    public Guest(String name, String contactNumber, String email) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return name + " (" + contactNumber + ", " + email + ")";
    }
}
