package com.app.shared.models.item.Creator;

import com.app.shared.models.item.Electronics;
import com.app.shared.models.item.Item;

public class ElectronicCreator implements ItemCreator {
    @Override
    public Item createItem(String name, String desc, double startingPrice, String sellerId) {
        return new Electronics(name, desc, startingPrice, sellerId);
    }
}
