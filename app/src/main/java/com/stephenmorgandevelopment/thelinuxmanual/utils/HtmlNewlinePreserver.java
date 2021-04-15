package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.database.CharArrayBuffer;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;

import java.nio.CharBuffer;

public class HtmlNewlinePreserver {
	private static final char[] PLACEHOLDER = "!!".toCharArray();
	private static final String PLACEHOLDER_ALT = "**";
	private static final String PLACEHOLDER_ALT_OTHER = "@!";

	private static final String NEWLINE_ESCAPE = "\\n";

	public static SpannableStringBuilder parse(String rawHtml) {
		char[] placeholderUsed = null;
		String text;

		if (!rawHtml.contains(String.valueOf(PLACEHOLDER))) {
			text = rawHtml.replaceAll(NEWLINE_ESCAPE, String.valueOf(PLACEHOLDER));
			placeholderUsed = PLACEHOLDER;	//.toCharArray();

		} else if (!rawHtml.contains(PLACEHOLDER_ALT)) {
			text = rawHtml.replaceAll(NEWLINE_ESCAPE, PLACEHOLDER_ALT);
			placeholderUsed = PLACEHOLDER_ALT.toCharArray();

		} else if (!rawHtml.contains(PLACEHOLDER_ALT_OTHER)) {
			text = rawHtml.replaceAll(NEWLINE_ESCAPE, PLACEHOLDER_ALT_OTHER);
			placeholderUsed = PLACEHOLDER_ALT_OTHER.toCharArray();

		} else {
			Log.i("HtmlNewlinePreserver", "Command data text contained all three stand-ins.");
			return SpannableStringBuilder.valueOf(Html.fromHtml(rawHtml, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
		}

		SpannableStringBuilder spannedText = SpannableStringBuilder.valueOf(
				Html.fromHtml(text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));



		char[] spannedCharArray = new char[spannedText.length()];
		spannedText.getChars(0, spannedText.length() - 1, spannedCharArray, 0);

		int index = 0;
		for (int i = 0; i < spannedCharArray.length; i++) {
			if (spannedCharArray[i] == placeholderUsed[0] && spannedCharArray[i + 1] == placeholderUsed[1]) {
//				spannedCharArray[i] = '\\';
//				spannedCharArray[i + 1] = 'n';


//				spannedCharArray[i] = System.getProperty("line.seperator");

			}
		}

		return SpannableStringBuilder.valueOf(CharBuffer.wrap(spannedCharArray));
	}
}
