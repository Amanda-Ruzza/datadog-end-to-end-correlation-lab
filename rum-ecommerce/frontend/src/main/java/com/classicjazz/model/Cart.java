package com.classicjazz.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return items;
    }

    public void add(CartItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public double getTotal() {
        return items.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    public record CartItem(String sku, String title, double price, int quantity) implements Serializable {
        private static final long serialVersionUID = 1L;

        public double getLineTotal() {
            return price * quantity;
        }
    }
}
