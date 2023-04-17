package com.om;

import java.util.LinkedList;

public class BookLevel {
    Level level;
    LinkedList<Order> orders;

    public BookLevel(long price) {
        this.level = new Level(price);
        this.orders = new LinkedList<>();
    }

    void add(Order order) {
        if (order.remain_qty() > 0) {
            level.quantity += order.remain_qty();
            level.count++;
        }
        orders.addLast(order);
    }
}
