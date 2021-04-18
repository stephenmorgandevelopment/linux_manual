package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

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

    public SimpleCommand(long id, String name, String description, String url, int manN) {
        this(name, description, url, manN);
        this.id = id;
    }

    private static String empty = "";
    public SimpleCommand(String name, String url, int manN) {
        this(name, empty, url, manN);
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

    public SimpleCommand setDescriptionReturnSimpleCommand(String description) {
        this.description = description;
        return this;
    }

    public boolean needsDescription() {
        return description.equals(empty);
    }
}
