package com.stephenmorgandevelopment.thelinuxmanual.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.defaultTextStyle

val loadingString = stringFromRes(R.string.fetching_data)
val noInternedString = stringFromRes(R.string.offline_description_preview)

private val charactersToSanitize = listOf(
    '!', ';', '&', '"', '#', '\'', '\\',
)

fun sanitizeInput(text: String): String {
    return text.filterNot { charactersToSanitize.contains(it) }
}

val String.queryAdjusted get() = if (length >= 4) "%$this%" else "$this%"

fun stringFromRes(id: Int) =
    Helpers.getApplicationContext()?.resources?.getString(id)
        ?: "Preview string" // Needed for previews to work correctly

fun stringFromRes(id: Int, vararg params: Any) =
    Helpers.getApplicationContext()?.resources?.getString(id, *params)
        ?: "Preview string" // Needed for previews to work correctly

@Composable
fun getString(id: Int): String {
    return LocalResources.current.getString(id)
}

@Composable
fun getString(id: Int, vararg params: Any): String {
    return LocalResources.current.getString(id, *params)
}

@Composable
fun makeComposableText(id: Int, style: TextStyle = defaultTextStyle): @Composable () -> Unit {
    return { Text(getString(id), style = style) }
}

fun TextLayoutResult.calculateScrollOffsetFor(charIndex: Int): Int {
    return getLineBaseline(
        getLineForOffset(charIndex)
    ).toInt()
}

private var syncTextDots: Int = 0
fun showSyncText(title: String, syncProgress: String): String {
    val textDots = (++syncTextDots % 3).let {
        return@let when (it) {
            0 -> "."
            1 -> ".."
            2 -> "..."
            else -> ""
        }
    }
    return "$title\n\nSyncing$textDots\n\n$syncProgress"
}
