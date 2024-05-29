import java.sql.*;

class Requirements {
}

interface BookingSystem {
    static public void getTables() {
    };

    static public void getBookedTransaction(String CustomerName, Connection connection) throws SQLException {
    };

    static public void getCancelledTransaction(String CustomerName, Connection connection) throws SQLException {
    };

    static public void getLastTransaction() {
    };

    static public void bookTable() {
    };

    static public void cancelTable() {
    };

    static public void getRefundStatus() {
    };
}

class Table {
    private String CustomerName = null;
    private Integer bookingID = null;
    private String date = null;
    private Integer NumberOfTables = null;

    public int getNumberOfTables() {
        return NumberOfTables;
    }

    public void setNumberOfTables(int numberOfTables) {
        NumberOfTables = numberOfTables;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

class BookedTable extends Table {
    private Integer tabID = null;
    private Double amountPaid = null;

    public BookedTable(Connection conn, int bookingID) {
        try {
            PreparedStatement statement = conn.prepareStatement(Query.getBookedTable);
            statement.setInt(1, bookingID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                tabID = resultSet.getInt("TableID");
                amountPaid = resultSet.getDouble("total_amount");
                String name = resultSet.getString("customer_name");
                String date = resultSet.getString("booking_date");
                Integer tables = resultSet.getInt("num_tables_booked");
                super.setBookingID(bookingID);
                super.setDate(date);
                super.setCustomerName(name);
                super.setNumberOfTables(tables);
            } else {
                System.out.println("Booking Not Found");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setTableID(int tabID) {
        this.tabID = tabID;
    }

    public int getTableID() {
        return tabID;
    }

    public void setAmountPaid() {
        this.amountPaid = amountPaid;
    }

    public double getAmountPaid() {
        return amountPaid;
    }
}

class DB {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/ff", "root", "unknownguy49");
    }
}

class Query {
    static String isTable = "SELECT COUNT(*) AS table_count FROM `table` WHERE TableID=?;";
    static String price = "SELECT Price FROM `table` WHERE TableID=?;";
    static String bookTable = "INSERT INTO bookedtables(TableID, customer_name, booking_date, num_tables_booked, total_amount) VALUES (?, ?, CURRENT_DATE(), ?, ?);";
    static String totalTables = "SELECT num_tables_booked FROM bookedtables WHERE TableID=?;";
    static String updateTableQty = "UPDATE bookedtables SET num_tables_booked=? WHERE TableID=?;";
    static String getBookedTable = "SELECT * FROM bookedtables WHERE booking_id = ?;";
    static String cancelTable = "INSERT INTO cancelledtables(booking_id,cancel_date,num_tables_cancelled,refund_amount,customer_name) SELECT ?,CURRENT_DATE(),?,?,customer_name FROM BookedTables WHERE booking_id=?;";
    static String updateBooking = "UPDATE bookedtables SET num_tables_booked=? WHERE booking_id=?;";
    static String deleteBooking = "DELETE FROM bookedtables WHERE booking_id=?";
    static String getRefundStatus = "SELECT refund_amount FROM cancelledtables WHERE cancel_id=?";
    static String getTableType = "SELECT * FROM `table`";
    static String isCustomerBookingExist = "SELECT COUNT(*) FROM bookedtables WHERE customer_name=?;";
    static String isCancellationExist = "SELECT COUNT(*) FROM cancelledtables WHERE customer_name=?;";
    static String bookedTransactions = "SELECT * FROM bookedtables WHERE customer_name=?";
    static String cancelledTransactions = "SELECT * FROM cancelledtables WHERE customer_name=?";
    static String getTablesBookings = "SELECT * FROM bookedtables WHERE TableID=?;";
    static String getTableBookingAmount = "SELECT SUM(total_amount) FROM bookedtables WHERE TableID=?;";
}