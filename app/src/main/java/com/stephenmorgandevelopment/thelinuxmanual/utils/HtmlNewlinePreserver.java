package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;

public class HtmlNewlinePreserver {
	private static final String NEWLINE_ESCAPE = "\\n";
	private static final String LINE_BREAK = "<br>";

	public static SpannableStringBuilder replaceNLinesWithLBreaks(String rawHtml) {
		String text = rawHtml.replaceAll(NEWLINE_ESCAPE, LINE_BREAK);
		return new SpannableStringBuilder(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));		//FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
	}
}
