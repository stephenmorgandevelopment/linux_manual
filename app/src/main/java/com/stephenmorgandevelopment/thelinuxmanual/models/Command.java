package com.stephenmorgandevelopment.thelinuxmanual.models;

import android.text.Html;
import android.text.SpannableString;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;

public class Command {
    private final long id;
    private final Map<String, String> data;

    public Command(long id, Map<String, String> data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public String getShortName() {
        String name = String.valueOf(Html.fromHtml(data.get("NAME")));
        Log.i("Command", "shortName: " + name);

        if (name == null) {
            return "unknown-error";
        }

        name = name.substring(0, name.indexOf(" "));

        if (name.contains(",")) {
            name = name.substring(0, name.indexOf(","));
        }

        return name;
    }

    public Map<String, String> getData() {
        return data;
    }

    public static Map<String, String> parseMapFromJson(String json) {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);

        Type dataMapType = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(reader, dataMapType);
    }

    public static Command fromJson(long id, String dataJson) {
        return new Command(id, parseMapFromJson(dataJson));
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String dataMapToJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this.data);
    }

    public TextSearchResult searchDataForTextMatch(String query) {
        int count = 0;
        List<SingleTextMatch> matchIndexes = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
                List<SingleTextMatch> indexes =
                        getMatchIndexes(query.toLowerCase(), entry);

                count += indexes.size();

                matchIndexes.addAll(indexes);
            }
        }

        return new TextSearchResult(query, matchIndexes, count);
    }

    private List<SingleTextMatch> getMatchIndexes(String query, Map.Entry<String, String> entry) {
        List<SingleTextMatch> indexes = new ArrayList<>();
        SpannableString spannableString = SpannableString.valueOf(Html.fromHtml(entry.getValue(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE));

        String tmpText = String.valueOf(spannableString).toLowerCase();

        int runningIndex = 0;
        while (tmpText.contains(query)) {
            int idx = tmpText.indexOf(query); // + runningIndex;
            indexes.add(new SingleTextMatch(entry.getKey(), idx + runningIndex));
            tmpText = tmpText.substring(idx + query.length());

            runningIndex += (idx + query.length());
        }

        return indexes;
    }
}
