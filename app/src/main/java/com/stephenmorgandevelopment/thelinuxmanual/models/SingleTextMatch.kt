package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers
import kotlinx.parcelize.Parcelize

@Parcelize
data class SingleTextMatch(
    val section: String,
    val index: Int,
) : Parcelable {
    companion object {
        val foregroundColorSpan = ForegroundColorSpan(
            Helpers.color(R.color.colorPrimaryDark)
        )

        val backgroundSpan = BackgroundColorSpan(
            Helpers.color(R.color.textBubblesFont)
        )
    }
}