package com.stephenmorgandevelopment.thelinuxmanual.distros;

import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public interface LinuxDistro {
    enum Distro{
        UBUNTU(Ubuntu.TAG);

        String tag;
        Distro(String tag) {
            this.tag = tag;
        }
    }

    ArrayList<SimpleCommand> syncSimpleCommands();

//    ArrayList<SimpleCommand> crawlForManPages(String pageHtml, String url);

//    ArrayList<String> crawlForManDirs(String pageHtml);

    enum SearchType {COMMAND_NAME, COMMAND_DESCRIPTION}

}
