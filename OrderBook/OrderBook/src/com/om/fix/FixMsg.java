package com.om.fix;

import java.util.Map;

public interface FixMsg {
    void Parse(String msg);

    String GetSuccessMsg(Map<String, String> extraFields);
    String GetFailureMsg(Map<String, String> extraFields);
}
