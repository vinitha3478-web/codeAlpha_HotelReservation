import java.io.*;
import java.util.*;

/**
 * Handles all File I/O for the hotel system - persisting and loading
 * room inventory and reservation records so data survives across runs.
 */
public class FileStorageManager {
    private static final String ROOMS_FILE = "rooms.csv";
    private static final String RESERVATIONS_FILE = "reservations.csv";

    /** Saves the full room inventory to disk. */
    public void saveRooms(List<Room> rooms) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room room : rooms) {
                writer.println(room.getRoomNumber() + "|" + room.getCategory() + "|" + room.isAvailable());
            }
        } catch (IOException e) {
            System.out.println("Error saving rooms: " + e.getMessage());
        }
    }

    /** Loads room inventory from disk. Returns null if no file exists yet. */
    public List<Room> loadRooms() {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) {
            return null;
        }
        List<Room> rooms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                int roomNumber = Integer.parseInt(parts[0]);
                RoomCategory category = RoomCategory.valueOf(parts[1]);
                boolean available = Boolean.parseBoolean(parts[2]);
                rooms.add(new Room(roomNumber, category, available));
            }
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
            return null;
        }
        return rooms;
    }

    /** Saves all reservations (confirmed and cancelled) to disk. */
    public void saveReservations(Collection<Reservation> reservations) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (Reservation r : reservations) {
                writer.println(r.toCsvRow());
            }
        } catch (IOException e) {
            System.out.println("Error saving reservations: " + e.getMessage());
        }
    }

    /** Loads reservations from disk, matching each one back to its Room object. */
    public Map<String, Reservation> loadReservations(List<Room> rooms) {
        Map<String, Reservation> reservations = new LinkedHashMap<>();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) {
            return reservations;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|");
                int roomNumber = Integer.parseInt(parts[1]);
                Room matchedRoom = rooms.stream()
                        .filter(r -> r.getRoomNumber() == roomNumber)
                        .findFirst()
                        .orElse(null);
                if (matchedRoom == null) continue;
                Reservation reservation = Reservation.fromCsvRow(line, matchedRoom);
                reservations.put(reservation.getReservationId(), reservation);
            }
        } catch (IOException e) {
            System.out.println("Error loading reservations: " + e.getMessage());
        }
        return reservations;
    }
}
