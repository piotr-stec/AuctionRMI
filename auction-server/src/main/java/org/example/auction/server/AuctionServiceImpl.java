package org.example.auction.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;

import org.example.auction.common.AuctionItem;
import org.example.auction.common.AuctionRemoteInterface;
import org.example.auction.common.User;

public class AuctionServiceImpl extends UnicastRemoteObject implements AuctionRemoteInterface {
    private UserRepository userRepository;
    private AuctionRepository auctionRepository;

    protected AuctionServiceImpl() throws RemoteException {
        super();
        userRepository = new UserRepository();
        auctionRepository = new AuctionRepository();
        startAuctionTimer();
    }

    private void startAuctionTimer() {
        Thread timer = new Thread(() -> {
            while (true) {
                try {
                    checkExpiredAuctions();
                    Thread.sleep(1000); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        timer.setDaemon(true); 
        timer.start();
    }

    @Override
    public boolean registerUser(User user) throws RemoteException {
        boolean success = userRepository.addUser(user);
        if (success) {
            System.out.println("Zarejestrowano użytkownika: " + user.getLogin());
        } else {
            System.out.println("Nie można zarejestrować użytkownika (login zajęty): " + user.getLogin());
        }
        return success;
    }

    @Override
    public boolean login(String login, String password) throws RemoteException {
        boolean authenticated = userRepository.authenticate(login, password);
        if (authenticated) {
            System.out.println("Użytkownik zalogowany: " + login);
        } else {
            System.out.println("Nieudana próba logowania: " + login);
        }
        return authenticated;
    }

    @Override
    public boolean isUserAdmin(String username) throws RemoteException {
        User user = userRepository.getUser(username);
        return user != null && user.isAdmin();
    }

    @Override
    public AuctionItem addAuctionItem(String username, AuctionItem auctionItem) throws RemoteException {
        User user = userRepository.getUser(username);
        if (user == null) {
            System.out.println("Nie znaleziono uzytkownika: " + username);
            return null;
        }
        if (!user.isAdmin()) {
            System.out.println("Nieautoryzowana próba dodania przedmiotu przez: " + username);
            return null;
        }
        return auctionRepository.addAuction(auctionItem);
    }

    @Override
    public List<AuctionItem> listAuctionItems() throws RemoteException {
        return auctionRepository.getAllAuctions();
    }

    @Override
    public AuctionItem getAuctionItem(int id) throws RemoteException {
        return auctionRepository.getAuction(id);
    }

    @Override
    public boolean placeBid(int auctionId, String userLogin, double amount) throws RemoteException {
        AuctionItem item = auctionRepository.getAuction(auctionId);
        if (item == null) {
            System.out.println("Nie znaleziono aukcji o id: " + auctionId);
            return false;
        }
        boolean successBid = false;
        synchronized (item) {
            successBid = item.addBid(userLogin, amount);
        }
        if (successBid) {
            System.out.println("Użytkownik " + userLogin + " złożył ofertę " + amount + " na aukcji " + auctionId);
            auctionRepository.saveToFile();
        }
        return successBid;
    }

    @Override
    public String getWinner(int auctionId) throws RemoteException {
        AuctionItem item = auctionRepository.getAuction(auctionId);
        if (item == null) {
            System.out.println("Nie znaleziono aukcji o id: " + auctionId);
            return null;
        }
        if (item.isActive()) {
            System.out.println("Aukcja " + auctionId + " nadal aktywna. Nie można pobrać zwycięzcy.");
            return null;
        }
        return item.getHighestBidder();
    }

    private void checkExpiredAuctions() {
        for (AuctionItem item : auctionRepository.getAllAuctions()) {
            synchronized (item) {
                if (item.isActive() && LocalDateTime.now().isAfter(item.getEndTime())) {
                    item.setActive(false);
                    System.out.println("Aukcja zakonczona: " + item.getTitle());
                    System.out.println("Zwyciezca: " + item.getHighestBidder());
                    System.out.println("Cena koncowa: " + item.getCurrentPrice() + "zl");
                }
            }
        }
        auctionRepository.saveToFile();
    }

}
