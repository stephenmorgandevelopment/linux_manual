package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;

public class Preferences {
    private static final String DEFAULT_URL = "DEFAULT_URL";
    private static final String RELEASE = "RELEASE";

    public static void setRelease(String release) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext()).edit();
        editor.putString(RELEASE, release).commit();
    }

    public static String getRelease() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext());
        return prefs.getString(RELEASE, Ubuntu.Release.FOCAL.getName());
    }

    public static String getDefaultUrl() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext());
        return prefs.getString(Preferences.DEFAULT_URL, Helpers.getApplicationContext().getString(R.string.default_url));
    }

    public static void setDefaultUrl(String url) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext()).edit();
        editor.putString(DEFAULT_URL, url).commit();
    }

}
