package com.om.fix;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class FixErrorMsg implements FixMsg {
    String reason;
    public FixErrorMsg(Map<Integer, String> fields) {
        Parse(fields);
    }

    @Override
    public boolean Parse(Map<Integer, String> fields) {
        if (fields.containsKey(FixConstants.FieldReason)) {
            reason = fields.get(FixConstants.FieldReason);
        }
        return true;
    }

    @Override
    public String GetSuccessMsg(Map<Integer, String> extraFields) {
        return null;
    }

    @Override
    public String GetFailureMsg(Map<Integer, String> extraFields) {
        Map<Integer, String> fields = new TreeMap<>();
        fields.put(FixConstants.FieldMsgType, FixConstants.FieldMsgType_Reject); // Reject
        if (reason != null && !reason.isEmpty()) {
            fields.put(FixConstants.FieldReason, reason);
        }
        if (extraFields != null) {
            fields.putAll(extraFields);
        }
        return FixMsg.CompileMsg(fields);
    }
}
