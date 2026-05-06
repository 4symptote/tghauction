
import com.app.server.network.ClientManager;
import com.app.server.service.AuctionManager;
import com.app.server.service.BidService;
import com.app.shared.exceptions.AuctionClosedException;
import com.app.shared.exceptions.InvalidBidException;
import com.app.shared.models.auction.Auction;
import com.app.shared.models.item.Creator.ElectronicCreator;
import com.app.shared.models.item.Item;
import com.app.shared.network.AuctionObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class BidServiceTest {

    private AuctionManager auctionManager;
    private BidService bidService;
    private ClientManager clientManager;
    private Item testItem;

    @BeforeEach
    public void setUp() {
        auctionManager = AuctionManager.getInstance();
        bidService = BidService.getInstance();
        clientManager = ClientManager.getInstance();

        testItem = new ElectronicCreator().createItem("Smartphone", "Latest Model", 500.0, "seller1");
    }

    // observer
    @Test
    public void testObserverNotifiedOnNewBid() throws InvalidBidException, AuctionClosedException {
        Auction auction = new Auction(testItem, 20000); // 20 sec duration
        auctionManager.addAuction(auction);

        // Dummy Observer
        AtomicBoolean wasNotified = new AtomicBoolean(false);
        AuctionObserver testObserver = updatedAuction -> {
            if (updatedAuction.getId().equals(auction.getId())) {
                wasNotified.set(true);
            }
        };
        clientManager.addClient(testObserver);

        // Place a valid bid => Should trigger broadcastAuctionUpdate
        bidService.placeBid(auction.getId(), "bidder1", 600.0);

        assertTrue(wasNotified.get(), "Observer should be notified upon a new valid bid.");

        clientManager.removeClient(testObserver);
        auctionManager.removeAuction(auction.getId());
    }

    //exceptions
    @Test
    public void testInvalidBidAmount() {
        Auction auction = new Auction(testItem, 20000);
        auctionManager.addAuction(auction);

        // Expect Exception because starting price is 500, but bid is 400
        assertThrows(InvalidBidException.class, () -> {
            bidService.placeBid(auction.getId(), "bidder1", 400.0);
        }, "Should throw InvalidBidException when bid is lower than current price.");

        auctionManager.removeAuction(auction.getId());
    }

    // State logic
    @Test
    public void testStateChangeLogic() {
        long currentTime = System.currentTimeMillis();

        // OPEN state (starts in future)
        Auction auction = new Auction(testItem, currentTime + 5000, currentTime + 10000);
        assertEquals(Auction.Status.OPEN, auction.getStatus(), "Auction should be OPEN before start time.");
        auctionManager.addAuction(auction);

        // Bidding while OPEN -> AuctionClosedException
        assertThrows(AuctionClosedException.class, () -> {
            bidService.placeBid(auction.getId(), "bidder1", 600.0);
        });

        // RUNNING state
        auction.setStartTime(currentTime - 1000); // Shift start time to passed
        assertEquals(Auction.Status.RUNNING, auction.getStatus(), "Auction should be RUNNING now.");
        // No exception thrown
        assertDoesNotThrow(() -> {
            bidService.placeBid(auction.getId(), "bidder1", 600.0);
        });

        // FINISHED state
        auction.setEndTime(currentTime - 500); // Shift end time to passed
        assertEquals(Auction.Status.FINISHED, auction.getStatus(), "Auction should be FINISHED after end time.");
        assertThrows(AuctionClosedException.class, () -> {
            bidService.placeBid(auction.getId(), "bidder2", 700.0);
        });

        auctionManager.removeAuction(auction.getId());
    }
}