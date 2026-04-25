import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MetroClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader soketin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter soketout = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to Metro Server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            
            while (true) {
                System.out.println("\n=== MENU METRO ===");
                System.out.println("1. Add new card");
                System.out.println("2. Get client information");
                System.out.println("3. Top up account");
                System.out.println("4. Pay for ride");
                System.out.println("5. Get remaining balance");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine();
                if (choice.equals("0")) {
                    System.out.println("Exiting.");
                    break;
                }

                String request = buildRequest(choice, scanner);
                if (request != null) {
                    soketout.println(request); // Відправляємо запит серверу
                    String response = soketin.readLine(); // Читаємо відповідь
                    System.out.println("\nServer response: " + response);
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static String buildRequest(String choice, Scanner scanner) {
        System.out.print("Enter card ID (student identifier): ");
        String id = scanner.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter student name: ");
                String name = scanner.nextLine();
                return "ISSUE;" + id + ";" + name;
            case "2":
                return "INFO;" + id;
            case "3":
                System.out.print("Enter top-up amount: ");
                String amount = scanner.nextLine();
                return "ADD;" + id + ";" + amount;
            case "4":
                System.out.print("Enter ride fare: ");
                String fare = scanner.nextLine();
                return "PAY;" + id + ";" + fare;
            case "5":
                return "BALANCE;" + id;
            default:
                System.out.println("Invalid choice. Please try again.");
                return null;
        }
    }
}