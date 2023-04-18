package com.om;

import org.junit.Assert;
import org.junit.Test;

public class UpdateTest {
    @Test
    public void test_update_illegal_input() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=G;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no OrderId 37 field;", msgs.get(0));

        msgs = engine.process("35=G;37=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no new OrderQty 38 field;", msgs.get(0));

        msgs = engine.process("35=G;37=1;38=100;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=no such order;", msgs.get(0));
    }

    @Test
    public void test_update_bid() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());

        msgs = engine.process("35=G;37=1;38=200;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=200;54=1;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());
        var bid_levels = engine.GetL2Bids();
        var bid_level = bid_levels.get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(200, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);

        msgs = engine.process("35=G;37=1;38=200;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=new quantity is not changed;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());
    }

    @Test
    public void test_update_partially_filled_bid() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));
        msgs = engine.process("35=D;44=1000;38=50;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Bids().size());
        Assert.assertEquals("14=50;17=1;31=1000;32=50;35=8;37=2;38=50;39=2;44=1000;54=2;198=1;", msgs.get(0));

        msgs = engine.process("35=G;37=1;38=40;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=cannot modify order where new quantity is less than filled quantity;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());

        msgs = engine.process("35=G;37=1;38=60;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=50;35=8;37=1;39=1;40=2;44=1000;53=60;54=1;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Bids().size());
        var bid_levels = engine.GetL2Bids();
        var bid_level = bid_levels.get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(10, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
    }

    @Test
    public void test_update_ask() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Asks().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));

        msgs = engine.process("35=G;37=1;38=200;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=200;54=2;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Asks().size());
        var ask_levels = engine.GetL2Asks();
        var ask_level = ask_levels.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(200, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);

        msgs = engine.process("35=G;37=1;38=200;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=new quantity is not changed;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Asks().size());
    }

    @Test
    public void test_update_partially_filled_ask() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, engine.GetL2Asks().size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));
        msgs = engine.process("35=D;44=1000;38=50;40=2;54=1;");
        Assert.assertEquals("14=50;17=1;31=1000;32=50;35=8;37=2;38=50;39=2;44=1000;54=1;198=1;", msgs.get(0));

        msgs = engine.process("35=G;37=1;38=40;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=9;127=cannot modify order where new quantity is less than filled quantity;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Asks().size());

        msgs = engine.process("35=G;37=1;38=60;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=50;35=8;37=1;39=1;40=2;44=1000;53=60;54=2;", msgs.get(0));
        Assert.assertEquals(1, engine.GetL2Asks().size());
        var ask_levels = engine.GetL2Asks();
        var ask_level = ask_levels.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(10, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
    }
}
