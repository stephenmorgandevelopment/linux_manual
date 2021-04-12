package com.stephenmorgandevelopment.thelinuxmanual.distros;


import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.stream.JsonWriter;
import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;


public class UbuntuHtmlAdapter {
    public static final String TAG = UbuntuHtmlAdapter.class.getSimpleName();
    public static final String NAME = "Ubuntu";
    public static final String BASE_URL = "https://manpages.ubuntu.com/manpages/";
    private static final String CRAWLER_SELECTOR = "#tableWrapper pre a";

    private static Release release;

    public enum Release {
        ARTFUL("artful"), BIONIC("bionic"), COSMIC("cosmic"), DISCO("disco"), EOAN("eoan"), FOCAL("focal"), GROOVY("groovy"), HIRSUTE("hirsute"), PRECISE("precise"), TRUSY("trusty"), XENIAL("xenial");

        private String name;

        Release(String path) {
            this.name = path;
        }

        public String getName() {
            return name;
        }

        public static Release fromString(String releaseString) {
            for (Release release : Release.values()) {
                if (releaseString.equalsIgnoreCase(release.getName())) {
                    return release;
                }
            }

            Log.d(TAG, "Error matching release string: " + releaseString);
            return FOCAL;
        }
    }

    public static void setRelease(Release release) {
        UbuntuHtmlAdapter.release = release;
    }

    public static String getReleaseString() {
        return release.getName();
    }

    public static Single<Map<String, String>> crawlForCommandInfo(Response response)  throws IOException {
        return Single.just(crawlForCommandInfo(response.body().string()));
    }


    public static Map<String, String> crawlForCommandInfo(String pageHtml) {
        Map<String, String> info = new LinkedHashMap<>();

        Document document = Jsoup.parse(pageHtml);
        Elements h4List = document.select("#tableWrapper h4");
        Elements preList = document.select("#tableWrapper pre");
        preList.remove(0);

        for (Element h4 : h4List) {
            int idx = h4List.indexOf(h4);
            info.put(h4.text(), preList.get(idx).outerHtml());
        }

        return info;
    }

    public static synchronized void addDescriptionToSimpleCommand(SimpleCommand command, String pageHtml) {
        Document document = Jsoup.parse(pageHtml);
        Elements h4List = document.select("#tableWrapper h4");
        Elements preList = document.select("#tableWrapper pre");

        preList.remove(0);

        for (Element h4 : h4List) {
            if (h4.text().toUpperCase().contains("DESCRIPTION")) {
                command.setDescription(preList.get(h4List.indexOf(h4)).outerHtml());
                if (h4.text().equals("DESCRIPTION")) {
                    break;
                }
            }
        }

        if (command.getDescription().equals("")) {
            command.setDescription("No description available.");
        }
    }

    public static synchronized String crawlForDescription(String pageHtml) {
        Document document = Jsoup.parse(pageHtml);
        Elements h4List = document.select("#tableWrapper h4");
        Elements preList = document.select("#tableWrapper pre");

        preList.remove(0);

        String description = null;
        for (Element h4 : h4List) {
            if (h4.text().toUpperCase().contains("DESCRIPTION")) {
                description = preList.get(h4List.indexOf(h4)).outerHtml();
                if (h4.text().equals("DESCRIPTION")) {
                    return description;
                }
            }
        }

        if (description == null) {
            return "No description available.";
        }

        return description;
    }

    public static ArrayList<SimpleCommand> crawlForManPages(String pageHtml, String url) {
        String[] tmpArr = url.split("/");
        int manN = Integer.parseInt(tmpArr[tmpArr.length - 1].replaceAll("man", ""));
        String filter = "." + manN;

        Document document = Jsoup.parse(pageHtml);
        Elements anchors = document.select(CRAWLER_SELECTOR);

        ArrayList<SimpleCommand> unfinishedCommands = new ArrayList<>();
        for (Element a : anchors) {
            String path = url.concat(a.attr("href"));
            if (!path.endsWith(".html")) {
                continue;
            }

            String text = a.html();
            try {
                text = text.substring(0, text.lastIndexOf(filter));
            } catch (Exception e) {
                text = a.html();
                text = text.substring(0, text.lastIndexOf('.'));
            }

            unfinishedCommands.add(new SimpleCommand(text, path, manN));
        }

        Log.i(TAG, "Added commands for man" + manN);
        return unfinishedCommands;
    }

    public static ArrayList<String> crawlForManDirs(String pageHtml) {
        Document document = Jsoup.parse(pageHtml);
        Elements anchors = document.select(CRAWLER_SELECTOR);

        ArrayList<String> dirs = new ArrayList<>();

        for (Element a : anchors) {
            String path = a.attr("href");
            if (path.startsWith("man")) {
                dirs.add(path);
            }
        }

        return dirs;
    }

    public static synchronized void writeCommand(JsonWriter writer, SimpleCommand command) throws IOException {
        writer.beginObject();
        writer.name("id").value(command.getId());
        writer.name("name").value(command.getName());
        writer.name("description").value(command.getDescription());
        writer.name("url").value(command.getUrl());
        writer.name("manN").value(command.getManN());
        writer.endObject();
    }
}
