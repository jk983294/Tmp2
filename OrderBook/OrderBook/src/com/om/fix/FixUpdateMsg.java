package com.om.fix;

import java.util.Map;

public class FixUpdateMsg implements FixMsg {
    public long orderId;
    @Override
    public boolean Parse(Map<Integer, String> fields) {
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
