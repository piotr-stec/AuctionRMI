package org.example.auction.server;

import org.example.auction.common.AuctionItem;
import org.example.auction.common.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test współbieżności licytacji")
public class ConcurrencyTest {

    private AuctionServiceImpl server;
    private int auctionId;

    @BeforeEach
    void setUp() throws RemoteException {
        server = new AuctionServiceImpl() {};

        server.registerUser(new User("admin", "admin123", "admin@test", true));
        for (int i = 1; i <= 10; i++) {
            server.registerUser(new User("user" + i, "pass", "user@test", false));
        }

        AuctionItem item = new AuctionItem(0, "Testowy Przedmiot", "Opis", 100.0, 60);
        AuctionItem addedItem = server.addAuctionItem("admin", item);
        auctionId = addedItem.getId();
    }

    @Test
    @DisplayName("10 wątków licytuje jednocześnie – wygrywa najwyższa oferta")
    void testConcurrentBidding() throws InterruptedException, RemoteException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch readyLatch = new CountDownLatch(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threads);

        AtomicInteger successfulBids = new AtomicInteger(0);

        for (int i = 1; i <= threads; i++) {
            final String userLogin = "user" + i;
            final double bidAmount  = 100.0 + i;   // user10 oferuje 110.0 – najwyżej

            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    boolean success = server.placeBid(auctionId, userLogin, bidAmount);
                    if (success) {
                        successfulBids.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail("Wątek rzucił wyjątek: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("Przygotowywanie " + threads + " wątków...");
        readyLatch.await();

        System.out.println("START! Wszystkie wątki licytują jednocześnie.");
        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        // --- Asercje ---
        AuctionItem finalItem = server.getAuctionItem(auctionId);

        System.out.println("\n=== WYNIKI TESTU WSPÓŁBIEŻNOŚCI ===");
        System.out.println("Cena końcowa: " + finalItem.getCurrentPrice());
        System.out.println("Zwycięzca:    " + finalItem.getHighestBidder());
        System.out.println("Udane oferty: " + successfulBids.get());

        assertEquals(110.0, finalItem.getCurrentPrice(), 0.001,
                "Cena końcowa powinna wynosić 110.0 (oferta user10)");
        assertEquals("user10", finalItem.getHighestBidder(),
                "Zwycięzcą powinien być user10 z najwyższą ofertą");
        assertTrue(successfulBids.get() >= 1,
                "Co najmniej jedna oferta powinna zostać przyjęta");
    }
}
