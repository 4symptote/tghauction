# tGauction

Online auction system for LTNC 2026. The project is split into three Maven modules:

- `shared`: serializable domain models, exceptions, network request/response contracts, and payload DTOs.
- `server`: socket server, auction services, concurrency control, observer broadcasting, and MongoDB-backed persistence.
- `client`: JavaFX MVC client for login, listing auctions, creating auctions, placing bids, and receiving realtime updates.

## Features

- User login flow with server validation.
- Auction item hierarchy: `Item`, `Electronics`, `Art`, `Vehicle`.
- User hierarchy: `User`, `Bidder`, `Seller`, `Admin`.
- Auction lifecycle: `OPEN`, `RUNNING`, `FINISHED`, `PAID`, `CANCELED`.
- Factory Method for item creation.
- Singleton services for auction and bid management.
- Observer Pattern for realtime auction updates over sockets.
- Concurrent bid handling with per-auction `ReentrantLock`.
- Custom exceptions for invalid bids, closed auctions, and authentication failures.
- MongoDB document persistence for users and auctions, with local serialized file fallback when MongoDB is unavailable.
- Anti-sniping: bids inside the last 30 seconds extend the auction by 60 seconds.
- JUnit test coverage for auction state, invalid bids, and observer notification.
- GitHub Actions CI configured with Maven.

## Run

MongoDB configuration:

Create a `.env` file in the project root:

```text
MONGODB_URI=mongodb+srv://YOUR_USERNAME:YOUR_PASSWORD@YOUR_CLUSTER.mongodb.net/auction_db?retryWrites=true&w=majority&appName=tGhauction
MONGODB_DATABASE=auction_db
```

You can also set the same values through PowerShell environment variables:

```powershell
$env:MONGODB_URI="mongodb://localhost:27017"
$env:MONGODB_DATABASE="auction_db"
```

Environment variables take priority over `.env`. If neither is set, the server tries `mongodb://localhost:27017` and database `auction_db`. If MongoDB is unavailable, the app falls back to local files in `server/data/`.

Start the server first:

```powershell
mvn -pl shared,server -am compile
mvn -pl server exec:java -Dexec.mainClass=com.app.server.ServerMain
```

Start the client in another terminal:

```powershell
mvn -pl shared,client -am compile
mvn -pl client javafx:run
```

If running from IntelliJ, run `com.app.server.ServerMain` first, then run `com.app.client.Launcher`.

## Demo Flow

1. Start the server.
2. Start two client windows.
3. Register or login with a real username and password.
4. Create an auction from one client.
5. Refresh or watch the second client receive the realtime update.
6. Place bids from both clients and confirm the price and highest bidder update.

## Requirements Mapping

- Client-Server + Socket: `AuctionServer`, `ClientHandler`, `NetworkClient`.
- MVC + JavaFX + FXML: `LoginController`, `AuctionListController`, `AuthModel`, `AuctionListModel`, `LoginView.fxml`, `AuctionListView.fxml`.
- Data storage: MongoDB collections `users` and `auctions`; fallback files are `server/data/users.ser` and `server/data/auctions.ser`.

## MongoDB Collections

`users` documents include:

- `_id`, `username`, `passwordHash`, `email`, `role`
- `balance` for bidders
- `totalRevenue` for sellers

`auctions` documents include:

- `_id`, `startTime`, `endTime`, `currentPrice`, `status`, `highestBidderId`
- embedded `item` document with `id`, `type`, `name`, `description`, `startingPrice`, `currentHighestBid`, `sellerId`
- embedded `bids` array with `id`, `auctionId`, `bidderId`, `amount`, `timestamp`
- Concurrency: `BidService` locks per auction before validating and applying bids.
- Design patterns: Singleton, Factory Method, Observer.
- CI/CD: `.github/workflows/ci.yml`.
