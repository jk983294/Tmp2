package com.om;

import com.om.fix.FixAddMsg;
import com.om.fix.FixCancelMsg;

import java.util.*;

public class OrderBookEngine {
    private TreeMap<Long, BookLevel> bidBook = new TreeMap<>(Comparator.reverseOrder());

    private TreeMap<Long, BookLevel> askBook = new TreeMap<>();

    private Map<Long, Order> id2order = new HashMap<>();

    private long orderIdInternal = 0;

    public void printOrderBook() {
        System.out.println("___ ORDER BOOK ___");
        System.out.println("Asks:");
        for (var entry : askBook.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue().level);
        }
        System.out.println("Bids:");
        for (var entry : bidBook.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue().level);
        }
        System.out.println("_________________");
    }

    private List<String> AddOrder(FixAddMsg msg) {
        List<String> return_msgs = new ArrayList<>();
        Map<String, String> extraFields = new TreeMap<>();
        Order order = new Order(msg.price, msg.quantity, msg.isBuy);
        if (msg.quantity <= 0) {
            extraFields.put("285", "order quantity error");
            return_msgs.add(msg.GetFailureMsg(extraFields));
            return return_msgs;
        }

        if (msg.isBuy) {
            while (order.remain_qty() > 0) {
                if (askBook.isEmpty()) break;
                var best_ask_entry = askBook.firstEntry();
                var level_ = best_ask_entry.getValue();
                ListIterator<Order> itr = level_.orders.listIterator();
                while (level_.level.quantity > 0 && order.remain_qty() > 0) {
                    Order ask_order = itr.next();
                    if (ask_order.isCancelled) continue;
                    long fill_count = Math.min(ask_order.remain_qty(), order.remain_qty());
                    order.reduce_qty(fill_count);
                    ask_order.reduce_qty(fill_count);
                    OnTrade(ask_order, fill_count);
                    if (order.remain_qty() <= 0) break;

                }
            }

            if (order.remain_qty() > 0 && msg.isMarketOrder) {
                Add2Bid(order);
            }
        }
    }

    private String CancelOrder(FixCancelMsg msg) {
        Map<String, String> extraFields = new TreeMap<>();
        if(id2order.containsKey(msg.orderId)) {
            Order order = id2order.get(msg.orderId);
            if (order.isCancelled) {
                extraFields.put("285", "order already cancelled");
                return msg.GetFailureMsg(extraFields);
            } else if (order.remain_qty() <= 0) {
                extraFields.put("285", "order already filled");
                return msg.GetFailureMsg(extraFields);
            } else {
                order.isCancelled = true;
                if (order.isBuy) {
                    var level_ = bidBook.get(order.price);
                    level_.level.cancel(order.remain_qty());
                } else {
                    var level_ = askBook.get(order.price);
                    level_.level.cancel(order.remain_qty());
                }
                return msg.GetSuccessMsg(extraFields);
            }
        } else {
            extraFields.put("285", "no such order");
            return msg.GetFailureMsg(extraFields);
        }
    }

    private void Add2Bid(Order order) {
        if (bidBook.containsKey(order.price)) {
            BookLevel level_ = bidBook.get(order.price);
            level_.add(order);
        } else {
            BookLevel level_ = new BookLevel(order.price);
            level_.add(order);
            bidBook.put(order.price, level_);
        }
    }

    private void Add2Ask(Order order) {
        if (askBook.containsKey(order.price)) {
            BookLevel level_ = askBook.get(order.price);
            level_.add(order);
        } else {
            BookLevel level_ = new BookLevel(order.price);
            level_.add(order);
            askBook.put(order.price, level_);
        }
    }

    private void OnTrade(Order order, long qty) {
        System.out.println("OnTrade " + order + " qty=" + qty);
    }
}
