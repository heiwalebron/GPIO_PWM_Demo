package com.friendlyarm.Utils;

/**
 * Created by Administrator on 2016/12/12/012.
 */

public class LED {
    public int code = 0;
    String name = null;
    boolean selected = false;

    public LED(int code, String name, boolean selected) {
        super();
        this.code = code;
        this.name = name;
        this.selected = selected;
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
