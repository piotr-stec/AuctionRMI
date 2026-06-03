package org.example.auction.server;

import org.example.auction.common.AuctionItem;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionRepository {
    private static final String FILE = "auctions.csv";
    private final Map<Integer, AuctionItem> auctions = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public AuctionRepository() {
        loadFromFile();
    }

    public AuctionItem addAuction(AuctionItem item) {
        int id = nextId.getAndIncrement();
        item.setId(id);
        auctions.put(id, item);
        saveToFile();
        return item;
    }

    public AuctionItem getAuction(int id) {
        return auctions.get(id);
    }

    public List<AuctionItem> getAllAuctions() {
        return new java.util.ArrayList<>(auctions.values());
    }

    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (AuctionItem item : auctions.values()) {
                pw.println(item.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu auctions.csv: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File file = new File(FILE);
        if (!file.exists())
            return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    AuctionItem item = AuctionItem.fromCSV(line);
                    auctions.put(item.getId(), item);
                    if (item.getId() >= nextId.get()) {
                        nextId.set(item.getId() + 1);
                    }
                }
            }
            System.out.println("Wczytano " + auctions.size() + " aukcji.");
        } catch (IOException e) {
            System.err.println("Błąd odczytu auctions.csv: " + e.getMessage());
        }
    }
}