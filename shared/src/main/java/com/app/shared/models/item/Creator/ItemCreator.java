package com.app.shared.models.item.Creator;

import com.app.shared.models.item.Item;

public interface ItemCreator {

    public Item createItem(String name, String desc, double startingPrice, String sellerId);

}
