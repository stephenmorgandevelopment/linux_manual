package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stephenmorgandevelopment.thelinuxmanual.R;

import java.io.File;

public class Helpers {
    private static Context applicationContext;
    private static File filesDir;
    private static File cacheDir;
    private static String local;

    public static void init(Application app) {
        setApplicationContext(app);
        setFilesDir(app.getFilesDir());
        setCacheDir(app.getCacheDir());
    }


    private static void setLocal() {
        //TODO Set local according to systems default language code.

        local = "en";
    }

    public static String makeUrl(String baseUrl, String... paths) {
        String url = baseUrl;
        if(url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        for(String path : paths) {
            url += "/".concat(path);
        }

        return url;
    }

    public static String getLocal() {return local;}

    private static void setFilesDir(File dir) {
        filesDir = dir;
    }

    public static File getFilesDir() {return filesDir;}

    private static void setCacheDir(File dir) {
        cacheDir = dir;
    }

    public static File getCacheDir() {return cacheDir;}

    private static void setApplicationContext(Application application) {
        applicationContext = application.getApplicationContext();
    }

    public static Context getApplicationContext() {return applicationContext;}

}
