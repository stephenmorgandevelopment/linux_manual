package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

public record SingleTextMatch_Legacy(String section, int index) {

    public static final ForegroundColorSpan foregroundColorSpan
            = new ForegroundColorSpan(Helpers.color(R.color.colorPrimaryDark));

    public static final BackgroundColorSpan backgroundSpan
            = new BackgroundColorSpan(Helpers.color(R.color.textBubblesFont));
}
