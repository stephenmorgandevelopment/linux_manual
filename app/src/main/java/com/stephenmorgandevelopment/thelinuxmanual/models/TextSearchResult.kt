package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextSearchResult(
    val query: String,
    val textMatches: List<SingleTextMatch>,
) : Parcelable {
    val count get(): Int = textMatches.size

    /**
     * @return SingleTextMatch - text match at index, index 0
     * if out of bounds, or null if list is empty.
     */
    fun getMatch(index: Int): SingleTextMatch? = try {
        textMatches[index]
    } catch (e: IndexOutOfBoundsException) {
        if (textMatches.isNotEmpty()) textMatches[0]
        else null
    }

    fun getSectionAt(index: Int): String? = getMatch(index)?.section
}