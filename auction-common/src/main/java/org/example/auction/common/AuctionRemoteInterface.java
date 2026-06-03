package org.example.auction.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuctionRemoteInterface extends Remote {
    // user
    boolean registerUser(User user) throws RemoteException;

    boolean login(String login, String password) throws RemoteException;

    boolean isUserAdmin(String username) throws RemoteException;

    // auctions
    AuctionItem addAuctionItem(String username, AuctionItem auctionItem) throws RemoteException;

    List<AuctionItem> listAuctionItems() throws RemoteException;

    AuctionItem getAuctionItem(int id) throws RemoteException;

    boolean placeBid(int auctionId, String userLogin, double amount) throws RemoteException;

    String getWinner(int auctionId) throws RemoteException;

}