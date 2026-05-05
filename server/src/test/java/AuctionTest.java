import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;
import com.app.shared.models.item.Creator.*;
import com.app.shared.models.item.Item;
import com.app.shared.models.user.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {

    private Auction auction;
    private Item testItem;

    @BeforeEach
    public void setUp() {
        ItemCreator creator = new ElectronicCreator();
        Seller sellerA = new Seller("Dan", "Dan1234", "Dan1234@uet.vnu.edu");
        testItem = creator.createItem("Laptop", "Gaming Laptop", 1000.0, sellerA.getId());
    }


    @Test
    public void testAuctionInitialization() {
        auction = new Auction(testItem, 3000);
        assertEquals(testItem, auction.getItem());
        assertEquals(1000.0, auction.getCurrentPrice());
    }

    @Test
    public void test() {
        auction = new Auction(testItem, 100000);

        User bidderA = new Bidder("Mark", "mark2222", "mark@google.com");

        BidTransaction newBid = new BidTransaction(auction.getId(), bidderA.getId(), 10000);

        auction.addBid(newBid);

        BidTransaction thisBid = auction.getBids().getFirst();
        LocalDateTime thisBidTimeStamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(thisBid.getTimestamp()), ZoneId.systemDefault());

        System.out.println(thisBid.getBidderId());
        System.out.println(bidderA.getId());
    }

//    @Test
//    public void testForAuctionDuration() throws InterruptedException {
//        Thread.sleep(3000);
//        auction = new Auction(testItem, 3000);
//        System.out.println(auction.getStatus());
//        System.out.println("Auction opened for 3 seconds \nPerform a check after 5 seconds of opening\nwhen the auction is already finished");
//        Thread.sleep(5000);
//        System.out.println(auction.getStatus());
//
//        assertEquals(Auction.Status.FINISHED, auction.getStatus());
//    }


}