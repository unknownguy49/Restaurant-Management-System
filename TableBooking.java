import java.util.Scanner;

public class TableBooking {
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        int choice;
        boolean shouldExit = false;
        while (!shouldExit) {
            Restaurant.menu();
            System.out.println();
            System.out.print(">> ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    while (true) {
                        Restaurant.mainMenu();
                        System.out.println();
                        System.out.print(">> ");
                        choice = sc.nextInt();

                        if (choice == 1) {
                            Restaurant.bookTable();
                        } else if (choice == 2) {
                            Restaurant.cancelTable();
                        } else if (choice == 3) {
                            Restaurant.getRefundStatus();
                        } else if (choice == 4) {
                            Restaurant.getTableTypes();
                        } else if (choice == 5) {
                            break;
                        } else {
                            System.out.println("Invalid Choice");
                        }
                        System.out.println();
                    }
                    break;

                case 2:
                    System.out.println();
                    Restaurant.getTableTypes();
                    System.out.println();
                    break;

                case 3:
                    System.out.println();
                    Restaurant.getLastTransaction();
                    break;

                case 4:
                    System.out.println();
                    Restaurant.getTableBookings();
                    break;

                case 5:
                    System.out.println();
                    shouldExit = true;
                    break;
            }
        }
        sc.close();
    }
}