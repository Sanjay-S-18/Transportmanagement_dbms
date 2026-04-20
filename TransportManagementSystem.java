
package transportmanagement;

import java.sql.*;
import java.util.Scanner;

public class TransportManagementSystem {

    // UPDATE: The database name in the URL is now changed to "trans"
    static final String DB_URL = "jdbc:mysql://localhost:3306/transport?useSSL=false&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "dbms";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            
            // Auto-creates tables if they do not exist
            setupDatabase(conn);

            while (true) {
                System.out.println("\n===== TRANSPORT MANAGEMENT SYSTEM =====");
                System.out.println("1. View Vehicles");
                System.out.println("2. View Drivers");
                System.out.println("3. View Trips");
                System.out.println("4. Add Vehicle");
                System.out.println("5. Update Vehicle Capacity");
                System.out.println("6. Delete Vehicle");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        viewVehicles(conn);
                        break;

                    case 2:
                        viewDrivers(conn);
                        break;

                    case 3:
                        viewTrips(conn);
                        break;

                    case 4:
                        addVehicle(conn, sc);
                        break;

                    case 5:
                        updateVehicle(conn, sc);
                        break;

                    case 6:
                        deleteVehicle(conn, sc);
                        break;

                    case 7:
                        System.out.println("Exiting...");
                        return;

                    default:
                        System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // --- DATABASE SETUP ---
    static void setupDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String createVehicle = "CREATE TABLE IF NOT EXISTS vehicle (" +
                    "vehicle_id VARCHAR(20) PRIMARY KEY, " +
                    "model VARCHAR(50), " +
                    "vehicle_type VARCHAR(50), " +
                    "capacity INT)";

            String createDriver = "CREATE TABLE IF NOT EXISTS driver (" +
                    "driver_id VARCHAR(20) PRIMARY KEY, " +
                    "driver_name VARCHAR(50), " +
                    "phone VARCHAR(20))";

            String createTrip = "CREATE TABLE IF NOT EXISTS trip (" +
                    "trip_id VARCHAR(20) PRIMARY KEY, " +
                    "vehicle_id VARCHAR(20), " +
                    "distance INT, " +
                    "trip_date DATE, " +
                    "FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON DELETE CASCADE)";

            stmt.execute(createVehicle);
            stmt.execute(createDriver);
            stmt.execute(createTrip);
            
        } catch (SQLException e) {
            System.out.println("Note: Tables check/creation had an issue. It might be due to existing constraints.");
        }
    }

    // VIEW VEHICLES
    static void viewVehicles(Connection conn) throws SQLException {
        String sql = "SELECT * FROM vehicle";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\n--- Vehicle List ---");
        while (rs.next()) {
            System.out.println(rs.getString("vehicle_id") + " | "
                    + rs.getString("model") + " | "
                    + rs.getString("vehicle_type") + " | "
                    + "Capacity: " + rs.getInt("capacity"));
        }
    }

    // VIEW DRIVERS
    static void viewDrivers(Connection conn) throws SQLException {
        String sql = "SELECT * FROM driver";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\n--- Driver List ---");
        while (rs.next()) {
            System.out.println(rs.getString("driver_id") + " | "
                    + rs.getString("driver_name") + " | "
                    + rs.getString("phone"));
        }
    }

    // VIEW TRIPS
    static void viewTrips(Connection conn) throws SQLException {
        String sql = "SELECT * FROM trip";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\n--- Trips List ---");
        while (rs.next()) {
            System.out.println(rs.getString("trip_id") + " | "
                    + rs.getString("vehicle_id") + " | "
                    + "Distance: " + rs.getInt("distance") + "km | "
                    + "Date: " + rs.getDate("trip_date"));
        }
    }

    // ADD VEHICLE
    static void addVehicle(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Vehicle ID: ");
        String id = sc.next();

        System.out.print("Enter Model: ");
        String model = sc.next();

        System.out.print("Enter Type (e.g. Bus/Truck): ");
        String type = sc.next();

        System.out.print("Enter Capacity: ");
        int capacity = sc.nextInt();

        String sql = "INSERT INTO vehicle VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, id);
        ps.setString(2, model);
        ps.setString(3, type);
        ps.setInt(4, capacity);

        ps.executeUpdate();
        System.out.println("Vehicle added successfully!");
    }

    // UPDATE VEHICLE CAPACITY
    static void updateVehicle(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Vehicle ID: ");
        String id = sc.next();

        System.out.print("Enter New Capacity: ");
        int capacity = sc.nextInt();

        String sql = "UPDATE vehicle SET capacity=? WHERE vehicle_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, capacity);
        ps.setString(2, id);

        int rows = ps.executeUpdate();
        System.out.println(rows + " row(s) updated.");
    }

    // DELETE VEHICLE
    static void deleteVehicle(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter Vehicle ID: ");
        String id = sc.next();

        String sql = "DELETE FROM vehicle WHERE vehicle_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, id);

        int rows = ps.executeUpdate();
        System.out.println(rows + " row(s) deleted.");
    }
}
