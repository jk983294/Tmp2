package com.om;

import org.junit.Assert;
import org.junit.Test;

public class CancelTest {
    @Test
    public void test_cancel_illegal_input() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=F;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no OrderID field;", msgs.get(0));

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no such order;", msgs.get(0));
    }

    @Test
    public void test_cancel_bid() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=0;35=8;37=1;40=2;44=1000;53=100;54=1;", msgs.get(0));

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no such order;", msgs.get(0));
    }
}
