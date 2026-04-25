import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class MetroServer {
    private static final int PORT = 8888;
    // База даних карток (потокобезпечна)
    private static ConcurrentHashMap<String, MetroCard> cardDatabase = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                // Створення нового потоку для клієнта
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Внутрішній клас для обробки запитів клієнта
    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request;
                while ((request = in.readLine()) != null) {
                    String response = processRequest(request);
                    out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }
        }

        private String processRequest(String request) {
            // Формат: КОМАНДА;ID;Параметр1;...
            String[] parts = request.split(";");
            String command = parts[0];
            String id = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "ISSUE":
                    if (parts.length < 3) return "ERROR: Not enough data for registration.";
                    String name = parts[2];
                    if (cardDatabase.containsKey(id)) {
                        return "ERROR: Card with such ID already exists.";
                    }
                    cardDatabase.put(id, new MetroCard(id, name));
                    return "SUCCESS: Card issued to student " + name;

                case "INFO":
                    MetroCard infoCard = cardDatabase.get(id);
                    return infoCard != null ? "INFO: " + infoCard.toString() : "ERROR: Card not found.";

                case "ADD":
                    if (parts.length < 3) return "ERROR: Amount not specified.";
                    MetroCard addCard = cardDatabase.get(id);
                    if (addCard == null) return "ERROR: Card not found.";
                    try {
                        double amount = Double.parseDouble(parts[2]);
                        addCard.addFunds(amount);
                        return "SUCCESS: Account topped up. New balance: " + addCard.getBalance();
                    } catch (NumberFormatException e) {
                        return "ERROR: Invalid amount format.";
                    }

                case "PAY":
                    if (parts.length < 3) return "ERROR: Ride fare not specified.";
                    MetroCard payCard = cardDatabase.get(id);
                    if (payCard == null) return "ERROR: Card not found.";
                    try {
                        double fare = Double.parseDouble(parts[2]);
                        if (payCard.payTrip(fare)) {
                            return "SUCCESS: Payment successful. Remaining balance: " + payCard.getBalance();
                        } else {
                            return "ERROR: Insufficient funds on the card!";
                        }
                    } catch (NumberFormatException e) {
                        return "ERROR: Invalid fare format.";
                    }

                case "BALANCE":
                    MetroCard balCard = cardDatabase.get(id);
                    return balCard != null ? "BALANCE: " + balCard.getBalance() + " UAH" : "ERROR: Card not found.";

                default:
                    return "ERROR: Unknown command.";
            }
        }
    }
}