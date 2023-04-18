package com.om;

import com.om.fix.FixConstants;
import com.om.fix.FixMsg;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class Order {
    long price;
    long quantity;
    long filled = 0;
    boolean isBuy;
    /**
     * soft cancel, it only marks this order has been cancelled, but don't remove it right now
     * when add new order, do a match, check this flag to remove it later
     */
    boolean isCancelled = false;
    boolean isMarket;
    long orderId;

    public Order(long price, long quantity, boolean isBuy, boolean isMarket, long orderId) {
        this.price = price;
        this.quantity = quantity;
        this.isBuy = isBuy;
        this.isMarket = isMarket;
        this.orderId = orderId;
    }

    long remain_qty() { return quantity - filled; }
    void reduce_qty(long qty) {
        this.filled += qty;
    }

    String createOrderMsg() {
        Map<Integer, String> fields = new TreeMap<>();
        fields.put(FixConstants.FieldMsgType, FixConstants.FieldMsgType_ExecutionReport);
        if (!this.isMarket) {
            fields.put(FixConstants.FieldPrice, String.valueOf(this.price));
        }
        fields.put(FixConstants.FieldQuantity, String.valueOf(this.quantity));
        fields.put(FixConstants.FieldSide, this.isBuy ? FixConstants.FieldSide_Buy : FixConstants.FieldSide_Sell);
        fields.put(FixConstants.FieldOrdType, this.isMarket ? FixConstants.FieldOrdType_Market : FixConstants.FieldOrdType_Limit);
        fields.put(FixConstants.FieldOrderID, String.valueOf(this.orderId));
        fields.put(FixConstants.FieldCumQty, String.valueOf(this.filled));
        return FixMsg.CompileMsg(fields);
    }

    String createTradeMsg(long trade_price, long qty, long counterOrderId, long tradeId) {
        Map<Integer, String> fields = new TreeMap<>();
        fields.put(FixConstants.FieldMsgType, FixConstants.FieldMsgType_ExecutionReport);
        fields.put(FixConstants.FieldPrice, String.valueOf(this.price));
        fields.put(FixConstants.FieldOrderQty, String.valueOf(this.quantity));
        fields.put(FixConstants.FieldSide, this.isBuy ? FixConstants.FieldSide_Buy : FixConstants.FieldSide_Sell);
        fields.put(FixConstants.FieldOrderID, String.valueOf(this.orderId));
        fields.put(FixConstants.FieldCumQty, String.valueOf(this.filled));
        fields.put(FixConstants.FieldLastPx, String.valueOf(trade_price));
        fields.put(FixConstants.FieldLastQty, String.valueOf(qty));
        fields.put(FixConstants.FieldSecondaryOrderID, String.valueOf(counterOrderId));
        fields.put(FixConstants.FieldExecID, String.valueOf(tradeId));
        return FixMsg.CompileMsg(fields);
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
