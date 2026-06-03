package org.example.auction.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private double currentPrice;
    private String highestBidder;
    private Map<String, Double> bidders;
    private LocalDateTime endTime;
    private boolean active;

    public AuctionItem() {
    }

    public AuctionItem(int id, String title, String description,
            double startPrice, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentPrice = startPrice;
        this.highestBidder = "none";
        this.bidders = new LinkedHashMap<>();
        this.endTime = LocalDateTime.now().plusMinutes(durationMinutes);
        this.active = true;
    }

    public boolean addBid(String userLogin, double amount) {
        if (!active)
            return false;
        if (amount <= currentPrice)
            return false;

        bidders.put(userLogin, amount);

        bidders = bidders.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));

        Map.Entry<String, Double> highest = bidders.entrySet().iterator().next();
        currentPrice = highest.getValue();
        highestBidder = highest.getKey();

        return true;
    }

    public String toCSV() {
        String biddersStr = bidders.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(";"));

        return id + "," + title + "," + description + "," +
                currentPrice + "," + highestBidder + "," +
                biddersStr + "," + endTime + "," + active;
    }

    public static AuctionItem fromCSV(String line) {
        String[] parts = line.split(",", 8);
        AuctionItem item = new AuctionItem();
        item.id = Integer.parseInt(parts[0]);
        item.title = parts[1];
        item.description = parts[2];
        item.currentPrice = Double.parseDouble(parts[3]);
        item.highestBidder = parts[4];
        item.bidders = new LinkedHashMap<>();
        if (!parts[5].isEmpty()) {
            for (String s : parts[5].split(";")) {
                String[] b = s.split(":");
                item.bidders.put(b[0], Double.parseDouble(b[1]));
            }
        }
        item.endTime = LocalDateTime.parse(parts[6]);
        item.active = Boolean.parseBoolean(parts[7]);
        return item;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public Map<String, Double> getBidders() {
        return bidders;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AuctionItem{id=" + id +
                ", title='" + title + "'" +
                ", currentPrice=" + currentPrice +
                ", highestBidder='" + highestBidder + "'" +
                ", active=" + active + "}";
    }
}