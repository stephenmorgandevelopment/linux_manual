package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class HtmlTextFormat {
	private static final String NEWLINE_ESCAPE = "\\n";
	private static final String LINE_BREAK = "<br>";

	public static SpannableStringBuilder convertToHtmlAndReplaceNewLinesWithLineBreaks(String rawHtml) {
//		String text = String.valueOf(rawHtml);
		String text = rawHtml.replaceAll(NEWLINE_ESCAPE, LINE_BREAK);
		return new SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));		//FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
	}

	public static String replaceNewLinesWithLineBreaks(String text) {
		return text.replaceAll(NEWLINE_ESCAPE, LINE_BREAK);
	}
}
