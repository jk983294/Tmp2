package com.om;

import org.junit.Assert;
import org.junit.Test;

public class AddTest {
    @Test
    public void test_add_illegal_input() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=100;40=1;15=2;54=1;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=no OrderQty 38 field;", msgs.get(0));

        msgs = engine.process("35=D;38=100;40=2;15=2;54=1;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=no Price 44 field;", msgs.get(0));

        msgs = engine.process("44=100;38=100;40=1;15=2;54=1;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=raw msg has no MsgType field;", msgs.get(0));

        msgs = engine.process("");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=raw msg format error;", msgs.get(0));

        msgs = engine.process("35=D;44=100;38=100;40=1;15=2;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=no Side 54 field;", msgs.get(0));

        msgs = engine.process("35=D;44=100;38=100;54=1;15=2;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=no OrdType 40 field;", msgs.get(0));

        msgs = engine.process("35=D;44=-100;38=100;40=2;54=1;15=2;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=order price error;", msgs.get(0));

        msgs = engine.process("35=D;44=100;38=-100;40=1;54=1;15=2;100=CNY;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("35=3;39=8;127=order quantity error;", msgs.get(0));
    }

    @Test
    public void test_add_market_bid() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;38=100;40=1;54=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=1;53=100;54=1;", msgs.get(0));

