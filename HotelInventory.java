import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business-logic class of the system.
 * Manages the room inventory and all reservations, and coordinates
 * with FileStorageManager for persistence and PaymentSimulator for payments.
 */
public class HotelInventory {
    private List<Room> rooms;
    private Map<String, Reservation> reservations;
    private final FileStorageManager storageManager;
    private int reservationCounter;

    public HotelInventory() {
        this.storageManager = new FileStorageManager();
        loadData();
    }

    /** Loads existing data from disk, or sets up a fresh default inventory. */
    private void loadData() {
        List<Room> loadedRooms = storageManager.loadRooms();
        if (loadedRooms == null || loadedRooms.isEmpty()) {
            rooms = createDefaultRooms();
        } else {
            rooms = loadedRooms;
        }
        reservations = storageManager.loadReservations(rooms);
        reservationCounter = reservations.size() + 1;
    }

    /** Sets up a default set of rooms when the system runs for the first time. */
    private List<Room> createDefaultRooms() {
        List<Room> defaultRooms = new ArrayList<>();
        for (int i = 101; i <= 105; i++) defaultRooms.add(new Room(i, RoomCategory.STANDARD, true));
        for (int i = 201; i <= 204; i++) defaultRooms.add(new Room(i, RoomCategory.DELUXE, true));
        for (int i = 301; i <= 302; i++) defaultRooms.add(new Room(i, RoomCategory.SUITE, true));
        return defaultRooms;
    }

    public List<Room> getAllRooms() {
        return rooms;
    }

    public List<Room> searchAvailableRooms(RoomCategory category) {
        return rooms.stream()
                .filter(Room::isAvailable)
                .filter(r -> category == null || r.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Room findRoomByNumber(int roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Books a room for a guest. Validates availability and dates,
     * simulates a payment, then creates and stores the reservation.
     */
    public Reservation bookRoom(Room room, Guest guest, LocalDate checkIn, LocalDate checkOut) {
        if (!room.isAvailable()) {
            throw new IllegalStateException("Room #" + room.getRoomNumber() + " is not available.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        double amount = Reservation.calculateTotal(room, checkIn, checkOut);
        String transactionId = PaymentSimulator.processPayment(amount);

        String reservationId = "RES" + (reservationCounter++);
        Reservation reservation = new Reservation(reservationId, room, guest, checkIn, checkOut, transactionId);

        room.setAvailable(false);
        reservations.put(reservationId, reservation);
        persist();
        return reservation;
    }

    /**
     * Cancels an existing reservation, frees up the room, and simulates a refund.
     */
    public boolean cancelReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null || reservation.getStatus() == ReservationStatus.CANCELLED) {
            return false;
        }
        reservation.cancel();
        reservation.getRoom().setAvailable(true);
        PaymentSimulator.processRefund(reservation.getTransactionId(), reservation.getTotalAmount());
        persist();
        return true;
    }

    public Reservation getReservation(String reservationId) {
        return reservations.get(reservationId);
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    /** Saves the current state (rooms + reservations) to disk. */
    public void persist() {
        storageManager.saveRooms(rooms);
        storageManager.saveReservations(reservations.values());
    }
}
