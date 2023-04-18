package com.om.fix;

public class FixConstants {
    static public int FieldCumQty = 14;
    static public int FieldExecID = 17;
    static public int FieldLastPx = 31;
    static public int FieldLastQty = 32;
    static public int FieldMsgType = 35;
    static public int FieldOrderID = 37;
    static public int FieldOrderQty = 38;
    static public int FieldOrdType = 40;
    static public int FieldPrice = 44;
    static public int FieldQuantity = 53;
    static public int FieldSide = 54;
    static public int FieldReason = 127;
    static public int FieldSecondaryOrderID = 198;

    static public String FieldMsgType_Reject = "3";
    static public String FieldMsgType_ExecutionReport = "8";
    static public String FieldMsgType_NewOrderSingle = "D";
    static public String FieldMsgType_OrderCancelRequest = "F";
    static public String FieldMsgType_OrderCancelReject = "9";

    static public String FieldOrdType_Market = "1";
    static public String FieldOrdType_Limit = "2";

    static public String FieldSide_Buy = "1";
    static public String FieldSide_Sell = "2";
}
