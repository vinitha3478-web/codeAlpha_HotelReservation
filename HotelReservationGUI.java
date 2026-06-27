import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Swing GUI version of the Hotel Reservation System.
 * Reuses the existing HotelInventory / Room / Guest / Reservation /
 * PaymentSimulator / FileStorageManager classes - this file only adds
 * a graphical front-end on top of that same business logic.
 */
public class HotelReservationGUI extends JFrame {

    // ---- Theme colors ----
    private static final Color PRIMARY = new Color(20, 60, 90);
    private static final Color ACCENT = new Color(212, 160, 23);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HotelInventory inventory = new HotelInventory();

    // Rooms tab
    private DefaultTableModel roomsTableModel;
    private JComboBox<String> categoryFilterCombo;

    // Book tab
    private JComboBox<Integer> bookRoomCombo;
    private JTextField nameField, contactField, emailField, checkInField, checkOutField;
    private JLabel bookingSummaryLabel;

    // Manage tab
    private DefaultTableModel reservationsTableModel;
    private JTable reservationsTable;

    public HotelReservationGUI() {
        setLookAndFeel();
        setTitle("Hotel Reservation System");
        setSize(960, 640);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(BG);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                inventory.persist();
                dispose();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BUTTON_FONT);
        tabs.addTab("Available Rooms", buildRoomsTab());
        tabs.addTab("Book a Room", buildBookTab());
        tabs.addTab("Manage Reservations", buildManageTab());

        tabs.addChangeListener(e -> {
            refreshRoomsTable();
            refreshBookRoomCombo();
            refreshReservationsTable();
        });

        add(tabs, BorderLayout.CENTER);

