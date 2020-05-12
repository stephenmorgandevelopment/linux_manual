package com.stephenmorgandevelopment.thelinuxmanual.distros;


import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class Ubuntu extends Distribution implements LinuxDistro {
    public static final String TAG = Ubuntu.class.getSimpleName();
    public static final String NAME = Helpers.getApplicationContext().getString(R.string.ubuntu_name);

    public static final String BASE_URL = "https://manpages.ubuntu.com/manpages/";
    private static final String CRAWLER_SELECTOR = "#tableWrapper pre a";



    private static Release release;
    enum Release {
        ARTFUL("artful"), BIONIC("bionic"), COSMIC("cosmic"), DISCO("disco")
        , EOAN("eoan"), FOCAL("focal"), GROOVY("groovy")
        ,PRECISE("precise"), TRUSY("trusty"), XENIAL("xenial");

        String name;
        Release(String path) {
            this.name = path;
        }
    }

    public Ubuntu() {
        this(Release.FOCAL);
    }

    public Ubuntu(Release version) {
        release = version;
        commandsList = new ArrayList<>();
    }

    public static String getReleaseString() {return release.name;}

    public static synchronized void addToCommandList(List<SimpleCommand> commands) {
        if(commandsList == null) {
            commandsList = new ArrayList<>();
        }
        commandsList.addAll(commands);
    }

    public static synchronized List<SimpleCommand> getCommandsList() {
        return commandsList;
    }

    public static synchronized boolean isCommandInManList(int manNum, String manList, String command) {
        if(manList == null || manList.isEmpty()) {
            Log.e(TAG, Helpers.getApplicationContext().getString(R.string.manpage_parse_error));
            return false;
        }
        Document document = Jsoup.parse(manList);
        Elements hrefs = document.getElementsByTag("href");
        for(Element href : hrefs) {
            if(href.html().trim().equalsIgnoreCase(command + "." + manNum + ".html")) {
                return true;
            }
        }
        return false;
    }

    //TODO Create crawler to prepopulate command locations.
    public static synchronized String createSimpleCommandsJsonString() {
        StringBuilder jsonString = new StringBuilder();



        return jsonString.toString();
    }

    @Override
    public synchronized void addDescriptionToSimpleCommand(SimpleCommand command, String pageHtml) {
        Document document = Jsoup.parse(pageHtml);
        Elements h4List = document.select("#tableWrapper h4");
        Elements preList = document.select("#tableWrapper pre");

        preList.remove(0);

        for(Element h4 : h4List) {
            if(h4.text().equalsIgnoreCase("DESCRIPTION")) {
                int idx = h4List.indexOf(h4);
                if(h4.equals(h4List.get(idx))) {
                    command.addDescription(preList.get(idx).text());
                } else {
                    Log.e(TAG, "Error matching indexes while adding descriprtion.");
                }
            }
        }
    }

    @Override
    public ArrayList<SimpleCommand> crawlForManPages(String pageHtml, String url) {
        //String manDir = "man" + manN +"/";

        Document document = Jsoup.parse(pageHtml);
        Elements anchors = document.select(CRAWLER_SELECTOR);

        ArrayList<SimpleCommand> unfinishedCommands = new ArrayList<>();

        for(Element a : anchors) {
            //String path = manDir.concat(a.attr("href"));
            String path = url.concat(a.attr("href"));
            if(!path.endsWith(".html")) {
                continue;
            }
            String text = a.html();
            text = text.substring(0, text.indexOf('.'));
            unfinishedCommands.add(new SimpleCommand(text, path));
        }

        return unfinishedCommands;
    }

    @Override
    public ArrayList<String> crawlForManDirs(String pageHtml) {
        Document document = Jsoup.parse(pageHtml);
        Elements anchors = document.select(CRAWLER_SELECTOR);

        ArrayList<String> dirs = new ArrayList<>();

        for(Element a : anchors) {
            String path = a.attr("href");
            if(path.startsWith("man")) {
                dirs.add(path);
            }
        }

        return dirs;
    }


}
