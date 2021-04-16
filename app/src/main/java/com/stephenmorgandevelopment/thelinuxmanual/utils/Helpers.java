package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.core.os.LocaleListCompat;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;

import java.io.File;

public class Helpers {
    private static final String TAG = Helpers.class.getSimpleName();

    private static Context applicationContext;
    private static File cacheDir;
    private static String local;

    public static void init(Application app) {
        setApplicationContext(app);
        setCacheDir(app.getCacheDir());
        setLocale();

        UbuntuHtmlApiConverter.setRelease(UbuntuHtmlApiConverter.Release.fromString(Preferences.getRelease()));
    }

    private static void setLocale() {
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

    public static String getLocale() {
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

    public static String string(int id) {
        return applicationContext.getString(id);
    }

    public static CharSequence text(int id) {
        return applicationContext.getText(id);
    }

    public static int color(int id) {
        return applicationContext.getColor(id);
    }
}
