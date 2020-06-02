package com.stephenmorgandevelopment.thelinuxmanual.distros;


import android.util.Log;
import android.widget.Switch;

import com.google.gson.stream.JsonWriter;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class Ubuntu implements LinuxDistro {
    public static final String TAG = Ubuntu.class.getSimpleName();
    public static final String NAME = Helpers.getApplicationContext().getString(R.string.ubuntu_name);

    public static final String BASE_URL = "https://manpages.ubuntu.com/manpages/";
    private static final String CRAWLER_SELECTOR = "#tableWrapper pre a";



    private static Release release;
    public enum Release {
        ARTFUL("artful"), BIONIC("bionic"), COSMIC("cosmic"), DISCO("disco")
        , EOAN("eoan"), FOCAL("focal"), GROOVY("groovy")
        ,PRECISE("precise"), TRUSY("trusty"), XENIAL("xenial");

        private String name;
        Release(String path) {
            this.name = path;
        }

        public String getName() {return name;}
    }

    public static void setRelease(String releaseString) {
        for(Release release : Release.values()) {
            if(releaseString.equalsIgnoreCase(release.getName())) {
                Ubuntu.release = release;
            }
        }
    }

    public static String getReleaseString() {return release.getName();}

    @Override
    public ArrayList<SimpleCommand> syncSimpleCommands() {

        return null;
    }

    //    public static synchronized boolean isCommandInManList(int manNum, String manList, String command) {
//        if(manList == null || manList.isEmpty()) {
//            Log.e(TAG, Helpers.getApplicationContext().getString(R.string.manpage_parse_error));
//            return false;
//        }
//        Document document = Jsoup.parse(manList);
//        Elements hrefs = document.getElementsByTag("href");
//        for(Element href : hrefs) {
//            if(href.html().trim().equalsIgnoreCase(command + "." + manNum + ".html")) {
//                return true;
//            }
//        }
//        return false;
//    }

    //TODO Create crawler to prepopulate command locations.
    public static synchronized String createSimpleCommandsJsonString() {
        StringBuilder jsonString = new StringBuilder();



        return jsonString.toString();
    }


    public static synchronized void addDescriptionToSimpleCommand(SimpleCommand command, String pageHtml) {
        Log.d(TAG, "Adding description for " + command.getName());

        Document document = Jsoup.parse(pageHtml);
        Elements h4List = document.select("#tableWrapper h4");
        Elements preList = document.select("#tableWrapper pre");

        preList.remove(0);

        for(Element h4 : h4List) {
            if(h4.text().equalsIgnoreCase("DESCRIPTION")) {
                int idx = h4List.indexOf(h4);
                if(h4.equals(h4List.get(idx))) {
                    command.setDescription(preList.get(idx).text());
                } else {
                    Log.e(TAG, "Error matching indexes while adding descriprtion.");
                }
            }
        }
    }

    public static ArrayList<SimpleCommand> crawlForManPages(String pageHtml, String url) {
        String[] tmpArr = url.split("/");
        int manN = Integer.parseInt(tmpArr[tmpArr.length-1].replaceAll("man", ""));

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
            text = text.substring(0, text.lastIndexOf('.'));
            unfinishedCommands.add(new SimpleCommand(text, path, manN));
        }

        return unfinishedCommands;
    }


    public static ArrayList<String> crawlForManDirs(String pageHtml) {
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

//    public static synchronized void writeSimpleCommandsToDisk(int page) {
//        writeSimpleCommandsToDisk(true, page);
//    }

//    public static synchronized void writeSimpleCommandsToDisk(List<SimpleCommand> commands, int page) {
//        OutputStream outStream = null;
//        File simpleCommandsFile = new File(Helpers.getFilesDir(), "simple_commands_" + page + ".json");
////        Gson gson = new Gson();
//
//        Log.d(TAG, "Writing simple commands to disk for page " + page + ".");
//
//        JSONObject rootObject = new JSONObject();
//        JSONArray jsonArray = new JSONArray();
//
//        for(SimpleCommand command : commands) {
//            jsonArray.put(command.toJSONObject());
//        }
//
//        try {
//            rootObject.put("commands", jsonArray);
//        } catch (JSONException e) {
//            Log.e(TAG, "JSON error.");
//            e.printStackTrace();
//        }
//
//        try {
//            outStream = new FileOutputStream(simpleCommandsFile);
//            OutputStreamWriter writer = new OutputStreamWriter(outStream);
//
//            writer.write(rootObject.toString());
//            writer.flush();
//            writer.close();
//            outStream.close();
//
//            Log.d(TAG, "Simple commands written successfully.");
//        } catch (IOException ioe) {
//            Log.e(TAG, "IO error writing commands file.");
//            ioe.printStackTrace();
//        }

//        try {
//            outStream = new FileOutputStream(simpleCommandsFile);
//
//            JsonWriter writer = new JsonWriter(new OutputStreamWriter(outStream, "UTF-8"));
//            writer.setIndent("    ");
//
//            writer.beginArray();
//            for(SimpleCommand command : commands) {
//                writeCommand(writer, command);
//            }
//            writer.endArray();
//
//            writer.close();
//            outStream.close();
//
//            Log.d(TAG, "Simple commands written successfully for page " + page + ".");
//        } catch (IOException ioe) {
//            Log.e(TAG, "IO error writing commands file for page " + page + ".");
//            ioe.printStackTrace();
//        }
//    }

    public static synchronized void writeCommand(JsonWriter writer, SimpleCommand command) throws IOException {
        writer.beginObject();
        writer.name("id").value(command.getId());
        writer.name("name").value(command.getName());
        writer.name("description").value(command.getDescription());
        writer.name("url").value(command.getUrl());
        writer.name("manN").value(command.getManN());
        writer.endObject();
    }



//    public static synchronized void writeSimpleCommandsToDisk(boolean overwrite, int page) {
//        OutputStream outStream = null;
//        File simpleCommandsFile = new File(Helpers.getFilesDir(), "simple_commands_" + page + ".json");
//        Gson gson = new Gson();
//
//        if(overwrite) {
//            Log.d(TAG, "Writing simple commands to disk.");
//
//            JSONObject rootObject = new JSONObject();
//            JSONArray jsonArray = new JSONArray();
//
//            for(SimpleCommand command : getCommandsList()) {
//                jsonArray.put(command.toJSONObject());
//            }
//
//            try {
//                rootObject.put("commands", jsonArray);
//            } catch (JSONException e) {
//                Log.e(TAG, "JSON error.");
//                e.printStackTrace();
//            }
//
//            try {
//                outStream = new FileOutputStream(simpleCommandsFile);
//                OutputStreamWriter writer = new OutputStreamWriter(outStream);
//
//                writer.write(rootObject.toString());
//                writer.flush();
//                writer.close();
//                outStream.close();
//
//                Log.d(TAG, "Simple commands written successfully.");
//            } catch (IOException ioe) {
//                Log.e(TAG, "IO error writing commands file.");
//                ioe.printStackTrace();
//            }
//        }
//
//
//    }

}
