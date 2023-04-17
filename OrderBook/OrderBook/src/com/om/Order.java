package com.om;

import java.util.Objects;

public class Order {
    long price;
    long quantity;
    long filled;
    boolean isBuy;
    boolean isCancelled;
    long orderId;

    public Order(long price, long quantity, boolean isBuy) {
        this.price = price;
        this.quantity = quantity;
        this.isBuy = isBuy;
        this.filled = 0;
        this.isCancelled = false;
    }

    long remain_qty() { return quantity - filled; }
    void reduce_qty(long qty) {
        this.quantity -= qty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return orderId == order.orderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
