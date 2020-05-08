package com.stephenmorgandevelopment.thelinuxmanual.distros;

import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public interface LinuxDistro {
    ArrayList<SimpleCommand> crawlForManPages(String pageHtml, int manN);

    ArrayList<String> crawlForManDirs(String pageHtml);

    void addDescriptionToSimpleCommand(SimpleCommand command, String pageHtml);


    static SimpleCommand searchLocalForSimpleCommand(String search, SearchType searchBy) {
        String baseString = null;
        //TODO Pull String from local dir.


        try {
            JSONObject baseObj = new JSONObject(baseString);
            //TODO Search for match and return SimpleCommand.

            return SimpleCommand.fromJsonObject(null);
        } catch(JSONException je) {

        }
        return null;
    }

    enum SearchType {COMMAND_NAME, COMMAND_DESCRIPTION}

}
