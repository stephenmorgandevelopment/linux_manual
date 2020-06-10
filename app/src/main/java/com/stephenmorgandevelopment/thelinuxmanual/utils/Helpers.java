package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.os.LocaleListCompat;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;

import java.io.File;
import java.util.Locale;

public class Helpers {
    private static final String TAG = Helpers.class.getSimpleName();

    private static Context applicationContext;
    private static File filesDir;
    private static File cacheDir;
    private static String local;

    public static void init(Application app) {
        setApplicationContext(app);
        setFilesDir(app.getFilesDir());
        setCacheDir(app.getCacheDir());
        setLocal();

        Ubuntu.setRelease(Preferences.getRelease());
    }


    private static void setLocal() {
        String languageTag = LocaleListCompat.getAdjustedDefault().get(0).toLanguageTag().substring(0, 2);
        Log.i(TAG, "Language tag: " + languageTag);
        local = languageTag;
//        local = "en";
    }

    public static boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null) {
                return networkInfo.isConnectedOrConnecting();
            }
        }

        return false;
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
