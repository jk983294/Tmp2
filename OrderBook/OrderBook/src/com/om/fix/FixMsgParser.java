package com.om.fix;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class FixMsgParser {
    public FixMsg Parse(String msg) {
        String[] lets = msg.split(";");
        Map<Integer, String> fields = new TreeMap<>();
        for (var let : lets) {
            String[] strs = let.split("=");
            if (strs.length != 2) {
                fields.put(FixConstants.FieldReason, "raw msg format error");
                return new FixErrorMsg(fields);
            } else {
                fields.put(Integer.parseInt(strs[0]), strs[1]);
            }
        }

        if (!fields.containsKey(FixConstants.FieldMsgType)) {
            fields.put(FixConstants.FieldReason, "raw msg has no MsgType field");
            return new FixErrorMsg(fields);
        }

        String msgType = fields.get(FixConstants.FieldMsgType);
        if (msgType.equals(FixConstants.FieldMsgType_NewOrderSingle)) {
            FixAddMsg addMsg = new FixAddMsg();
            if (addMsg.Parse(fields)) {
                return addMsg;
            } else {
                fields.put(FixConstants.FieldReason, addMsg.fail_reason);
                return new FixErrorMsg(fields);
            }
        } else if (msgType.equals(FixConstants.FieldMsgType_OrderCancelRequest)) {
            FixCancelMsg cancelMsg = new FixCancelMsg();
            cancelMsg.Parse(fields);
            return cancelMsg;
        } else {
            fields.put(FixConstants.FieldReason, "unknown MsgType field");
            return new FixErrorMsg(fields);
        }
    }
}
