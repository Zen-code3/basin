package Customers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CartStorage {

    static final class Item {
        final int productId;
        final String name;
        final double price;
        int quantity;

        Item(int productId, String name, double price, int quantity) {
            this.productId = productId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        double getSubtotal() {
            return price * quantity;
        }
    }

    private static final List<Item> ITEMS = new ArrayList<>();

    private CartStorage() {
    }

    static synchronized void addItem(int productId, String name, double price, int quantity) {
        for (Item it : ITEMS) {
            if (it.productId == productId) {
                it.quantity += quantity;
                return;
            }
        }
        ITEMS.add(new Item(productId, name, price, quantity));
    }

    static synchronized List<Item> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(ITEMS));
    }

    static synchronized void clear() {
        ITEMS.clear();
    }

    static synchronized double getTotal() {
        double sum = 0;
        for (Item it : ITEMS) {
            sum += it.getSubtotal();
        }
        return sum;
    }
}

