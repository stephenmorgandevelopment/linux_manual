package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

public class SingleTextMatch {
    private final String section;
    private final int index;

    public SingleTextMatch(String section, int index) {
        this.section = section;
        this.index = index;
    }

    public String getSection() {
        return section;
    }

    public int getIndex() {
        return index;
    }

    public static final ForegroundColorSpan foregroundColorSpan
            = new ForegroundColorSpan(Helpers.color(R.color.colorPrimaryDark));

    public static final BackgroundColorSpan backgroundSpan
            = new BackgroundColorSpan(Helpers.color(R.color.textBubblesFont));
}
