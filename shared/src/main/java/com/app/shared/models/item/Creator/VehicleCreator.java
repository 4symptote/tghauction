package com.app.shared.models.item.Creator;

import com.app.shared.models.item.Item;
import com.app.shared.models.item.Vehicle;

public class VehicleCreator implements ItemCreator {
    @Override
    public Item createItem(String name, String desc, double startingPrice, String sellerId) {
        return new Vehicle(name, desc, startingPrice, sellerId);
    }
}
