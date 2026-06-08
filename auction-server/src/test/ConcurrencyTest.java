// package org.example.auction.server;

// import org.example.auction.common.AuctionItem;
// import org.example.auction.common.User;

// import java.rmi.RemoteException;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.atomic.AtomicInteger;

// public class ConcurrencyTest {

//    public static void main(String[] args) throws RemoteException, InterruptedException {
//        AuctionServiceImpl server = new AuctionServiceImpl() {
//        };

//        server.registerUser(new User("admin", "admin123", "admin@test", true));
//        for (int i = 1; i <= 10; i++) {
//            server.registerUser(new User("user" + i, "pass", "user@test", false));
//        }

//        AuctionItem item = new AuctionItem(0, "Testowy Przedmiot", "Opis", 100.0, 60);
//        AuctionItem addedItem = server.addAuctionItem("admin", item);
//        int auctionId = addedItem.getId();

//        int threads = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threads);

//        CountDownLatch readyLatch = new CountDownLatch(threads); 
//        CountDownLatch startLatch = new CountDownLatch(1);       
//        CountDownLatch doneLatch = new CountDownLatch(threads);  

//        AtomicInteger successfulBids = new AtomicInteger(0);

     
//        for (int i = 1; i <= threads; i++) {
//            final String userLogin = "user" + i;
//            final double bidAmount = 100.0 + i; 

//            executor.submit(() -> {
//                try {
//                    readyLatch.countDown(); 
//                    startLatch.await();     

//                    boolean success = server.placeBid(auctionId, userLogin, bidAmount);
//                    if (success) {
//                        successfulBids.incrementAndGet();
//                    }
//                } catch (Exception e) {
//                    System.err.println("Błąd wątku: " + e.getMessage());
//                } finally {
//                    doneLatch.countDown();  
//                }
//            });
//        }

//        System.out.println("Przygotowywanie 10 wątków...");
//        readyLatch.await(); 

//        System.out.println("START! Wszystkie wątki licytują.");
//        startLatch.countDown();

//        doneLatch.await(); 

//        // Weryfikacja wyników
//        AuctionItem finalItem = server.getAuctionItem(auctionId);
//        System.out.println("\n WYNIKI TESTU WSPÓŁBIEŻNOŚCI");
//        System.out.println("Cena końcowa: " + finalItem.getCurrentPrice() + " (Oczekiwano: 110.0)");
//        System.out.println("Zwycięzca:    " + finalItem.getHighestBidder() + " (Oczekiwano: user10)");
//        System.out.println("Udane oferty: " + successfulBids.get() + " (Może być mniej niż 10)");

//        executor.shutdown();
//        System.exit(0); 
//    }
// }