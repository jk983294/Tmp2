package com.om;

import com.om.fix.*;

import java.util.*;

public class OrderBookEngine {
    private TreeMap<Long, BookLevel> bidBook = new TreeMap<>(Comparator.reverseOrder());

    private TreeMap<Long, BookLevel> askBook = new TreeMap<>();

    private Map<Long, Order> id2order = new HashMap<>();

    private long orderIdInternal = 1;
    private long tradeIdInternal = 1;
    private FixMsgParser parser = new FixMsgParser();

    public List<String> process(String msg) {
        List<String> return_msgs = new ArrayList<>();
        FixMsg fixMsg = parser.Parse(msg);
        if (fixMsg instanceof FixAddMsg) {
            return_msgs.addAll(AddOrder((FixAddMsg) fixMsg));
            return return_msgs;
        } else if (fixMsg instanceof FixCancelMsg) {
            return_msgs.add(CancelOrder((FixCancelMsg) fixMsg));
            return return_msgs;
        } else if (fixMsg instanceof FixUpdateMsg) {
            return_msgs.add(UpdateOrder((FixUpdateMsg) fixMsg));
            return return_msgs;
        } else {
            return_msgs.add(fixMsg.GetFailureMsg(null));
            return return_msgs;
        }
    }

    private List<String> AddOrder(FixAddMsg msg) {
        List<String> return_msgs = new ArrayList<>();
        Order order = new Order(msg.price, msg.quantity, msg.isBuy, msg.isMarketOrder, orderIdInternal);
        ++orderIdInternal;

        if (msg.isBuy) {
            while (order.remain_qty() > 0) {
                if (askBook.isEmpty()) break;
                var best_ask_entry = askBook.firstEntry();
                var level_ = best_ask_entry.getValue();
                if (!order.isMarket && level_.level.price > order.price) {
                    break;
                }
                ListIterator<Order> itr = level_.orders.listIterator();
                while (level_.level.quantity > 0 && order.remain_qty() > 0) {
                    Order ask_order = itr.next();
                    if (ask_order.isCancelled) {
                        id2order.remove(ask_order.orderId);
                        itr.remove();
                        continue;
                    }
                    long fill_count = Math.min(ask_order.remain_qty(), order.remain_qty());
                    order.reduce_qty(fill_count);
                    ask_order.reduce_qty(fill_count);
                    level_.reduce(ask_order, fill_count);
                    if (ask_order.remain_qty() <= 0) {
                        itr.remove();
                        id2order.remove(ask_order.orderId);
                    }
                    return_msgs.add(order.createTradeMsg(ask_order.price, fill_count, ask_order.orderId, tradeIdInternal++));
                    // TODO notify counterparty trade happened
                }

                if (!level_.valid()) {
                    askBook.remove(best_ask_entry.getKey());
                }
            }

            if (order.remain_qty() > 0 && !msg.isMarketOrder) {
                Add2Bid(order);
            }
        } else { // sell
            while (order.remain_qty() > 0) {
                if (bidBook.isEmpty()) break;
                var best_bid_entry = bidBook.firstEntry();
                var level_ = best_bid_entry.getValue();
                if (!order.isMarket && level_.level.price < order.price) {
                    break;
                }
                ListIterator<Order> itr = level_.orders.listIterator();
                while (level_.level.quantity > 0 && order.remain_qty() > 0) {
                    Order bid_order = itr.next();
                    if (bid_order.isCancelled) {
                        id2order.remove(bid_order.orderId);
                        itr.remove();
                        continue;
                    }
                    long fill_count = Math.min(bid_order.remain_qty(), order.remain_qty());
                    order.reduce_qty(fill_count);
                    bid_order.reduce_qty(fill_count);
                    level_.reduce(bid_order, fill_count);
                    if (bid_order.remain_qty() <= 0) {
                        itr.remove();
                        id2order.remove(bid_order.orderId);
                    }
                    return_msgs.add(order.createTradeMsg(bid_order.price, fill_count, bid_order.orderId, tradeIdInternal++));
                    // TODO notify counterparty trade happened
                }

                if (!level_.valid()) {
                    bidBook.remove(best_bid_entry.getKey());
                }
            }

            if (order.remain_qty() > 0 && !msg.isMarketOrder) {
                Add2Ask(order);
            }
        }
        return_msgs.add(order.createOrderMsg());
        return return_msgs;
    }

