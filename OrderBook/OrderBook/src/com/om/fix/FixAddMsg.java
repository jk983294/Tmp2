package com.om.fix;

import java.util.Map;

public class FixAddMsg implements FixMsg {
    public long price;
    public long quantity;
    public boolean isBuy;
    public boolean isMarketOrder;

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
