package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SimpleCommand {
    private String name;
    private String description;
    private String url;

    public SimpleCommand(String name, String path) {
        this(name, "", path);
    }

    public SimpleCommand(String name, String description, String path) {
        this.name = name;
        this.description = description;
        this.url = path;
    }

    public String getName() {return name;}
    public String getDescription() {return description;}
    public String getUrl() {return url;}

    public void addDescription(String description) {
        if(this.description != null || !this.description.isEmpty()) {
            Log.e(SimpleCommand.class.getSimpleName(), "Failed adding description: "
                    + this.name + " already has a description.");
        }
        this.description = description;
    }

    public static SimpleCommand fromJsonString(String json) {
        try {
            return fromJsonObject(new JSONObject(json));
        } catch (JSONException je) {
            Log.e(SimpleCommand.class.getSimpleName(), "JSON parsing error.");
            je.printStackTrace();
            return null;
        }
    }

    public static SimpleCommand fromJsonObject(JSONObject object) {
        try {
            return new SimpleCommand(object.getString("name"), object.getString("description"), object.getString("url"));
        } catch (JSONException je) {
            Log.e(SimpleCommand.class.getSimpleName(), "JSON parsing error.");
            je.printStackTrace();
            return null;
        }
    }
}
