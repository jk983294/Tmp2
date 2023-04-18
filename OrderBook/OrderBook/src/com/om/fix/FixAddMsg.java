package com.om.fix;

import java.util.Map;

public class FixAddMsg implements FixMsg {
    public long price = 0;
    public long quantity = 0;
    public boolean isBuy;
    public boolean isMarketOrder;
    public String fail_reason;

    @Override
    public boolean Parse(Map<Integer, String> fields) {
        if (fields.containsKey(FixConstants.FieldOrderQty)) {
            quantity = Long.parseLong(fields.get(FixConstants.FieldOrderQty));
        } else {
            fail_reason = "no OrderQty 38 field";
            return false;
        }

        if (fields.containsKey(FixConstants.FieldOrdType)) {
            var type_ = fields.get(FixConstants.FieldOrdType);
            if (type_.equals(FixConstants.FieldOrdType_Market)) {
                isMarketOrder = true;
            } else if (type_.equals(FixConstants.FieldOrdType_Limit)) {
                isMarketOrder = false;
            } else {
                fail_reason = "not support OrdType 40 field";
                return false;
            }
        } else {
            fail_reason = "no OrdType 40 field";
            return false;
        }

        if (!isMarketOrder) {
            if (fields.containsKey(FixConstants.FieldPrice)) {
                price = Long.parseLong(fields.get(FixConstants.FieldPrice));
            } else {
                fail_reason = "no Price 44 field";
                return false;
            }

            if (price <= 0) {
                fail_reason = "order price error";
                return false;
            }
        }

        if (fields.containsKey(FixConstants.FieldSide)) {
            var side = fields.get(FixConstants.FieldSide);
            if (side.equals(FixConstants.FieldSide_Buy)) {
                isBuy = true;
            } else if (side.equals(FixConstants.FieldSide_Sell)) {
                isBuy = false;
            } else {
                fail_reason = "not support Side 54 field";
                return false;
            }
        } else {
            fail_reason = "no Side 54 field";
            return false;
        }

        if (quantity <= 0) {
            fail_reason = "order quantity error";
            return false;
        }
        return true;
    }

    @Override
    public String GetSuccessMsg(Map<Integer, String> extraFields) {
        return null;
    }

    @Override
    public String GetFailureMsg(Map<Integer, String> extraFields) {
        return null;
    }
}
