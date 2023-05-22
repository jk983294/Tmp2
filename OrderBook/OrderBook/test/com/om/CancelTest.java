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
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=4;40=2;44=1000;53=100;54=1;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Bids().size());

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=order already cancelled;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Bids().size());
    }

    @Test
    public void test_cancel_partially_filled_bid() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));
        msgs = engine.process("35=D;44=1000;38=50;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=50;17=1;31=1000;32=50;35=8;37=2;38=50;39=2;44=1000;54=2;198=1;", msgs.get(0));

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=50;35=8;37=1;39=4;40=2;44=1000;53=100;54=1;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Bids().size());

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=order already cancelled;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Bids().size());
    }

    @Test
    public void test_cancel_ask() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Asks().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=4;40=2;44=1000;53=100;54=2;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Asks().size());

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=order already cancelled;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Asks().size());
    }

    @Test
    public void test_cancel_partially_filled_ask() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Asks().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));
        msgs = engine.process("35=D;44=1000;38=50;40=2;54=1;");
        Assert.assertEquals("14=50;17=1;31=1000;32=50;35=8;37=2;38=50;39=2;44=1000;54=1;198=1;", msgs.get(0));

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=50;35=8;37=1;39=4;40=2;44=1000;53=100;54=2;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Asks().size());

        msgs = engine.process("35=F;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=order already cancelled;", msgs.get(0));
        Assert.assertEquals(0, engine.GetL2Asks().size());
    }
}
