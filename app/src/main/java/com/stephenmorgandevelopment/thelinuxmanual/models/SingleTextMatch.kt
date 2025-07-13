package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SingleTextMatch(
    val section: String,
    val startIndex: Int,
    val endIndex: Int,
) : Parcelable {
    companion object {
        @IgnoredOnParcel
        const val NO_TEXT_MATCH: Int = -27499
    }

}