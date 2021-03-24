package com.stephenmorgandevelopment.thelinuxmanual.models;

import java.util.Map;

public class Command {
    private final long id;
    private final Map<String, String> data;

//    public Command(long id) {
//        this(id, new LinkedHashMap<String, String>());
//    }

    public Command(long id, Map<String, String> data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data.putAll(data);
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
