package com.stephenmorgandevelopment.thelinuxmanual.models;

import com.google.gson.Gson;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.util.LinkedHashMap;
import java.util.Map;

public class Command {
    private long id;
    private String name;
    private Map<String, String> data;

    public Command(long id, String name) {
        this(id, name, new LinkedHashMap<String, String>());
    }

    public Command(long id, String name, Map<String, String> data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

//    public String toJson() {
//        Gson gson = new Gson();
//        return gson.toJson(this);
//    }
//
//    public static Command fromJson() {
//
//
//
//    }
}
