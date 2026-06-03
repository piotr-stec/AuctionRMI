package org.example.auction.client;

import java.io.IOException;
import java.rmi.Naming;

import org.example.auction.common.AuctionItem;
import org.example.auction.common.AuctionRemoteInterface;
import org.example.auction.common.User;

public class AuctionClient {
    public static void main(String[] args) throws Exception {
        AuctionRemoteInterface service = (AuctionRemoteInterface) Naming.lookup("rmi://localhost:1099/AuctionService");

        User user = new User("janAdmin", "haslo123", "jan@wp.pl", true);
        boolean result = service.registerUser(user);
        if (result) {
            System.out.println("Użytkownik zarejestrowany: " + user.getLogin());
        } else {
            System.out.println("Nie można zarejestrować użytkownika (login zajęty): " + user.getLogin());
        }

        AuctionItem item = new AuctionItem(0, "Laptop", "Nowy laptop gamingowy", 3000.0, 2);
        try {
            AuctionItem addedItem = service.addAuctionItem(user.getLogin(), item);
            if (addedItem == null) {
                System.out.println("Nie dodano aukcji (brak uprawnien lub blad serwera).");
                return;
            }
            System.out.println("Dodano aukcje: " + addedItem.getTitle());

        } catch (IOException e) {
            System.err.println("Błąd podczas dodawania aukcji: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        System.out.println("Lista aukcji:");
        for (AuctionItem ai : service.listAuctionItems()) {
            printAuction(ai);
        }
        // Pokazuj aukcje co sekundę 
        while (true) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println("Aktualne aukcje:");
            for (AuctionItem ai : service.listAuctionItems()) {
                printAuction(ai);
            }
            Thread.sleep(1000);
        }
    }

    private static void printAuction(AuctionItem ai) {
        System.out.println(ai.getId() + ": " + ai.getTitle());
        System.out.println("Opis: " + ai.getDescription());
        System.out.println("Cena: " + ai.getCurrentPrice() + " zl");
        System.out.println("Aktywna: " + ai.isActive());
        System.out.println("Licytujacy: " + formatBidders(ai));
        System.out.println();
    }

    private static String formatBidders(AuctionItem ai) {
        if (ai.getBidders() == null || ai.getBidders().isEmpty()) {
            return "brak";
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Double> entry : ai.getBidders().entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}