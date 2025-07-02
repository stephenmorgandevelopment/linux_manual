package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import android.text.Html
import android.text.SpannableStringBuilder
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.StringReader
import java.util.Locale

@Parcelize
data class Command(
    val id: Long,
    val data: Map<String, String>,
) : Parcelable {

    @IgnoredOnParcel
    @Ignore
    private var _shortName: String? = null

    fun getShortName(): String = _shortName ?: genShortName().also { _shortName = it }

    fun genShortName(): String {
        var name = Html.fromHtml(data["NAME"], Html.FROM_HTML_MODE_LEGACY)
            .toString().let {
                it.substring(0, it.indexOf(" "))
            }

        if (name.contains(",")) {
            name = name.substring(0, name.indexOf(","))
        }

        return name
    }

    fun dataMapToJsonString(): String {
        val gson = Gson()
        return gson.toJson(this.data)
    }

    fun searchDataForTextMatch(query: String): TextSearchResult {
        val matchIndexes: MutableList<SingleTextMatch> = mutableListOf<SingleTextMatch>()

        for (entry in data.entries) {
            val tmpText = entry.value.lowercase(Locale.getDefault())
            val lowerCaseQuery = query.lowercase(Locale.getDefault())

            if (tmpText.contains(lowerCaseQuery)) {
                val textString = SpannableStringBuilder(
                    Html.fromHtml(tmpText, Html.FROM_HTML_MODE_LEGACY)
                ).toString()

                val indexes: List<SingleTextMatch> = getMatchIndexes(
                    query = lowerCaseQuery,
                    text = textString,
                    header = entry.key
                )

                matchIndexes.addAll(indexes)
            }
        }

        return TextSearchResult(query, matchIndexes.toList())
    }

    fun getMatchIndexes(query: String, text: String, header: String): List<SingleTextMatch> {
        val indexes: MutableList<SingleTextMatch> = mutableListOf<SingleTextMatch>()

        var mutableText = text
        var runningIndex = 0
        while (mutableText.contains(query)) {
            val idx = text.indexOf(query)
            indexes.add(SingleTextMatch(header, idx + runningIndex))
            mutableText = text.substring(idx + query.length)

            runningIndex += (idx + query.length)
        }

        return indexes.toList()
    }

    companion object {
        fun fromJson(id: Long, dataJson: String) = Command(id, parseMapFromJson(dataJson))

        fun parseMapFromJson(json: String): Map<String, String> {
            val reader = JsonReader(StringReader(json))

            //  TODO: Point of interest for issues.
            //  reader.setLenient(true);
            reader.setStrictness(Strictness.LEGACY_STRICT)
            //  reader.setStrictness(Strictness.LENIENT);

            val dataMapType = object : TypeToken<MutableMap<String?, String?>?>() {}.type
            return Gson().fromJson(reader, dataMapType)
        }
    }
}




