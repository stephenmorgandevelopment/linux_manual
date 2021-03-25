package com.stephenmorgandevelopment.thelinuxmanual.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class Command {
    private final long id;
    private final Map<String, String> data;

    public Command(long id, Map<String, String> data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public String getShortName() {
        String name = data.get("NAME");
        if(name == null) {
            return "unknown-error";
        }

        name = name.substring(0, name.indexOf(" "));

        if(name.contains(",")) {
            name = name.substring(0, name.indexOf(","));
        }

        return name;
    }

    public Map<String, String> getData() {
        return data;
    }

    public static Map<String, String> parseMapFromJson(String json) {
        Type dataMapType = new TypeToken<Map<String, String>>() {}.getType();
        return new Gson().fromJson(json, dataMapType);
    }

    public static Command fromJson(long id, String dataJson) {
        return new Command(id, parseMapFromJson(dataJson));
    }

    public String toJsonString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public String dataMapToJsonString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this.data);
    }
}
