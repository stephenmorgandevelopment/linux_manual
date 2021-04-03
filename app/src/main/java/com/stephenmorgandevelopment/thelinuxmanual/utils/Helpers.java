package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.core.os.LocaleListCompat;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;

import java.io.File;

public class Helpers {
    private static final String TAG = Helpers.class.getSimpleName();

    private static Context applicationContext;
    private static File cacheDir;
    private static String local;

    public static void init(Application app) {
        setApplicationContext(app);
        setCacheDir(app.getCacheDir());
        setLocal();

        UbuntuHtmlAdapter.setRelease(UbuntuHtmlAdapter.Release.fromString(Preferences.getRelease()));
    }

    private static void setLocal() {
        String languageTag = LocaleListCompat.getAdjustedDefault().get(0).toLanguageTag().substring(0, 2);
        Log.i(TAG, "Language tag: " + languageTag);
        local = languageTag;
    }

    public static boolean hasInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isConnectedOrConnecting();
            }
        }

        return false;
    }

    public static String getLocal() {
        return local;
    }

    private static void setCacheDir(File dir) {
        cacheDir = dir;
    }

    public static File getCacheDir() {
        return cacheDir;
    }

    private static void setApplicationContext(Application application) {
        applicationContext = application.getApplicationContext();
    }

    public static Context getApplicationContext() {
        return applicationContext;
    }

    public static void cleanup() {
        applicationContext = null;
    }
}
