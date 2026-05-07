package com.app.server.dao;

import com.app.shared.models.auction.Auction;
import com.app.shared.models.auction.BidTransaction;

public interface AuctionDao {
    //Lưu phiên đấu giá mới vào DB
    void createAuction(Auction auction);

    //Cập nhật lượt Bid mới
    void addBid(String auctionId, BidTransaction bid);

    //Cập nhật trạng thái
    void updateAuctionStatus(String auctionId, Auction.Status status);

    //Gia hạn thời gian kết thúc
    void updateAuctionEndTime(String auctionId, long newEndTime);
}