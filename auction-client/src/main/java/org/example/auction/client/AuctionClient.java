package org.example.auction.client;

import org.example.auction.common.AuctionItem;
import org.example.auction.common.AuctionRemoteInterface;
import org.example.auction.common.User;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AuctionClient {
    private static AuctionRemoteInterface server;
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static String loggedInUser = null;
    private static boolean isAdmin = false;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            server = (AuctionRemoteInterface) registry.lookup("AuctionService");

            boolean running = true;
            while (running) {
                if (loggedInUser == null) {
                    running = showGuestMenu();
                } else if (isAdmin) {
                    showAdminMenu();
                } else {
                    showUserMenu();
                }
            }
        } catch (Exception e) {
            System.err.println("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }

    private static boolean showGuestMenu() {
        System.out.println("\nMENU GŁÓWNE");
        System.out.println("1. Zaloguj się");
        System.out.println("2. Zarejestruj się");
        System.out.println("3. Wyjdź");
        System.out.print("Wybór: ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    return false;
                default:
                    System.out.println("Nieprawidłowy wybór.");
            }
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
        return true;
    }

    private static void handleLogin() throws Exception {
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();

        if (server.login(login, password)) {
            loggedInUser = login;
            isAdmin = server.isUserAdmin(login);
            System.out.println("Zalogowano użytkownika: " + login);
        } else {
            System.out.println("Nieprawidłowe dane logowania.");
        }
    }

    private static void handleRegister() throws Exception {
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Konto administratora? (T/N): ");
        boolean isUserAdmin = scanner.nextLine().trim().equalsIgnoreCase("T");

        User newUser = new User(login, password, email, isUserAdmin);

        if (server.registerUser(newUser)) {
            System.out.println("Rejestracja zakończona. Można się zalogować.");
        } else {
            System.out.println("Błąd: Login jest już zajęty.");
        }
    }

    private static void showUserMenu() {
        System.out.println("\nMENU UŻYTKOWNIKA (" + loggedInUser + ")");
        System.out.println("1. Przeglądaj aukcje");
        System.out.println("2. Szczegóły aukcji");
        System.out.println("3. Licytuj");
        System.out.println("4. Moje licytacje");
        System.out.println("5. Wyloguj");
        System.out.print("Wybór: ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1":
                    listAuctions();
                    break;
                case "2":
                    showAuctionDetails();
                    break;
                case "3":
                    placeBid();
                    break;
                case "4":
                    myBids();
                    break;
                case "5":
                    loggedInUser = null;
                    break;
                default:
                    System.out.println("Nieprawidłowy wybór.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Błąd: Wprowadzono nieprawidłowe dane.");
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void showAdminMenu() {
        System.out.println("\nMENU ADMINISTRATORA (" + loggedInUser + ")");
        System.out.println("1. Przeglądaj aukcje");
        System.out.println("2. Szczegóły aukcji");
        System.out.println("3. Dodaj aukcję");
        System.out.println("4. Licytuj");
        System.out.println("5. Moje licytacje");
        System.out.println("6. Wyloguj");
        System.out.print("Wybór: ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1":
                    listAuctions();
                    break;
                case "2":
                    showAuctionDetails();
                    break;
                case "3":
                    addAuction();
                    break;
                case "4":
                    placeBid();
                    break;
                case "5":
                    myBids();
                    break;
                case "6":
                    loggedInUser = null;
                    break;
                default:
                    System.out.println("Nieprawidłowy wybór.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Błąd: Wprowadzono nieprawidłowe dane.");
        } catch (Exception e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }

    private static void listAuctions() throws Exception {
        List<AuctionItem> auctions = server.listAuctionItems();
        if (auctions.isEmpty()) {
            System.out.println("Brak aukcji w systemie.");
            return;
        }

        System.out.println("\nLISTA AUKCJI:");
        for (AuctionItem item : auctions) {
            String status = item.isActive() ? "Aktywna (do " + item.getEndTime().format(DATE_FORMATTER) + ")" : "Zakończona";
            String highest = item.getHighestBidder().equals("none") ? "Brak" : item.getHighestBidder();

            System.out.printf("[%d] %s - %s | Cena: %.2f | Najwyższa oferta: %s | Status: %s\n",
                    item.getId(), item.getTitle(), item.getDescription(), item.getCurrentPrice(), highest, status);
        }
    }

    private static void showAuctionDetails() throws Exception {
        System.out.print("ID aukcji: ");
        int auctionId = Integer.parseInt(scanner.nextLine());

        AuctionItem item = server.getAuctionItem(auctionId);
        if (item == null) {
            System.out.println("Brak aukcji o podanym ID.");
            return;
        }

        System.out.println("\nSZCZEGÓŁY AUKCJI [" + item.getId() + "]");
        System.out.println("Tytuł: " + item.getTitle());
        System.out.println("Opis: " + item.getDescription());

        Map<String, Double> bidders = item.getBidders();
        boolean hasBids = (bidders != null && !bidders.isEmpty());
        String currentWinner = hasBids ? item.getHighestBidder() : "Brak";

        if (item.isActive()) {
            System.out.println("Status: Aktywna (do " + item.getEndTime().format(DATE_FORMATTER) + ")");
            System.out.println("Aktualna cena: " + item.getCurrentPrice());
            System.out.println("Aktualnie wygrywa: " + currentWinner);
        } else {
            System.out.println("Status: Zakończona");
            System.out.println("Cena ostateczna: " + item.getCurrentPrice());
            System.out.println("Zwycięzca: " + currentWinner);
        }

        System.out.println("\nLISTA LICYTUJĄCYCH:");
        if (!hasBids) {
            System.out.println("Brak ofert.");
        } else {
            int position = 1;
            for (Map.Entry<String, Double> entry : bidders.entrySet()) {
                System.out.printf("%d. %s - %.2f\n", position++, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void myBids() throws Exception {
        List<AuctionItem> auctions = server.listAuctionItems();
        boolean foundAny = false;

        System.out.println("\nMOJE LICYTACJE:");
        for (AuctionItem item : auctions) {
            Map<String, Double> bidders = item.getBidders();
            if (bidders != null && bidders.containsKey(loggedInUser)) {
                foundAny = true;
                double myBid = bidders.get(loggedInUser);
                boolean amIWinning = item.getHighestBidder().equals(loggedInUser);

                System.out.printf("[%d] %s\n", item.getId(), item.getTitle());

                if (item.isActive()) {
                    System.out.println("   Status: Aktywna (do " + item.getEndTime().format(DATE_FORMATTER) + ")");
                    if (amIWinning) {
                        System.out.printf("   Prowadzisz z kwotą %.2f\n", myBid);
                    } else {
                        System.out.printf("   Przebito! Twoja oferta: %.2f, Aktualna cena: %.2f\n", myBid, item.getCurrentPrice());
                    }
                } else {
                    System.out.println("   Status: Zakończona");
                    if (amIWinning) {
                        System.out.printf("   Wynik: WYGRANA za kwotę %.2f\n", item.getCurrentPrice());
                    } else {
                        System.out.printf("   Wynik: PRZEGRANA. Twoja oferta: %.2f, Sprzedano za: %.2f\n", myBid, item.getCurrentPrice());
                    }
                }
            }
        }

        if (!foundAny) {
            System.out.println("Nie brałeś udziału w żadnych aukcjach.");
        }
    }

    private static void placeBid() throws Exception {
        System.out.print("ID aukcji: ");
        int auctionId = Integer.parseInt(scanner.nextLine());

        AuctionItem item = server.getAuctionItem(auctionId);
        if (item == null) {
            System.out.println("Brak aukcji o podanym ID.");
            return;
        }

        System.out.println("Aktualna cena: " + item.getCurrentPrice());
        System.out.print("Kwota licytacji: ");
        double amount = Double.parseDouble(scanner.nextLine());

        if (server.placeBid(auctionId, loggedInUser, amount)) {
            System.out.println("Złożono ofertę.");
        } else {
            System.out.println("Odrzucono ofertę (zbyt niska kwota lub aukcja zakończona).");
        }
    }

    private static void addAuction() throws Exception {
        System.out.print("Tytuł: ");
        String title = scanner.nextLine();
        System.out.print("Opis: ");
        String description = scanner.nextLine();
        System.out.print("Cena początkowa: ");
        double startingPrice = Double.parseDouble(scanner.nextLine());
        System.out.print("Czas trwania (minuty): ");
        int durationMinutes = Integer.parseInt(scanner.nextLine());

        AuctionItem newItem = new AuctionItem(0, title, description, startingPrice, durationMinutes);

        if (server.addAuctionItem(loggedInUser, newItem) != null) {
            System.out.println("Aukcja została dodana.");
        } else {
            System.out.println("Błąd podczas dodawania aukcji.");
        }
    }
}