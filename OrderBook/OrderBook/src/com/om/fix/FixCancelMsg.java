package com.om.fix;

import java.util.Map;

public class FixCancelMsg implements FixMsg {
    public long orderId;
    @Override
    public void Parse(String msg) {

    }

    @Override
    public String GetSuccessMsg(Map<String, String> extraFields) {
        return null;
    }

    @Override
    public String GetFailureMsg(Map<String, String> extraFields) {
        return null;
    }
}
