package com.json_tool;

import com.json_tool.biz.Biz;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Main {

    private Biz biz;

    public static void main(String[] args) {
        new Main().init();
    }

    public Main(){}

    public void init(){
        this.biz = Biz.getInstance();
    }
}