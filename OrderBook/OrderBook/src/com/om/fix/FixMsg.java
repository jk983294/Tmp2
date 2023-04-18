package com.om.fix;

import java.util.Map;

public interface FixMsg {
    boolean Parse(Map<Integer, String> fields);

    String GetSuccessMsg(Map<Integer, String> extraFields);
    String GetFailureMsg(Map<Integer, String> extraFields);

    static String CompileMsg(Map<Integer, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (var entry : fields.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }
}
