import java.sql.*;
import java.util.Scanner;

class FoodOrderSystem {
    Connection conn;

    FoodOrderSystem(Connection conn) {
        this.conn = conn;
    }

    void displayMenu(String category) throws SQLException {
        String query = "";
        switch (category) {
            case "Starters":
                query = "SELECT * FROM Starters";
                break;
            case "MainCourse":
                query = "SELECT * FROM MainCourse";
                break;
            case "Desserts":
                query = "SELECT * FROM Desserts";
                break;
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ". " + rs.getString("name") + " - Rs." + rs.getDouble("price"));
        }
    }

    void placeOrder(int bookingId) throws SQLException {
        Scanner sc = new Scanner(System.in);
        int foodId, quantity;
        String category, moreOrders;
        double totalAmount = 0;

        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Orders (booking_id, total_amount) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, bookingId);
        pstmt.setDouble(2, 0);
        pstmt.executeUpdate();
        ResultSet rs = pstmt.getGeneratedKeys();
        rs.next();
        int orderId = rs.getInt(1);

        do {
            System.out.println("Enter category (Starters/MainCourse/Desserts): ");
            category = sc.next();
            displayMenu(category);

            System.out.println("Enter food item ID: ");
            foodId = sc.nextInt();
            System.out.println("Enter quantity: ");
            quantity = sc.nextInt();

            double price = 0;
            String foodItem = "";
            switch (category) {
                case "Starters":
                    pstmt = conn.prepareStatement("SELECT name, price FROM Starters WHERE id = ?");
                    break;
                case "MainCourse":
                    pstmt = conn.prepareStatement("SELECT name, price FROM MainCourse WHERE id = ?");
                    break;
                case "Desserts":
                    pstmt = conn.prepareStatement("SELECT name, price FROM Desserts WHERE id = ?");
                    break;
            }
            pstmt.setInt(1, foodId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                foodItem = rs.getString("name");
                price = rs.getDouble("price");
            }

            totalAmount += price * quantity;

            pstmt = conn.prepareStatement(
                    "INSERT INTO OrderDetails (order_id, food_item, quantity, price) VALUES (?, ?, ?, ?)");
            pstmt.setInt(1, orderId);
            pstmt.setString(2, foodItem);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, price * quantity);
            pstmt.executeUpdate();

            System.out.println("Do you want to place more orders? (Y/N): ");
            moreOrders = sc.next();
        } while (moreOrders.equalsIgnoreCase("Y"));

        pstmt = conn.prepareStatement("UPDATE Orders SET total_amount = ? WHERE order_id = ?");
        pstmt.setDouble(1, totalAmount);
        pstmt.setInt(2, orderId);
        pstmt.executeUpdate();

        System.out.println("Order placed successfully! Total amount: Rs." + totalAmount);
    }
    
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ff", "root", "unknownguy49");
            FoodOrderSystem fos = new FoodOrderSystem(conn);
            Scanner sc = new Scanner(System.in);
            
            System.out.println();
            System.out.println("----------------------------Welcome to FoodFrenzy Restaurant Food Order Menu----------------------------");
            System.out.println("Enter booking ID: ");
            int bookingId = sc.nextInt();
            fos.placeOrder(bookingId);

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}