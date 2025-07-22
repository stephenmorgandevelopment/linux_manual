package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.os.LocaleListCompat;

import java.io.File;
import java.util.Objects;

import javax.annotation.Nonnull;

public class Helpers {
    private static Context applicationContext;
    private static File cacheDir;
    private static String local;

    public static void init(Application app) {
        setApplicationContext(app);
        setCacheDir(app.getCacheDir());
        setLocale();
    }

    private static void setLocale() {
        try {
            local = Objects.requireNonNull(LocaleListCompat.getAdjustedDefault().get(0))
                    .toLanguageTag().substring(0, 2);
        } catch (NullPointerException npe) {
            local = "en";
        }
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

    public static @Nonnull Context getApplicationContext() {
        return applicationContext;
    }
}
