import java.sql.*;
import java.util.*;

public class Restaurant implements BookingSystem {
    static Scanner sc = new Scanner(System.in);

    public static void menu() {
        System.out.println();
        System.out.println("----------------------------Welcome to FoodFrenzy Restaurant----------------------------");
        System.out.println("1. Main Menu");
        System.out.println("2. Show Types of Table Bookings");
        System.out.println("3. Show Previous Transactions");
        System.out.println("4. See Table Bookings");
        System.out.println("5. Exit\nEnter your choice");
    }

    public static void mainMenu() {
        System.out.println();
        System.out.println("----------------------------Main Menu----------------------------");
        System.out.println("1. Book Table");
        System.out.println("2. Cancel Booking");
        System.out.println("3. Refund Status");
        System.out.println("4. Show Types of Table Bookings");
        System.out.println("5. Go back");
    }

    static int availableTable;

    public static void bookTable() {
        System.out.println("----------------------------Book Table----------------------------");
        availableTable = 150;
        System.out.print("Enter Table Type ID: ");
        int tabID = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Customer Name: ");
        String customerName = sc.nextLine();
        System.out.print("Enter Number of Tables: ");
        int table = sc.nextInt();
        try (Connection conn = DB.getConnection()) {
            if (tabID == 1 || tabID == 2 || tabID == 3) {
                double price = getTablePrice(tabID, conn) * table;
                PreparedStatement statement = conn.prepareStatement(Query.bookTable);
                statement.setInt(1, tabID);
                statement.setString(2, customerName);
                statement.setInt(3, table);
                statement.setDouble(4, price);

                if (table <= availableTable) {
                    conn.setAutoCommit(false);
                    statement.executeUpdate();
                    conn.commit();
                    System.out.println();
                    System.out.println("Table Booked Successfully");
                    System.out.println("For Table ID: " + tabID);
                    System.out.println("Customer Name: " + customerName);
                    System.out.println("Number of Tables: " + table);
                    System.out.println("Total Amount: $" + price);
                    availableTable -= table;
                } else {
                    System.out.println("Required Tables are not available");
                    System.out.println("Available Tables: " + availableTable);
                }
            } else {
                System.out.println("Table Type Not Found");
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong");
        }
    }

    public static boolean isTableExist(int tabID, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(Query.isTable);
        statement.setInt(1, tabID);
        ResultSet result = statement.executeQuery();
        result.next();
        return result.getInt(1) == 1;
    }

    public static double getTablePrice(int tabID, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(Query.price);
        statement.setInt(1, tabID);
        ResultSet result = statement.executeQuery();
        result.next();
        return result.getDouble(1);
    }

    public static int getTables(int tabID, Connection conn) {
        try {
            PreparedStatement statement = conn.prepareStatement(Query.totalTables);
            statement.setInt(1, tabID);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching table information: " + e.getMessage());
            return -1;
        }
    }

    public static void updateTables(int tabID, int updatedQty, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(Query.updateTableQty);
        statement.setInt(1, updatedQty);
        statement.setInt(2, tabID);
        statement.executeUpdate();
    }

    public static void cancelTable() {
        Connection conn = null;
        try {
            System.out.print("Enter booking ID: ");
            int bookingID = sc.nextInt();
            System.out.print("Enter Number of Tables to Cancel: ");
            int tablesToBeCancelled = sc.nextInt();
            System.out.print("Enter Table ID: ");
            int tab_id = sc.nextInt();

            conn = DB.getConnection();
            conn.setAutoCommit(false);

            BookedTable table = new BookedTable(conn, bookingID);
            if (table.getNumberOfTables() == 0) {
                System.out.println("Booking ID not found or no tables booked.");
                conn.rollback();
                return;
            }

            double refundAmount = table.getAmountPaid() / table.getNumberOfTables() * tablesToBeCancelled;

            try (PreparedStatement statement = conn.prepareStatement(Query.cancelTable)) {
                statement.setInt(1, bookingID);
                statement.setInt(2, tablesToBeCancelled);
                statement.setDouble(3, refundAmount);
                statement.setInt(4, bookingID);
                statement.executeUpdate();
            }

            int bookedTables = table.getNumberOfTables();
            if (tablesToBeCancelled < bookedTables) {
                try (PreparedStatement statement = conn.prepareStatement(Query.updateBooking)) {
                    statement.setInt(1, bookedTables - tablesToBeCancelled);
                    statement.setInt(2, bookingID);
                    statement.executeUpdate();
                }
            } else if (tablesToBeCancelled == bookedTables) {
                try (PreparedStatement statement = conn.prepareStatement(Query.deleteBooking)) {
                    statement.setInt(1, bookingID);
                    statement.executeUpdate();
                }
            } else {
                System.out.println("Number of tables booked are: " + bookedTables);
                System.out.println("Please try lower or equal to Booked Tables");
                conn.rollback();
                return;
            }

            int availableTable = getTables(tab_id, conn);
            updateTables(tab_id, availableTable + tablesToBeCancelled, conn);

            conn.commit();
            System.out.println("Booking cancelled successfully.");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error during rollback: " + ex.getMessage());
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid Input");
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public static void getRefundStatus() {
        System.out.println("Enter Cancel ID: ");
        int cancelID = sc.nextInt();
        try (Connection conn = DB.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(Query.getRefundStatus);
            statement.setInt(1, cancelID);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                System.out.println("Refund of $" + result.getInt(1) + " initiated to the source account");
            } else {
                System.out.println("Cancellation Does Not Exist");
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
    }

    public static void getTableTypes() {
        try (Connection conn = DB.getConnection()) {
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(Query.getTableType);

            System.out.printf("%-10s%-20s%-15s%n", "TableID", "Name", "Price");

            while (result.next()) {
                int tabID = result.getInt(1);
                String name = result.getString(2);
                double price = result.getDouble(3);
                System.out.printf("%-10d%-20s%-15.2f%n", tabID, name, price);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void getLastTransaction() {
        System.out.println("----------------------------Last Transaction----------------------------");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Customer Name: ");
        String customerName = sc.nextLine();
        boolean customerExists = isCustomerExist(customerName);

        if (customerExists) {
            try (Connection conn = DB.getConnection()) {
                getBookedTransaction(customerName, conn);
                getCancelledTransaction(customerName, conn);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Customer does not exist");
        }
    }

    public static boolean isCustomerExist(String name) {
        try (Connection conn = DB.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(Query.isCustomerBookingExist);
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            result.next();

            if (result.getInt(1) >= 1) {
                return true;
            } else {
                PreparedStatement statement1 = conn.prepareStatement(Query.isCancellationExist);
                statement1.setString(1, name);
                ResultSet result1 = statement1.executeQuery();
                result1.next();
                return (result1.getInt(1) >= 1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static void getBookedTransaction(String customerName, Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement(Query.bookedTransactions);
        statement.setString(1, customerName);
        ResultSet result = statement.executeQuery();
        System.out.println();
        if (result.next()) {
            System.out.println("----------------------------Booked Transactions----------------------------");
            System.out.printf("%-10s%-15s%-15s%-15s%-10s%n", "BookingID", "TableID", "BookingDate", "TableBooked",
                    "Amount Paid");
            System.out.println();
            do {
                int bookingID = result.getInt(1);
                int tabID = result.getInt(2);
                String bookingDate = result.getString(4);
                int tableBooked = result.getInt(5);
                double price = result.getDouble(6);
                System.out.printf("%-10s%-15s%-15s%-15s$%-10s%n", bookingID, tabID, bookingDate, tableBooked, price);
            } while (result.next());
        }
        result.close();
    }

    public static void getCancelledTransaction(String customerName, Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement(Query.cancelledTransactions);
        statement.setString(1, customerName);
        ResultSet result = statement.executeQuery();
        System.out.println();
        if (result.next()) {
            System.out.println("----------------------------Cancelled Transactions----------------------------");
            System.out.printf("%-10s%-15s%-15s%-15s%-10s%n", "CancelID", "TableID", "CancelDate", "TableCancelled",
                    "Refund Amount");
            System.out.println();
            do {
                int cancelID = result.getInt(1);
                int tabID = result.getInt(2);
                String cancelDate = result.getString(4);
                int tableCancelled = result.getInt(5);
                double price = result.getDouble(6);
                System.out.printf("%-10s%-15s%-15s%-15s$%-10s%n", cancelID, "N/A", cancelDate, tableCancelled, price);
            } while (result.next());
        }
        result.close();
    }

    static void getTableBookings() {
        System.out.println("----------------------------Bookings----------------------------");
        System.out.println();
        System.out.print("Enter Table ID: ");
        int tabID = sc.nextInt();
        try (Connection con = DB.getConnection()) {
            if (isTableExist(tabID, con)) {
                PreparedStatement statement = con.prepareStatement(Query.getTablesBookings);
                statement.setInt(1, tabID);
                ResultSet result = statement.executeQuery();
                System.out.printf("%-10s%-15s%-15s%-15s%-10s%n", "BookingID", "CustomerName", "BookingDate",
                        "TableBooked", "AmountPaid");
                System.out.println();
                while (result.next()) {
                    int bookingID = result.getInt(1);
                    String name = result.getString(3);
                    String bookingDate = result.getString(4);
                    int tablesBooked = result.getInt(5);
                    double price = result.getDouble(6);
                    System.out.printf("%-10s%-15s%-15s%-15s$%-10s%n", bookingID, name, bookingDate, tablesBooked,
                            price);
                }
                System.out.println();
                System.out.println("Total Amount: $" + getTableBookingAmount(tabID, con));
            } else {
                System.out.println();
                System.out.println("Event Not Found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
    }

    static double getTableBookingAmount(int tabID, Connection con) throws SQLException {
        PreparedStatement statement = con.prepareStatement(Query.getTableBookingAmount);
        statement.setInt(1, tabID);
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            return result.getDouble(1);
        } else
            return 0;
    }
}
