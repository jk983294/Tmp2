package com.om.fix;

import java.util.Map;
import java.util.TreeMap;

public class FixCancelMsg implements FixMsg {
    public long orderId;
    public String fail_reason;
    @Override
    public boolean Parse(Map<Integer, String> fields) {
        if (fields.containsKey(FixConstants.FieldOrderID)) {
            orderId = Long.parseLong(fields.get(FixConstants.FieldOrderID));
        } else {
            fail_reason = "no OrderID field";
            return false;
        }
        return true;
    }

    @Override
    public String GetSuccessMsg(Map<Integer, String> extraFields) {
        Map<Integer, String> fields = new TreeMap<>();
        if (extraFields != null) {
            fields.putAll(extraFields);
        }
        return FixMsg.CompileMsg(fields);
    }

    @Override
    public String GetFailureMsg(Map<Integer, String> extraFields) {
        Map<Integer, String> fields = new TreeMap<>();
        fields.put(FixConstants.FieldMsgType, FixConstants.FieldMsgType_OrderCancelReject);
        if (fail_reason != null && !fail_reason.isEmpty()) {
            fields.put(FixConstants.FieldReason, fail_reason);
        }
        if (extraFields != null) {
            fields.putAll(extraFields);
        }
        return FixMsg.CompileMsg(fields);
    }
}
