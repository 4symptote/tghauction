package com.app.shared.models.item.Creator;

import com.app.shared.models.item.Item;

import java.util.Locale;
import java.util.Map;

public final class ItemFactory {
    private static final Map<String, ItemCreator> CREATORS = Map.of(
            "art", new ArtCreator(),
            "vehicle", new VehicleCreator(),
            "electronics", new ElectronicCreator(),
            "electronic", new ElectronicCreator()
    );

    private ItemFactory() {
    }

    public static Item createItem(
            String itemType,
            String name,
            String description,
            double startingPrice,
            String sellerId
    ) {
        String normalizedType = normalizeType(itemType);
        ItemCreator creator = CREATORS.get(normalizedType);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported item type: " + itemType);
        }

        return creator.createItem(name, description, startingPrice, sellerId);
    }

    private static String normalizeType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return "electronics";
        }
        return itemType.trim().toLowerCase(Locale.ROOT);
    }
}
