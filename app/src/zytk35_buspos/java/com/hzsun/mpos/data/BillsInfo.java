package com.hzsun.mpos.data;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "ZYTK", strict = false)
public class BillsInfo {
    @Element(name = "Code", required = false)
    private int code;
    @Element(name = "Msg", required = false)
    private String msg;
    @Element(name = "Money", required = false)
    private int money;
    @Element(name = "Count", required = false)
    private int count;
    @ElementList(name = "Table", inline = true, required = false)
    private List<TableItem> tables;


    @Root(name = "Table")
    public static  class TableItem {
        @Element(name = "Name", required = true)
        private String name;
        @Element(name = "FeeNum", required = false)
        private int feenum;
        @Element(name = "Time", required = false)
        private String time;
        @Element(name = "Amount", required = false)
        private int amount;
        @Element(name = "Type", required = false)
        private int type;

        public String getName() {
            return name == null ? "" : name;
        }

        public int getFeenum() {
            return feenum;
        }

        public String getTime() {
            return time == null ? "" : time;
        }

        public int getAmount() {
            return amount;
        }

        public int getType() {
            return type;
        }


    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public int getMoney() {
        return money;
    }

    public int getCount() {
        return count;
    }

    public List<TableItem> getTables() {
        if (tables == null) {
            return new ArrayList<>();
        }
        return tables;
    }
}
