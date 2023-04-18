package com.om;

public class Level {
    long price;
    long quantity;
    long count;

    public Level(long price) {
        this.price = price;
        this.quantity = 0;
        this.count = 0;
    }

    public void cancel(long qty) {
        this.quantity -= qty;
        this.count--;
    }

    public boolean valid() {
        return this.quantity > 0;
    }

    @Override
    public String toString() {
        return "Level{" +
                "price=" + price +
                ", quantity=" + quantity +
                ", count=" + count +
                '}';
    }
}
