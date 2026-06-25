# AuctionRMI

A distributed auction system built with Java RMI as a concurrent programming coursework project. The application demonstrates key concurrency concepts — critical sections, thread-safe data structures, and atomic operations — in the context of a real-time, multi-client bidding platform.

## Overview

The system allows multiple clients to connect simultaneously to a central auction server and place bids on active items. The server enforces correct concurrent access so that simultaneous bids from many clients always produce a consistent result: only the highest valid bid wins, with no race conditions or data corruption.

## Architecture

The project is a Maven multi-module build composed of three modules:

| Module | Responsibility |
|---|---|
| `auction-common` | Shared model classes (`AuctionItem`, `User`) and the `AuctionRemoteInterface` RMI contract |
| `auction-server` | RMI server implementation, repositories, auction timer thread |
| `auction-client` | Command-line client that connects to the server via RMI registry |

## Concurrency Design

Concurrent access to shared state is the central concern of this project. The following mechanisms are used:

**Critical sections (`synchronized`)**  
The `placeBid` method in `AuctionServiceImpl` synchronizes on the individual `AuctionItem` object. This ensures that when multiple clients bid on the same auction simultaneously, the price update and highest-bidder assignment are performed atomically. The expired-auction checker uses the same per-item lock to safely transition auction state.

```java
synchronized (item) {
    successBid = item.addBid(userLogin, amount);
}
```

**Thread-safe collections (`ConcurrentHashMap`)**  
`AuctionRepository` stores all auctions in a `ConcurrentHashMap`, allowing concurrent reads from multiple client threads without blocking.

**Atomic ID generation (`AtomicInteger`)**  
Auction IDs are generated using `AtomicInteger.getAndIncrement()`, guaranteeing unique IDs under concurrent auction creation without requiring a lock.

**Background daemon thread**  
A daemon thread runs on the server and checks every second for auctions whose time has expired, closing them automatically and persisting the result to disk.

**Barrier-based concurrency test (`CountDownLatch`)**  
The test suite includes a scenario where 10 threads are prepared simultaneously and released at exactly the same instant using a `CountDownLatch` barrier, verifying that the server handles a burst of concurrent bids correctly.

## API

The remote interface exposes the following operations:

```java
boolean registerUser(User user)
boolean login(String login, String password)
boolean isUserAdmin(String username)
AuctionItem addAuctionItem(String username, AuctionItem item)
List<AuctionItem> listAuctionItems()
AuctionItem getAuctionItem(int id)
boolean placeBid(int auctionId, String userLogin, double amount)
String getWinner(int auctionId)
```

## Persistence

Auctions and users are persisted to CSV files (`auctions.csv`, `users.csv`) on the server's filesystem. State is loaded on startup and saved after every bid and every auction closure, so the system survives server restarts.

## Getting Started

**Prerequisites:** Java 11+, Maven 3.6+

All commands must be run from the **project root** (`AuctionRMI/`).

**Step 1 — install all modules** (required once, or after changing `auction-common`):

```bash
mvn install -DskipTests
```

**Step 2 — start the server:**

```bash
mvn exec:java -pl auction-server
```

**Step 3 — start a client** (in a separate terminal):

```bash
mvn exec:java -pl auction-client
```

The server registers itself in the local RMI registry on port `1099` by default.

## Running the Concurrency Test

```bash
mvn test -pl auction-server -am
```

The test (`ConcurrencyTest`) spins up 10 threads, synchronizes their start with a `CountDownLatch`, fires all bids simultaneously, and then asserts:

- The final price equals the highest submitted bid (110.0)
- The winner is the user who submitted that bid (`user10`)
- At least one bid was accepted

## Technology Stack

- Java 11
- Java RMI (`java.rmi`)
- Maven (multi-module)
- JUnit Jupiter 5.10 (test)
- `ConcurrentHashMap`, `AtomicInteger`, `synchronized`, `CountDownLatch`, `ExecutorService`
