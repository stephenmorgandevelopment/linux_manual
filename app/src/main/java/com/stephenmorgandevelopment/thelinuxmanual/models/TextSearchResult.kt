package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextSearchResult(
    val query: String,
    val matchIndexes: List<SingleTextMatch>,
) : Parcelable {
    val count get(): Int = matchIndexes.size

    /**
     * @return SingleTextMatch
     * @throws IndexOutOfBoundsException - if index is out of bounds...duh.
     */
    fun getMatch(index: Int): SingleTextMatch = try {
        matchIndexes[index]
    } catch (e: IndexOutOfBoundsException) {
        if (matchIndexes.isNotEmpty()) matchIndexes[0]
        else throw e
    }
}