package com.stephenmorgandevelopment.thelinuxmanual.utils;

public class HtmlTextFormat {
    private static final String NEWLINE_ESCAPE = "\\n";
    private static final String LINE_BREAK = "<br>";

    public static String replaceNewLinesWithLineBreaks(String text) {
        return text.replaceAll(NEWLINE_ESCAPE, LINE_BREAK);
    }
}
