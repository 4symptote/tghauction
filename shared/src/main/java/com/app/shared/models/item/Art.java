package com.app.shared.models.item;

public class Art extends Item {
    private String artist;

    public Art(String name, String description, double startingPrice, String sellerId) {
        super(name, description, startingPrice, sellerId);
    }

    public String getArtist() { return artist; }

    public Art setArtist(String artist) {
        this.artist = artist;
        return this;
    }
}
