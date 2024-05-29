import java.sql.*;
import java.util.Scanner;

class FeedbackSystem {

    static final String JDBC_URL = "jdbc:mysql://localhost:3306/ff";
    static final String JDBC_USER = "root";
    static final String JDBC_PASSWORD = "unknownguy49";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    public static void insertFeedback(int bookingId, String customerName, int rating, String comments) {
        String sql = "INSERT INTO Feedback (booking_id, customer_name, rating, comments) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            pstmt.setString(2, customerName);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comments);
            pstmt.executeUpdate();
            System.out.println("Feedback submitted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getFeedbackByBookingId(int bookingId) {
        String sql = "SELECT * FROM Feedback WHERE booking_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int feedbackId = rs.getInt("feedback_id");
                String customerName = rs.getString("customer_name");
                int rating = rs.getInt("rating");
                String comments = rs.getString("comments");
                Timestamp feedbackTime = rs.getTimestamp("feedback_time");
                System.out.println("Feedback ID: " + feedbackId);
                System.out.println("Customer Name: " + customerName);
                System.out.println("Rating: " + rating);
                System.out.println("Comments: " + comments);
                System.out.println("Feedback Time: " + feedbackTime);
                System.out.println("--------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println();
        System.out.println("----------------------------Welcome to the Feedback System----------------------------");
        
        while (true) {
            System.out.println("1. Submit Feedback");
            System.out.println("2. View Feedback by Booking ID");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1:
                    System.out.print("Enter Booking ID: ");
                    int bookingId = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Customer Name: ");
                    String customerName = sc.nextLine();
                    System.out.print("Enter Rating (1-5): ");
                    int rating = sc.nextInt();
                    sc.nextLine(); 
                    System.out.print("Enter Comments: ");
                    String comments = sc.nextLine();
                    insertFeedback(bookingId, customerName, rating, comments);
                    break;
                case 2:
                    System.out.print("Enter Booking ID: ");
                    int searchBookingId = sc.nextInt();
                    getFeedbackByBookingId(searchBookingId);
                    break;
                case 3:
                    System.out.println("Exiting the system.");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
