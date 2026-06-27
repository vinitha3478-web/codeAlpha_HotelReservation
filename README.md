# 🏨 Hotel Reservation System (Java)

A Java-based Hotel Reservation System built using core **Object-Oriented Programming** principles, with both a **console interface** and a **Swing GUI**. Developed as part of the **CodeAlpha Java Programming Internship**.

## ✨ Features

- 🔍 **Search Available Rooms** — filter by category (Standard, Deluxe, Suite)
- 🛏️ **Room Categorization** — Standard, Deluxe, and Suite with different pricing
- 📅 **Book & Cancel Reservations** — full booking lifecycle with date validation
- 💳 **Payment Simulation** — simulated transaction processing and refunds
- 💾 **File I/O Persistence** — bookings and room availability saved to CSV files, so data survives between runs
- 🖥️ **Two interfaces**: a console-based menu app and a graphical Swing desktop app
- 🧱 **OOP Design** — encapsulation, separation of concerns across Room, Guest, Reservation, Inventory, and Payment classes

## 🧩 Tech Stack

- Java (JDK 17+)
- Swing (for GUI)
- File I/O (CSV-based persistence)

## 📂 Project Structure

| File | Responsibility |
|---|---|
| `Room.java` | Represents a hotel room (number, category, availability) |
| `RoomCategory.java` | Enum for Standard / Deluxe / Suite pricing |
| `Guest.java` | Guest details |
| `Reservation.java` | Booking record with dates, amount, status |
| `ReservationStatus.java` | Enum for Confirmed / Cancelled |
| `PaymentSimulator.java` | Simulated payment & refund processing |
| `HotelInventory.java` | Core business logic — search, book, cancel |
| `FileStorageManager.java` | Reads/writes rooms & reservations to CSV |
| `HotelReservationSystem.java` | Console-based entry point |
| `HotelReservationGUI.java` | Swing GUI entry point |

## 🚀 How to Run

**Console version:**
```bash
javac *.java
java HotelReservationSystem
```

**GUI version:**
```bash
javac *.java
java HotelReservationGUI
```

## 📸 Screenshots
*(Add your own screenshots here)*

## 🎓 Internship Project

This project was built as part of Task 4 of the **CodeAlpha Java Programming Internship**.