    private String CancelOrder(FixCancelMsg msg) {
        Map<Integer, String> extraFields = new TreeMap<>();
        if (msg.fail_reason != null) {
            return msg.GetFailureMsg(extraFields);
        }
        if(id2order.containsKey(msg.orderId)) {
            Order order = id2order.get(msg.orderId);
            if (order.isCancelled) {
                extraFields.put(FixConstants.FieldReason, "order already cancelled");
                return msg.GetFailureMsg(extraFields);
            } else if (order.remain_qty() <= 0) {
                extraFields.put(FixConstants.FieldReason, "order already filled");
                return msg.GetFailureMsg(extraFields);
            } else {
                order.isCancelled = true;
                if (order.isBuy) {
                    var level_ = bidBook.get(order.price);
                    level_.level.cancel(order.remain_qty());
                    if (!level_.valid()) {
                        bidBook.remove(order.price);
                    }
                } else {
                    var level_ = askBook.get(order.price);
                    level_.level.cancel(order.remain_qty());
                    if (!level_.valid()) {
                        askBook.remove(order.price);
                    }
                }
                extraFields.putAll(order.getOrderFields());
                return msg.GetSuccessMsg(extraFields);
            }
        } else {
            extraFields.put(FixConstants.FieldReason, "no such order");
            return msg.GetFailureMsg(extraFields);
        }
    }

    private String UpdateOrder(FixUpdateMsg msg) {
        Map<Integer, String> extraFields = new TreeMap<>(Comparator.reverseOrder());
        if (msg.fail_reason != null) {
            return msg.GetFailureMsg(extraFields);
        }
        if(id2order.containsKey(msg.orderId)) {
            Order order = id2order.get(msg.orderId);
            if (order.isCancelled) {
                extraFields.put(FixConstants.FieldReason, "order already cancelled");
                return msg.GetFailureMsg(extraFields);
            } else if (order.remain_qty() <= 0) {
                extraFields.put(FixConstants.FieldReason, "order already filled");
                return msg.GetFailureMsg(extraFields);
            } else if (order.filled > msg.newQuantity){
                extraFields.put(FixConstants.FieldReason, "cannot modify order where new quantity is less than filled quantity");
                return msg.GetFailureMsg(extraFields);
            } else if (order.quantity == msg.newQuantity){
                extraFields.put(FixConstants.FieldReason, "new quantity is not changed");
                return msg.GetFailureMsg(extraFields);
            } else {
                long delta = msg.newQuantity - order.quantity;
                order.quantity = msg.newQuantity;
                if (order.isBuy) {
                    var level_ = bidBook.get(order.price);
                    level_.level.update(delta);
                } else {
                    var level_ = askBook.get(order.price);
                    level_.level.update(delta);
                }
                return order.createOrderMsg();
            }
        } else {
            extraFields.put(FixConstants.FieldReason, "no such order");
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
        id2order.put(order.orderId, order);
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
        id2order.put(order.orderId, order);
    }

    public List<Level> GetL2Asks() {
        return GetL2Asks(10);
    }
    public List<Level> GetL2Asks(int max_depth) {
        List<Level> asks = new ArrayList<>();
        int level_cnt = 1;
        for (var entry : askBook.entrySet()) {
            var level = entry.getValue();
            if (level.valid()) {
                asks.add(level.level);
                level_cnt++;
            }
            if (max_depth > 0 && level_cnt > max_depth) break;
        }
        return asks;
    }

    public List<Level> GetL2Bids() {
        return GetL2Bids(10);
    }
    public List<Level> GetL2Bids(int max_depth) {
        List<Level> bids = new ArrayList<>();
        int level_cnt = 1;
        for (var entry : bidBook.entrySet()) {
            var level = entry.getValue();
            if (level.valid()) {
                bids.add(level.level);
                level_cnt++;
            }
            if (max_depth > 0 && level_cnt > max_depth) break;
        }
        return bids;
    }

    public void printOrderBook() {
        printOrderBook(0);
    }
    public void printOrderBook(int max_depth) {
        System.out.println("___ ORDER BOOK ___");
        System.out.println("Asks:");
        int level_cnt = 1;
        for (var entry : askBook.entrySet()) {
            var level = entry.getValue();
            if (level.valid()) {
                System.out.println(entry.getKey() + "/" + entry.getValue().level);
                level_cnt++;
            }
            if (max_depth > 0 && level_cnt > max_depth) break;
        }
        System.out.println("Bids:");
        level_cnt = 1;
        for (var entry : bidBook.entrySet()) {
            var level = entry.getValue();
            if (level.valid()) {
                System.out.println(entry.getKey() + "/" + entry.getValue().level);
                level_cnt++;
            }
            if (max_depth > 0 && level_cnt > max_depth) break;
        }
        System.out.println("_________________");
    }
}