        Assert.assertEquals(0, engine.GetL2Asks().size());
        Assert.assertEquals(0, engine.GetL2Bids().size());
    }

    @Test
    public void test_add_market_ask() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;38=100;40=1;54=2;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=1;53=100;54=2;", msgs.get(0));

        Assert.assertEquals(0, engine.GetL2Asks().size());
        Assert.assertEquals(0, engine.GetL2Bids().size());
    }

    @Test
    public void test_add_limit_bid_no_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));

        Assert.assertEquals(0, engine.GetL2Asks().size());
        Assert.assertEquals(1, engine.GetL2Bids().size());
        var bid_level = engine.GetL2Bids().get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(100, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);

        msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=2;39=0;40=2;44=1000;53=100;54=1;", msgs.get(0));
        bid_level = engine.GetL2Bids().get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(200, bid_level.quantity);
        Assert.assertEquals(2, bid_level.count);

        msgs = engine.process("35=D;44=1001;38=100;40=2;54=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=3;39=0;40=2;44=1001;53=100;54=1;", msgs.get(0));
        var bid_levels = engine.GetL2Bids();
        bid_level = bid_levels.get(0);
        Assert.assertEquals(1001, bid_level.price);
        Assert.assertEquals(100, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        var bid_level2 = bid_levels.get(1);
        Assert.assertEquals(1000, bid_level2.price);
        Assert.assertEquals(200, bid_level2.quantity);
        Assert.assertEquals(2, bid_level2.count);

        msgs = engine.process("35=D;44=999;38=100;40=2;54=1;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=4;39=0;40=2;44=999;53=100;54=1;", msgs.get(0));
        bid_levels = engine.GetL2Bids();
        bid_level = bid_levels.get(0);
        Assert.assertEquals(1001, bid_level.price);
        Assert.assertEquals(100, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        bid_level2 = bid_levels.get(1);
        Assert.assertEquals(1000, bid_level2.price);
        Assert.assertEquals(200, bid_level2.quantity);
        Assert.assertEquals(2, bid_level2.count);
        var bid_level3 = bid_levels.get(2);
        Assert.assertEquals(999, bid_level3.price);
        Assert.assertEquals(100, bid_level3.quantity);
        Assert.assertEquals(1, bid_level3.count);
    }

    @Test
    public void test_add_limit_ask_no_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=1;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));

        Assert.assertEquals(1, engine.GetL2Asks().size());
        Assert.assertEquals(0, engine.GetL2Bids().size());
        var ask_level = engine.GetL2Asks().get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(100, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);

        msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=2;39=0;40=2;44=1000;53=100;54=2;", msgs.get(0));
        var asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(200, ask_level.quantity);
        Assert.assertEquals(2, ask_level.count);

        msgs = engine.process("35=D;44=1001;38=100;40=2;54=2;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=3;39=0;40=2;44=1001;53=100;54=2;", msgs.get(0));
        asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(200, ask_level.quantity);
        Assert.assertEquals(2, ask_level.count);
        var ask_level2 = asks.get(1);
        Assert.assertEquals(1001, ask_level2.price);
        Assert.assertEquals(100, ask_level2.quantity);
        Assert.assertEquals(1, ask_level2.count);

        msgs = engine.process("35=D;44=999;38=100;40=2;54=2;");
        Assert.assertEquals(1, msgs.size());
        Assert.assertEquals("14=0;35=8;37=4;39=0;40=2;44=999;53=100;54=2;", msgs.get(0));
        asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(999, ask_level.price);
        Assert.assertEquals(100, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        ask_level2 = asks.get(1);
        Assert.assertEquals(1000, ask_level2.price);
        Assert.assertEquals(200, ask_level2.quantity);
        Assert.assertEquals(2, ask_level2.count);
        var ask_level3 = asks.get(2);
        Assert.assertEquals(1001, ask_level3.price);
        Assert.assertEquals(100, ask_level3.quantity);
        Assert.assertEquals(1, ask_level3.count);
    }

    @Test
    public void test_add_market_bid_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=1001;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=999;38=100;40=2;54=2;");

        msgs = engine.process("35=D;38=50;40=1;54=1;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=50;17=1;31=999;32=50;35=8;37=5;38=50;39=2;44=0;54=1;198=4;", msgs.get(0));
        Assert.assertEquals("14=50;35=8;37=5;39=2;40=1;53=50;54=1;", msgs.get(1));

        var asks = engine.GetL2Asks();
        var ask_level = asks.get(0);
        Assert.assertEquals(999, ask_level.price);
        Assert.assertEquals(50, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        var ask_level2 = asks.get(1);
        Assert.assertEquals(1000, ask_level2.price);
        Assert.assertEquals(200, ask_level2.quantity);
        Assert.assertEquals(2, ask_level2.count);
        var ask_level3 = asks.get(2);
        Assert.assertEquals(1001, ask_level3.price);
        Assert.assertEquals(100, ask_level3.quantity);
        Assert.assertEquals(1, ask_level3.count);

        msgs = engine.process("35=D;38=200;40=1;54=1;");
        Assert.assertEquals(4, msgs.size());
        Assert.assertEquals("14=50;17=2;31=999;32=50;35=8;37=6;38=200;39=1;44=0;54=1;198=4;", msgs.get(0));
        Assert.assertEquals("14=150;17=3;31=1000;32=100;35=8;37=6;38=200;39=1;44=0;54=1;198=1;", msgs.get(1));
        Assert.assertEquals("14=200;17=4;31=1000;32=50;35=8;37=6;38=200;39=2;44=0;54=1;198=2;", msgs.get(2));
        Assert.assertEquals("14=200;35=8;37=6;39=2;40=1;53=200;54=1;", msgs.get(3));

        asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(50, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        ask_level2 = asks.get(1);
        Assert.assertEquals(1001, ask_level2.price);
        Assert.assertEquals(100, ask_level2.quantity);
        Assert.assertEquals(1, ask_level2.count);

        msgs = engine.process("35=D;38=200;40=1;54=1;");
        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("14=50;17=5;31=1000;32=50;35=8;37=7;38=200;39=1;44=0;54=1;198=2;", msgs.get(0));
        Assert.assertEquals("14=150;17=6;31=1001;32=100;35=8;37=7;38=200;39=1;44=0;54=1;198=3;", msgs.get(1));
        Assert.assertEquals("14=150;35=8;37=7;39=1;40=1;53=200;54=1;", msgs.get(2));

        asks = engine.GetL2Asks();
        Assert.assertEquals(0, asks.size());
    }

    @Test
    public void test_add_market_ask_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=1001;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=999;38=100;40=2;54=1;");

        msgs = engine.process("35=D;38=50;40=1;54=2;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=50;17=1;31=1001;32=50;35=8;37=5;38=50;39=2;44=0;54=2;198=3;", msgs.get(0));
        Assert.assertEquals("14=50;35=8;37=5;39=2;40=1;53=50;54=2;", msgs.get(1));

        var bids = engine.GetL2Bids();
        var bid_level = bids.get(0);
        Assert.assertEquals(1001, bid_level.price);
        Assert.assertEquals(50, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        var bid_level2 = bids.get(1);
        Assert.assertEquals(1000, bid_level2.price);
        Assert.assertEquals(200, bid_level2.quantity);
        Assert.assertEquals(2, bid_level2.count);
        var bid_level3 = bids.get(2);
        Assert.assertEquals(999, bid_level3.price);
        Assert.assertEquals(100, bid_level3.quantity);
        Assert.assertEquals(1, bid_level3.count);

        msgs = engine.process("35=D;38=200;40=1;54=2;");
        Assert.assertEquals(4, msgs.size());
        Assert.assertEquals("14=50;17=2;31=1001;32=50;35=8;37=6;38=200;39=1;44=0;54=2;198=3;", msgs.get(0));
        Assert.assertEquals("14=150;17=3;31=1000;32=100;35=8;37=6;38=200;39=1;44=0;54=2;198=1;", msgs.get(1));
        Assert.assertEquals("14=200;17=4;31=1000;32=50;35=8;37=6;38=200;39=2;44=0;54=2;198=2;", msgs.get(2));
        Assert.assertEquals("14=200;35=8;37=6;39=2;40=1;53=200;54=2;", msgs.get(3));

        bids = engine.GetL2Bids();
        bid_level = bids.get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(50, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        bid_level2 = bids.get(1);
        Assert.assertEquals(999, bid_level2.price);
        Assert.assertEquals(100, bid_level2.quantity);
        Assert.assertEquals(1, bid_level2.count);

        msgs = engine.process("35=D;38=200;40=1;54=2;");
        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("14=50;17=5;31=1000;32=50;35=8;37=7;38=200;39=1;44=0;54=2;198=2;", msgs.get(0));
        Assert.assertEquals("14=150;17=6;31=999;32=100;35=8;37=7;38=200;39=1;44=0;54=2;198=4;", msgs.get(1));
        Assert.assertEquals("14=150;35=8;37=7;39=1;40=1;53=200;54=2;", msgs.get(2));

        bids = engine.GetL2Bids();
        Assert.assertEquals(0, bids.size());
    }

    @Test
    public void test_add_limit_bid_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=1000;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=1001;38=100;40=2;54=2;");
        msgs = engine.process("35=D;44=999;38=100;40=2;54=2;");

        msgs = engine.process("35=D;44=999;38=50;40=2;54=1;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=50;17=1;31=999;32=50;35=8;37=5;38=50;39=2;44=999;54=1;198=4;", msgs.get(0));
        Assert.assertEquals("14=50;35=8;37=5;39=2;40=2;44=999;53=50;54=1;", msgs.get(1));

        var asks = engine.GetL2Asks();
        var ask_level = asks.get(0);
        Assert.assertEquals(999, ask_level.price);
        Assert.assertEquals(50, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        var ask_level2 = asks.get(1);
        Assert.assertEquals(1000, ask_level2.price);
        Assert.assertEquals(200, ask_level2.quantity);
        Assert.assertEquals(2, ask_level2.count);
        var ask_level3 = asks.get(2);
        Assert.assertEquals(1001, ask_level3.price);
        Assert.assertEquals(100, ask_level3.quantity);
        Assert.assertEquals(1, ask_level3.count);

        msgs = engine.process("35=D;38=300;40=2;44=1000;54=1;");
        Assert.assertEquals(4, msgs.size());
        Assert.assertEquals("14=50;17=2;31=999;32=50;35=8;37=6;38=300;39=1;44=1000;54=1;198=4;", msgs.get(0));
        Assert.assertEquals("14=150;17=3;31=1000;32=100;35=8;37=6;38=300;39=1;44=1000;54=1;198=1;", msgs.get(1));
        Assert.assertEquals("14=250;17=4;31=1000;32=100;35=8;37=6;38=300;39=1;44=1000;54=1;198=2;", msgs.get(2));
        Assert.assertEquals("14=250;35=8;37=6;39=1;40=2;44=1000;53=300;54=1;", msgs.get(3));

        asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(1001, ask_level.price);
        Assert.assertEquals(100, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        var bids = engine.GetL2Bids();
        var bid_level = bids.get(0);
        Assert.assertEquals(1000, bid_level.price);
        Assert.assertEquals(50, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);

        msgs = engine.process("35=D;38=200;40=2;44=1002;54=1;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=100;17=5;31=1001;32=100;35=8;37=7;38=200;39=1;44=1002;54=1;198=3;", msgs.get(0));
        Assert.assertEquals("14=100;35=8;37=7;39=1;40=2;44=1002;53=200;54=1;", msgs.get(1));

        asks = engine.GetL2Asks();
        Assert.assertEquals(0, asks.size());
        bids = engine.GetL2Bids();
        bid_level = bids.get(0);
        Assert.assertEquals(1002, bid_level.price);
        Assert.assertEquals(100, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        var bid_level1 = bids.get(1);
        Assert.assertEquals(1000, bid_level1.price);
        Assert.assertEquals(50, bid_level1.quantity);
        Assert.assertEquals(1, bid_level1.count);
    }

    @Test
    public void test_add_limit_ask_cross() {
        OrderBookEngine engine = new OrderBookEngine();
        var msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=1000;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=1001;38=100;40=2;54=1;");
        msgs = engine.process("35=D;44=999;38=100;40=2;54=1;");

        msgs = engine.process("35=D;38=50;40=2;44=1001;54=2;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=50;17=1;31=1001;32=50;35=8;37=5;38=50;39=2;44=1001;54=2;198=3;", msgs.get(0));
        Assert.assertEquals("14=50;35=8;37=5;39=2;40=2;44=1001;53=50;54=2;", msgs.get(1));

        var bids = engine.GetL2Bids();
        var bid_level = bids.get(0);
        Assert.assertEquals(1001, bid_level.price);
        Assert.assertEquals(50, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);
        var bid_level2 = bids.get(1);
        Assert.assertEquals(1000, bid_level2.price);
        Assert.assertEquals(200, bid_level2.quantity);
        Assert.assertEquals(2, bid_level2.count);
        var bid_level3 = bids.get(2);
        Assert.assertEquals(999, bid_level3.price);
        Assert.assertEquals(100, bid_level3.quantity);
        Assert.assertEquals(1, bid_level3.count);

        msgs = engine.process("35=D;38=300;40=2;44=1000;54=2;");
        Assert.assertEquals(4, msgs.size());
        Assert.assertEquals("14=50;17=2;31=1001;32=50;35=8;37=6;38=300;39=1;44=1000;54=2;198=3;", msgs.get(0));
        Assert.assertEquals("14=150;17=3;31=1000;32=100;35=8;37=6;38=300;39=1;44=1000;54=2;198=1;", msgs.get(1));
        Assert.assertEquals("14=250;17=4;31=1000;32=100;35=8;37=6;38=300;39=1;44=1000;54=2;198=2;", msgs.get(2));
        Assert.assertEquals("14=250;35=8;37=6;39=1;40=2;44=1000;53=300;54=2;", msgs.get(3));

        bids = engine.GetL2Bids();
        bid_level = bids.get(0);
        Assert.assertEquals(999, bid_level.price);
        Assert.assertEquals(100, bid_level.quantity);
        Assert.assertEquals(1, bid_level.count);

        var asks = engine.GetL2Asks();
        var ask_level = asks.get(0);
        Assert.assertEquals(1000, ask_level.price);
        Assert.assertEquals(50, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);

        msgs = engine.process("35=D;38=200;40=2;44=998;54=2;");
        Assert.assertEquals(2, msgs.size());
        Assert.assertEquals("14=100;17=5;31=999;32=100;35=8;37=7;38=200;39=1;44=998;54=2;198=4;", msgs.get(0));
        Assert.assertEquals("14=100;35=8;37=7;39=1;40=2;44=998;53=200;54=2;", msgs.get(1));

        bids = engine.GetL2Bids();
        Assert.assertEquals(0, bids.size());

        asks = engine.GetL2Asks();
        ask_level = asks.get(0);
        Assert.assertEquals(998, ask_level.price);
        Assert.assertEquals(100, ask_level.quantity);
        Assert.assertEquals(1, ask_level.count);
        var ask_level1 = asks.get(1);
        Assert.assertEquals(1000, ask_level1.price);
        Assert.assertEquals(50, ask_level1.quantity);
        Assert.assertEquals(1, ask_level1.count);
    }
}
