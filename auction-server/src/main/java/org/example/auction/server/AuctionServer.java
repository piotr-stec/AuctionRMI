package org.example.auction.server;

import java.io.IOException;

import java.rmi.registry.Registry;

import java.rmi.registry.LocateRegistry;

public class AuctionServer {
    public static void main(String[] args) throws IOException {
        try {
            AuctionServiceImpl auctionService = new AuctionServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AuctionService", auctionService);
            System.out.println("Serwer aukcji uruchomiony.");
        } catch (Exception e) {
            System.err.println("Błąd serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }
}