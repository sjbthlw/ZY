package com.hzsun.mpos.Adapter;


import java.io.Serializable;

public class ListInfoBean implements Serializable {
    private String title;
    private String item1;
    private String item2;
    private int value;

    public ListInfoBean(String title, String item1, int value) {
        this.title = title;
        this.item1 = item1;
        this.value = value;
    }

    public ListInfoBean(String title, String item1, String item2, int value) {
        this.title = title;
        this.item1 = item1;
        this.item2 = item2;
        this.value = value;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItem1() {
        return item1 == null ? "" : item1;
    }

    public void setItem1(String item1) {
        this.item1 = item1;
    }

    public String getItem2() {
        return item2 == null ? "" : item2;
    }

    public void setItem2(String item2) {
        this.item2 = item2;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
