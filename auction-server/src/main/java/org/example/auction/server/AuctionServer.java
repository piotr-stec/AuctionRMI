package org.example.auction.server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class AuctionServer {
    public static void main(String[] args) {
        try {
            AuctionServiceImpl auctionService = new AuctionServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AuctionService", auctionService);
            System.out.println("Serwer aukcji uruchomiony. Nacisnij Ctrl+C aby zatrzymac.");
            Thread.currentThread().join(); // block forever — keeps JVM alive
        } catch (InterruptedException e) {
            System.out.println("Serwer zatrzymany.");
        } catch (Exception e) {
            System.err.println("Blad serwera: " + e.getMessage());
            e.printStackTrace();
        }
    }
}