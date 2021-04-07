package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlAdapter;

import java.util.ResourceBundle;

public class Preferences {
    private static final String RELEASE = "RELEASE";
//    private static final String COLOR_PRIMARY = "COLOR_PRIMARY";
//    private static final String COLOR_SECONDARY = "COLOR_SECONDARY";
//    private static final String COLOR_THIRD = "COLOR_THIRD";
//    private static final String COLOR_ACCENT = "COLOR_ACCENT";

    private static final String LIST_FONT_SIZE = "LIST_FONT_SIZE";
    private static final String DETAILS_FONT_SIZE = "DETAILS_FONT_SIZE";

    @SuppressLint("ApplySharedPref")
    public static void setRelease(String release) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext()).edit();
        editor.putString(RELEASE, release).commit();
    }

    public static String getRelease() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext());
        return prefs.getString(RELEASE, UbuntuHtmlAdapter.Release.FOCAL.getName());
    }

    public static int getListFontSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext());
        return prefs.getInt(LIST_FONT_SIZE, 16);
    }

    public static int getDetailsFontSize() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext());
        return prefs.getInt(DETAILS_FONT_SIZE, 16);
    }

    @SuppressLint("ApplySharedPref")
    public static void setListFontSize(int size) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext()).edit();
        editor.putInt(LIST_FONT_SIZE, size).commit();
    }

    @SuppressLint("ApplySharedPref")
    public static void setDetailsFontSize(int size) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Helpers.getApplicationContext()).edit();
        editor.putInt(LIST_FONT_SIZE, size).commit();
    }
}
