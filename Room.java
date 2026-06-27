/**
 * Represents a single hotel room.
 * Demonstrates encapsulation - all fields are private with public accessors.
 */
public class Room {
    private final int roomNumber;
    private final RoomCategory category;
    private boolean available;

    public Room(int roomNumber, RoomCategory category, boolean available) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.available = available;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getPricePerNight() {
        return category.getBasePricePerNight();
    }

    @Override
    public String toString() {
        return String.format("Room #%-4d | %-8s | Rs.%-8.2f/night | %s",
                roomNumber, category, getPricePerNight(),
                available ? "AVAILABLE" : "BOOKED");
    }
}