        refreshRoomsTable();
        refreshBookRoomCombo();
        refreshReservationsTable();
    }

    private void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    // ===================================================================
    // HEADER
    // ===================================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("\uD83C\uDFE8  Hotel Reservation System");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Search · Book · Manage your reservations");
        subtitle.setFont(LABEL_FONT);
        subtitle.setForeground(new Color(220, 225, 230));
        header.add(subtitle, BorderLayout.EAST);

        return header;
    }

    // ===================================================================
    // TAB 1: AVAILABLE ROOMS
    // ===================================================================
    private JPanel buildRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterRow.setBackground(BG);
        JLabel filterLabel = new JLabel("Filter by category:");
        filterLabel.setFont(LABEL_FONT);
        categoryFilterCombo = new JComboBox<>(new String[]{"ALL", "STANDARD", "DELUXE", "SUITE"});
        categoryFilterCombo.setFont(LABEL_FONT);
        JButton searchBtn = styledButton("Search", ACCENT, Color.BLACK);
        JButton refreshBtn = styledButton("Refresh", PRIMARY, Color.WHITE);

        searchBtn.addActionListener(e -> refreshRoomsTable());
        refreshBtn.addActionListener(e -> {
            categoryFilterCombo.setSelectedIndex(0);
            refreshRoomsTable();
        });

        filterRow.add(filterLabel);
        filterRow.add(categoryFilterCombo);
        filterRow.add(searchBtn);
        filterRow.add(refreshBtn);

        roomsTableModel = new DefaultTableModel(new Object[]{"Room #", "Category", "Price/Night (Rs.)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable roomsTable = new JTable(roomsTableModel);
        styleTable(roomsTable);

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshRoomsTable() {
        roomsTableModel.setRowCount(0);
        String selected = categoryFilterCombo != null ? (String) categoryFilterCombo.getSelectedItem() : "ALL";
        RoomCategory category = (selected == null || selected.equals("ALL")) ? null : RoomCategory.valueOf(selected);

        List<Room> rooms = category == null ? inventory.getAllRooms() : inventory.searchAvailableRooms(category);
        if (category == null) {
            // show all rooms (available + booked) when no filter chosen
            for (Room r : inventory.getAllRooms()) {
                roomsTableModel.addRow(new Object[]{
                        r.getRoomNumber(), r.getCategory(),
                        String.format("%.2f", r.getPricePerNight()),
                        r.isAvailable() ? "AVAILABLE" : "BOOKED"
                });
            }
        } else {
            for (Room r : rooms) {
                roomsTableModel.addRow(new Object[]{
                        r.getRoomNumber(), r.getCategory(),
                        String.format("%.2f", r.getPricePerNight()),
                        "AVAILABLE"
                });
            }
        }
    }

    // ===================================================================
    // TAB 2: BOOK A ROOM
    // ===================================================================
    private JPanel buildBookTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        bookRoomCombo = new JComboBox<>();
        nameField = new JTextField(18);
        contactField = new JTextField(18);
        emailField = new JTextField(18);
        checkInField = new JTextField(LocalDate.now().plusDays(1).format(DATE_FORMAT), 18);
        checkOutField = new JTextField(LocalDate.now().plusDays(2).format(DATE_FORMAT), 18);

        addFormRow(card, gbc, 0, "Available Room:", bookRoomCombo);
        addFormRow(card, gbc, 1, "Guest Name:", nameField);
        addFormRow(card, gbc, 2, "Contact Number:", contactField);
        addFormRow(card, gbc, 3, "Email:", emailField);
        addFormRow(card, gbc, 4, "Check-in (yyyy-MM-dd):", checkInField);
        addFormRow(card, gbc, 5, "Check-out (yyyy-MM-dd):", checkOutField);

        bookingSummaryLabel = new JLabel(" ");
        bookingSummaryLabel.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        card.add(bookingSummaryLabel, gbc);

        JButton bookBtn = styledButton("Book Now & Pay", ACCENT, Color.BLACK);
        bookBtn.addActionListener(e -> handleBooking());
        gbc.gridy = 7;
        card.add(bookBtn, gbc);

        outer.add(card);
        return outer;
    }

    private void addFormRow(JPanel card, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        GridBagConstraints labelGbc = (GridBagConstraints) gbc.clone();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        card.add(label, labelGbc);

        GridBagConstraints fieldGbc = (GridBagConstraints) gbc.clone();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        field.setFont(LABEL_FONT);
        card.add(field, fieldGbc);
    }

    private void refreshBookRoomCombo() {
        if (bookRoomCombo == null) return;
        bookRoomCombo.removeAllItems();
        for (Room r : inventory.searchAvailableRooms(null)) {
            bookRoomCombo.addItem(r.getRoomNumber());
        }
    }

    private void handleBooking() {
        Integer roomNumber = (Integer) bookRoomCombo.getSelectedItem();
        if (roomNumber == null) {
            showError("No available room selected. Please refresh the list.");
            return;
        }
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || email.isEmpty()) {
            showError("Please fill in all guest details.");
            return;
        }

        LocalDate checkIn, checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim(), DATE_FORMAT);
            checkOut = LocalDate.parse(checkOutField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            showError("Dates must be in yyyy-MM-dd format (e.g. 2026-07-15).");
            return;
        }

        Room room = inventory.findRoomByNumber(roomNumber);
        if (room == null) {
            showError("Selected room could not be found.");
            return;
        }

        try {
            Guest guest = new Guest(name, contact, email);
            Reservation reservation = inventory.bookRoom(room, guest, checkIn, checkOut);

            bookingSummaryLabel.setText("<html><b>Booked!</b> " + reservation.getReservationId()
                    + " — Rs." + String.format("%.2f", reservation.getTotalAmount()) + "</html>");

            JOptionPane.showMessageDialog(this,
                    "Booking Confirmed!\n\n"
                            + "Reservation ID : " + reservation.getReservationId() + "\n"
                            + "Room           : #" + room.getRoomNumber() + " (" + room.getCategory() + ")\n"
                            + "Guest          : " + name + "\n"
                            + "Nights         : " + reservation.getNumberOfNights() + "\n"
                            + "Total Paid     : Rs." + String.format("%.2f", reservation.getTotalAmount()) + "\n"
                            + "Transaction ID : " + reservation.getTransactionId(),
                    "Booking Successful", JOptionPane.INFORMATION_MESSAGE);

            nameField.setText("");
            contactField.setText("");
            emailField.setText("");
            refreshBookRoomCombo();
            refreshRoomsTable();
            refreshReservationsTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError("Booking failed: " + ex.getMessage());
        }
    }

    // ===================================================================
    // TAB 3: MANAGE RESERVATIONS
    // ===================================================================
    private JPanel buildManageTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        reservationsTableModel = new DefaultTableModel(
                new Object[]{"Reservation ID", "Guest", "Room #", "Check-in", "Check-out", "Amount (Rs.)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        reservationsTable = new JTable(reservationsTableModel);
        styleTable(reservationsTable);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.setBackground(BG);
        JButton viewBtn = styledButton("View Details", PRIMARY, Color.WHITE);
        JButton cancelBtn = styledButton("Cancel Reservation", new Color(180, 50, 50), Color.WHITE);
        JButton refreshBtn = styledButton("Refresh", ACCENT, Color.BLACK);

        viewBtn.addActionListener(e -> handleViewDetails());
        cancelBtn.addActionListener(e -> handleCancel());
        refreshBtn.addActionListener(e -> refreshReservationsTable());

        buttonRow.add(viewBtn);
        buttonRow.add(cancelBtn);
        buttonRow.add(refreshBtn);

        panel.add(buttonRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(reservationsTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshReservationsTable() {
        if (reservationsTableModel == null) return;
        reservationsTableModel.setRowCount(0);
        for (Reservation r : inventory.getAllReservations()) {
            reservationsTableModel.addRow(new Object[]{
                    r.getReservationId(), r.getGuest().getName(), r.getRoom().getRoomNumber(),
                    r.getCheckInDate(), r.getCheckOutDate(),
                    String.format("%.2f", r.getTotalAmount()), r.getStatus()
            });
        }
    }

    private String getSelectedReservationId() {
        int row = reservationsTable.getSelectedRow();
        if (row == -1) {
            showError("Please select a reservation from the table first.");
            return null;
        }
        return (String) reservationsTableModel.getValueAt(row, 0);
    }

    private void handleViewDetails() {
        String id = getSelectedReservationId();
        if (id == null) return;
        Reservation r = inventory.getReservation(id);
        if (r == null) {
            showError("Reservation not found.");
            return;
        }
        JOptionPane.showMessageDialog(this, r.toString(), "Booking Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void handleCancel() {
        String id = getSelectedReservationId();
        if (id == null) return;
        Reservation r = inventory.getReservation(id);
        if (r == null) {
            showError("Reservation not found.");
            return;
        }
        if (r.getStatus() == ReservationStatus.CANCELLED) {
            showError("This reservation is already cancelled.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel reservation " + id + " for " + r.getGuest().getName() + "?\nA refund will be simulated.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.cancelReservation(id);
            JOptionPane.showMessageDialog(this, "Reservation " + id + " cancelled and refunded.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            refreshReservationsTable();
            refreshRoomsTable();
            refreshBookRoomCombo();
        }
    }

    // ===================================================================
    // HELPERS
    // ===================================================================
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(LABEL_FONT);
        table.setRowHeight(26);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(255, 236, 179));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(225, 228, 232));

        // Custom header renderer - Nimbus LAF ignores plain setBackground/setForeground
        // on JTableHeader, so we force the colors via a DefaultTableCellRenderer instead.
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setOpaque(true);
                label.setBackground(PRIMARY);
                label.setForeground(Color.WHITE);
                label.setFont(BUTTON_FONT);
                label.setHorizontalAlignment(JLabel.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return label;
            }
        });

        // Custom cell renderer for body rows - ensures readable black-on-white / striped rows
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                if (isSelected) {
                    label.setBackground(new Color(255, 236, 179));
                    label.setForeground(Color.BLACK);
                } else {
                    label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 247, 250));
                    label.setForeground(Color.BLACK);
                }
                return label;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationGUI().setVisible(true));
    }
}