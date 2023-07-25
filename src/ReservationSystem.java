import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Statement;




public class ReservationSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/my_database";
    private static final String USER = "root";
    private static final String PASS = "System@1234";

    public static void main(String[] args) {
        ReservationSystem reservationSystem = new ReservationSystem();
        reservationSystem.login();
    }

    public void login() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Make a reservation");
            System.out.println("4. Cancellation Form");
            System.out.print("Enter your choice (1, 2, 3, or 4): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                // Existing login logic
                System.out.print("Enter your username: ");
                String username = scanner.nextLine();

                System.out.print("Enter your password: ");
                String password = scanner.nextLine();

                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Login successful!");
                            makeReservation(conn, scanner, username); // Call makeReservation() after successful login
                        } else {
                            System.out.println("Invalid credentials. Please try again.");
                        }
                    }
                }
            } else if (choice == 2) {
                // Registration logic
                System.out.print("Enter your username: ");
                String username = scanner.nextLine();

                System.out.print("Enter your password: ");
                String password = scanner.nextLine();

                String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Registration successful!");
                    } else {
                        System.out.println("Registration failed. Please try again.");
                    }
                }
            } else if (choice == 3) {
                // Make a reservation
                System.out.println("Please log in first to make a reservation.");
                login(); // Redirect to login if the user selects option 3 without logging in
            }else if (choice == 4) {
                // Cancellation Form
                System.out.print("Enter your PNR number: ");
                String pnrNumber = scanner.nextLine();
                cancelReservation(conn, pnrNumber); 
            }else {
                System.out.println("Invalid choice. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void cancelReservation(Connection conn, String pnrNumber) {
        try {
            String sql = "SELECT * FROM reservations WHERE PNR_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, pnrNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Reservation details for PNR number " + pnrNumber + ":");
                        System.out.println("Train Number: " + rs.getInt("train_number"));
                        System.out.println("Class Type: " + rs.getString("class_type"));
                        System.out.println("Date of Journey: " + rs.getString("date_of_journey"));
                        System.out.println("From: " + rs.getString("from_place"));
                        System.out.println("Destination: " + rs.getString("destination"));
                        System.out.println("Passenger Name: " + rs.getString("name"));
                        System.out.println("Phone Number: " + rs.getString("phone_number"));

                        // Ask for confirmation before canceling
                        System.out.print("Do you want to cancel this reservation? (Press 'OK' to confirm): ");
                        Scanner scanner = new Scanner(System.in);
                        String confirm = scanner.nextLine();
                        if (confirm.equalsIgnoreCase("OK")) {
                            deleteReservation(conn, pnrNumber);
                        } else {
                            System.out.println("Cancellation not confirmed.");
                        }
                    } else {
                        System.out.println("No reservation found for PNR number " + pnrNumber);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReservation(Connection conn, String pnrNumber) {
        try {
            String deleteSql = "DELETE FROM reservations WHERE PNR_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, pnrNumber);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Reservation with PNR number " + pnrNumber + " has been canceled.");
                } else {
                    System.out.println("Cancellation failed. PNR number not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void makeReservation(Connection conn, Scanner scanner, String username) {
        try {
            System.out.println("Reservation Form");
    
            int trainNumber;
            do {
                System.out.print("Enter train number: ");
                try {
                    trainNumber = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    trainNumber = -1; // Invalid train number entered
                }
    
                if (trainNumber <= 0) {
                    System.out.println("Invalid train number. Please enter a valid positive integer.");
                }
            } while (trainNumber <= 0);
    
            String trainName = getTrainName(conn, trainNumber);
            if (trainName == null) {
                System.out.println("Invalid train number.");
                return;
            }
    
            System.out.print("Enter class type: ");
            String classType = scanner.nextLine();
    
            System.out.print("Enter date of journey (YYYY-MM-DD): ");
            String dateOfJourney = scanner.nextLine();
    
            System.out.print("Enter from (place): ");
            String fromPlace = scanner.nextLine();
    
            System.out.print("Enter destination: ");
            String destination = scanner.nextLine();

            System.out.print("Enter the name of passenger: ");
            String name = scanner.nextLine();

            System.out.print("Enter the phone number: ");
            String phone = scanner.nextLine();

            
    
            String reservationSql = "INSERT INTO reservations (username, train_number, class_type, date_of_journey, from_place, destination, name, phone_number, PNR_number) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, username);
        stmt.setInt(2, trainNumber);
        stmt.setString(3, classType);
        stmt.setString(4, dateOfJourney);
        stmt.setString(5, fromPlace);
        stmt.setString(6, destination);
        stmt.setString(7, name);
        stmt.setString(8, phone);

        String pnrNumber = "PNR" + String.format("%06d", (int) (Math.random() * 1000000));
        stmt.setString(9, pnrNumber);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Reservation successful!");
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int reservationId = generatedKeys.getInt(1);
                    System.out.println("Reservation ID: " + reservationId);
                    System.out.println("PNR number: " + pnrNumber);
                }
            }
        } else {
            System.out.println("Reservation failed. Please try again.");
        }
    }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getTrainName(Connection conn, int trainNumber) throws SQLException {
        String sql = "SELECT train_name FROM trains WHERE train_number = " + trainNumber;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("train_name");
            } else {
                return null;
            }
        }
    }
    
}
