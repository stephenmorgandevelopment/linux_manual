package com.stephenmorgandevelopment.thelinuxmanual.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.defaultTextStyle

internal val loadingString = stringFromRes(R.string.fetching_data)
internal val noInternedString = stringFromRes(R.string.offline_description_preview)
internal const val NULL_STRING = "null"

internal fun String.trim(string: String) = this.trim(*string.toCharArray())

private val charactersToSanitize = listOf(
    '!', ';', '&', '"', '#', '\'', '\\',
)

internal fun sanitizeInput(text: String): String {
    return text.filterNot { charactersToSanitize.contains(it) }
}

internal val String.queryAdjusted get() = if (length >= 4) "%$this%" else "$this%"

internal fun stringFromRes(id: Int) =
    Helpers.getApplicationContext()?.resources?.getString(id)
        ?: "Preview string" // Needed for previews to work correctly

internal fun stringFromRes(id: Int, vararg params: Any) =
    Helpers.getApplicationContext()?.resources?.getString(id, *params)
        ?: "Preview string" // Needed for previews to work correctly

@Composable
internal fun getString(id: Int): String {
    return LocalContext.current.resources.getString(id)
}

@Composable
internal fun getString(id: Int, vararg params: Any): String {
    return LocalContext.current.resources.getString(id, *params)
}

@Composable
internal fun makeComposableText(
    id: Int,
    style: TextStyle = defaultTextStyle
): @Composable () -> Unit {
    return { Text(getString(id), style = style) }
}

internal fun TextLayoutResult.calculateScrollOffsetFor(charIndex: Int): Int {
    return getLineBaseline(
        getLineForOffset(charIndex)
    ).toInt()
}

private var syncTextDots: Int = 0
internal fun showSyncText(syncProgress: String): String {
    val textDots = (++syncTextDots % 3).let {
        return@let when (it) {
            0 -> "."
            1 -> ".."
            2 -> "..."
            else -> ""
        }
    }
    return "\n\nSyncing$textDots\n\n$syncProgress"
}
