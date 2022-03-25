package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;

public enum Release {
    BIONIC("bionic"),
    FOCAL("focal"),
    HIRSUTE("hirsute"),
    IMPISH("impish"),
    JAMMY("jammy"),
    TRUSTY("trusty"),
    XENIAL("xenial");

    private final String name;

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

        Log.d(UbuntuHtmlApiConverter.TAG, "Error matching release string: " + releaseString);
        return FOCAL;
    }
}
