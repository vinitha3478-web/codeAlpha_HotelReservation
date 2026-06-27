import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the application.
 * Provides a console-based menu so the user can search rooms, book,
 * cancel, and view reservations. All data is persisted to CSV files
 * via HotelInventory + FileStorageManager so it survives between runs.
 */
public class HotelReservationSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final HotelInventory inventory = new HotelInventory();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   HOTEL RESERVATION SYSTEM (Java OOP)   ");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> searchRooms();
                case 2 -> bookRoom();
                case 3 -> cancelReservation();
                case 4 -> viewBookingDetails();
                case 5 -> viewAllReservations();
                case 6 -> {
                    inventory.persist();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------- MENU -----------------");
        System.out.println("1. Search Available Rooms");
        System.out.println("2. Book a Room");
        System.out.println("3. Cancel a Reservation");
        System.out.println("4. View Booking Details");
        System.out.println("5. View All Reservations");
        System.out.println("6. Exit");
        System.out.println("-----------------------------------------");
    }

    // ----------------------- Menu actions ------------------------------

    private static void searchRooms() {
        System.out.println("\nRoom categories: 1.STANDARD  2.DELUXE  3.SUITE  4.ALL");
        int choice = readInt("Choose category to search: ");
        RoomCategory category = switch (choice) {
            case 1 -> RoomCategory.STANDARD;
            case 2 -> RoomCategory.DELUXE;
            case 3 -> RoomCategory.SUITE;
            default -> null;
        };

        List<Room> available = inventory.searchAvailableRooms(category);
        if (available.isEmpty()) {
            System.out.println("No available rooms found in that category.");
            return;
        }
        System.out.println("\nAvailable Rooms:");
        for (Room r : available) {
            System.out.println("  " + r);
        }
    }

    private static void bookRoom() {
        int roomNumber = readInt("\nEnter room number to book: ");
        Room room = inventory.findRoomByNumber(roomNumber);
        if (room == null) {
            System.out.println("Room not found.");
            return;
        }
        if (!room.isAvailable()) {
            System.out.println("Sorry, Room #" + roomNumber + " is already booked.");
            return;
        }

        System.out.print("Enter guest name: ");
        String name = scanner.nextLine();
        System.out.print("Enter contact number: ");
        String contact = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        Guest guest = new Guest(name, contact, email);

        LocalDate checkIn = readDate("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = readDate("Enter check-out date (yyyy-MM-dd): ");

        try {
            Reservation reservation = inventory.bookRoom(room, guest, checkIn, checkOut);
            System.out.println("\nBooking CONFIRMED!");
            System.out.println(reservation);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private static void cancelReservation() {
        System.out.print("\nEnter reservation ID to cancel: ");
        String id = scanner.nextLine().trim();
        Reservation existing = inventory.getReservation(id);
        if (existing == null) {
            System.out.println("No reservation found with ID: " + id);
            return;
        }
        if (existing.getStatus() == ReservationStatus.CANCELLED) {
            System.out.println("This reservation is already cancelled.");
            return;
        }
        boolean cancelled = inventory.cancelReservation(id);
        if (cancelled) {
            System.out.println("Reservation " + id + " has been cancelled and refunded.");
        } else {
            System.out.println("Cancellation failed.");
        }
    }

    private static void viewBookingDetails() {
        System.out.print("\nEnter reservation ID: ");
        String id = scanner.nextLine().trim();
        Reservation reservation = inventory.getReservation(id);
        if (reservation == null) {
            System.out.println("No reservation found with ID: " + id);
            return;
        }
        System.out.println("\n--- Booking Details ---");
        System.out.println(reservation);
    }

    private static void viewAllReservations() {
        List<Reservation> all = inventory.getAllReservations();
        if (all.isEmpty()) {
            System.out.println("\nNo reservations yet.");
            return;
        }
        System.out.println("\n--- All Reservations (" + all.size() + ") ---");
        for (Reservation r : all) {
            System.out.printf("%-8s | %-9s | Room #%-4d | %-15s | %s to %s | Rs.%.2f%n",
                    r.getReservationId(), r.getStatus(), r.getRoom().getRoomNumber(),
                    r.getGuest().getName(), r.getCheckInDate(), r.getCheckOutDate(), r.getTotalAmount());
        }
    }

    // ----------------------- Input helpers ------------------------------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd (e.g., 2026-07-15).");
            }
        }
    }
}
