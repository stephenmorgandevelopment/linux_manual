package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.google.gson.Gson
import com.google.gson.Strictness
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.parcelize.Parcelize
import java.io.StringReader
import java.util.Locale

@Parcelize
data class Command(
    val id: Long,
    val data: Map<String, String>,
) : Parcelable {
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
                val textString = AnnotatedString.fromHtml(tmpText)

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

    private fun getMatchIndexes(
        query: String,
        text: AnnotatedString,
        header: String,
    ): List<SingleTextMatch> {
        val indexes: MutableList<SingleTextMatch> = mutableListOf<SingleTextMatch>()

        var mutableText = text
        var runningIndex = 0
        while (mutableText.contains(query)) {
            val idx = mutableText.indexOf(query)

            (idx + runningIndex).let {
                indexes.add(
                    SingleTextMatch(header, it, it.plus(query.length))
                )
            }

            runningIndex += (idx + query.length)
            mutableText = text.subSequence(runningIndex, text.lastIndex)
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




