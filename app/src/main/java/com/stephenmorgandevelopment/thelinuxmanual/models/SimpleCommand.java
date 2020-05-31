package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.databases.DatabaseHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class SimpleCommand {
    private long id;
    private String name;
    private String description;
    private String url;
    private int manN;

    public SimpleCommand() {

    }

    public SimpleCommand(String name, String url, int manN) {
        this(name, "", url, manN);
    }

    public SimpleCommand(String name, String description, String url, int manN) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.manN = manN;
    }


    public long getId() {return id;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public String getUrl() {return url;}
    public int getManN() {return manN;}

    public void setId(long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setDescription(String description) {this.description = description;}
    public void setUrl(String url) {this.url = url;}
    public void setManN(int manN) {this.manN = manN;}









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
            return new SimpleCommand(object.getString("name"), object.getString("description"), object.getString("url"), object.getInt("manN"));
        } catch (JSONException je) {
            Log.e(SimpleCommand.class.getSimpleName(), "JSON parsing error.");
            je.printStackTrace();
            return null;
        }
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("name", name);
            object.put("description", description);
            object.put("url", url);
            object.put("manN", manN);
        } catch (JSONException e) {
            Log.e("SimpleCommand", "Parse error for " + name + ". " + e.toString());
            e.printStackTrace();
        }

        return object;
    }
}
