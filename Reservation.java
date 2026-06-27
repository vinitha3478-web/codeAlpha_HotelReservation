import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a booking made by a guest for a specific room.
 * Holds all details required to track, cancel, or display the booking.
 */
public class Reservation {
    private final String reservationId;
    private final Room room;
    private final Guest guest;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final double totalAmount;
    private String transactionId;
    private ReservationStatus status;

    public Reservation(String reservationId, Room room, Guest guest,
                        LocalDate checkInDate, LocalDate checkOutDate,
                        String transactionId) {
        this.reservationId = reservationId;
        this.room = room;
        this.guest = guest;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = calculateTotal(room, checkInDate, checkOutDate);
        this.transactionId = transactionId;
        this.status = ReservationStatus.CONFIRMED;
    }

    public static double calculateTotal(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return nights * room.getPricePerNight();
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public String getReservationId() {
        return reservationId;
    }

    public Room getRoom() {
        return room;
    }

    public Guest getGuest() {
        return guest;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    /** Converts this reservation into a single CSV line for file storage. */
    public String toCsvRow() {
        return String.join("|",
                reservationId,
                String.valueOf(room.getRoomNumber()),
                guest.getName(),
                guest.getContactNumber(),
                guest.getEmail(),
                checkInDate.toString(),
                checkOutDate.toString(),
                String.format("%.2f", totalAmount),
                transactionId,
                status.toString());
    }

    /** Rebuilds a Reservation object from a CSV line, given the matching Room. */
    public static Reservation fromCsvRow(String line, Room room) {
        String[] parts = line.split("\\|");
        Guest guest = new Guest(parts[2], parts[3], parts[4]);
        LocalDate checkIn = LocalDate.parse(parts[5]);
        LocalDate checkOut = LocalDate.parse(parts[6]);
        Reservation r = new Reservation(parts[0], room, guest, checkIn, checkOut, parts[8]);
        if (parts[9].equals(ReservationStatus.CANCELLED.toString())) {
            r.cancel();
        }
        return r;
    }

    @Override
    public String toString() {
        return String.format(
                "Reservation ID : %s%n" +
                "Status         : %s%n" +
                "Guest          : %s%n" +
                "Room           : #%d (%s)%n" +
                "Check-in       : %s%n" +
                "Check-out      : %s%n" +
                "Nights         : %d%n" +
                "Total Amount   : Rs.%.2f%n" +
                "Transaction ID : %s",
                reservationId, status, guest, room.getRoomNumber(), room.getCategory(),
                checkInDate, checkOutDate, getNumberOfNights(), totalAmount, transactionId);
    }
}
