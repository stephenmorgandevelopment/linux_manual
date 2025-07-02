package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.google.gson.Gson;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Command_Legacy(long id, Map<String, String> data) {

    public String getShortName() {
        String name = String.valueOf(Html.fromHtml(data.get("NAME"), Html.FROM_HTML_MODE_LEGACY));
        name = name.substring(0, name.indexOf(" "));

        if (name.contains(",")) {
            name = name.substring(0, name.indexOf(","));
        }

        return name;
    }

    public static Command fromJson(long id, String dataJson) {
        return new Command(id, parseMapFromJson(dataJson));
    }

    public static Map<String, String> parseMapFromJson(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        //  TODO: Point of interest for issues.
//        reader.setLenient(true);
//        reader.setStrictness(Strictness.LEGACY_STRICT);
        reader.setStrictness(Strictness.LENIENT);

        Type dataMapType = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(reader, dataMapType);
    }

    public String dataMapToJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this.data);
    }

    public TextSearchResult searchDataForTextMatch(String query) {
        int count = 0;
        List<SingleTextMatch> matchIndexes = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String tmpText = entry.getValue().toLowerCase();

            if (tmpText.contains(query.toLowerCase())) {
                SpannableStringBuilder spannableStringBuilder =
                        new SpannableStringBuilder(Html.fromHtml(tmpText, Html.FROM_HTML_MODE_LEGACY));

                List<SingleTextMatch> indexes = getMatchIndexes(
                        query.toLowerCase(),
                        String.valueOf(spannableStringBuilder),
                        entry.getKey());

                count += indexes.size();
                matchIndexes.addAll(indexes);
            }
        }

        return new TextSearchResult(query, matchIndexes);
    }

    private List<SingleTextMatch> getMatchIndexes(String query, String text, String header) {
        List<SingleTextMatch> indexes = new ArrayList<>();

        int runningIndex = 0;
        while (text.contains(query)) {
            int idx = text.indexOf(query);
            indexes.add(new SingleTextMatch(header, idx + runningIndex));
            text = text.substring(idx + query.length());

            runningIndex += (idx + query.length());
        }

        return indexes;
    }
}
